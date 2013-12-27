package kvstore

import akka.actor.{ OneForOneStrategy, Props, ActorRef, Actor, Cancellable }
import kvstore.Arbiter._
import scala.collection.immutable.Queue
import akka.actor.SupervisorStrategy.Restart
import scala.annotation.tailrec
import akka.pattern.{ ask, pipe }
import akka.actor.Terminated
import scala.concurrent.duration._
import akka.actor.PoisonPill
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.util.Timeout
import scala.language.postfixOps

object Replica {
  sealed trait Operation {
    def key: String
    def id: Long
  }
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  case class GenerateFailure(id: Long)
  
  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {
  import Replica._
  import Replicator._
  import Persistence._
  import context.dispatcher

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */
  
  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators = Set.empty[ActorRef]

  var snapshotSeq = 0
  var persistAcks = Map.empty[Long, ActorRef]
  var replicateAcks = Map.empty[Long, (ActorRef, Set[ActorRef])]
  var persistRepeaters = Map.empty[Long, Cancellable]
  var failureGenerators = Map.empty[Long, Cancellable]
  
  val persistor = context.actorOf(persistenceProps)
  
  arbiter ! Join
  
  def receive = {
    case JoinedPrimary   ⇒ context.become(leader)
    case JoinedSecondary ⇒ context.become(replica)
  }

  /* TODO Behavior for  the leader role. */
  val leader: Receive = {
    case Insert(key, value, id) =>
      kv += (key -> value)
      persistAcks += id -> sender

      if(replicators.nonEmpty) {
        replicateAcks += id -> (sender, replicators)
        replicators.foreach { replicator =>
          replicator ! Replicate(key, Some(value), id)
        }
      }

      persistRepeaters += id -> context.system.scheduler.schedule(
        0 millis, 100 millis, persistor, Persist(key, Some(value), id)
      )

      failureGenerators += id -> context.system.scheduler.scheduleOnce(1 second) {
        self ! GenerateFailure(id)
      }

    case Remove(key, id) =>
      kv -= key
      persistAcks += id -> sender

      if(replicators.nonEmpty) {
        replicateAcks += id -> (sender, replicators)
        replicators.foreach { replicator =>
          replicator ! Replicate(key, None, id)
        }
      }

      persistRepeaters += id -> context.system.scheduler.schedule(
        0 millis, 100 millis, persistor, Persist(key, None, id)
      )

      failureGenerators += id -> context.system.scheduler.scheduleOnce(1 second) {
        self ! GenerateFailure(id)
      }

    case Get(key, id) =>
//    println(s"leader -> Get($key,$id) <- $sender")
      sender ! GetResult(key, kv.get(key), id)
      
    case Persisted(key, id) =>
      persistRepeaters(id).cancel()
      persistRepeaters -= id
      val origSender = persistAcks(id)
      persistAcks -= id
      if(!replicateAcks.contains(id)) {
        failureGenerators(id).cancel()
        failureGenerators -= id
        origSender ! OperationAck(id)
      }
      
    case Replicated(key, id) =>
      if(replicateAcks.contains(id)) {
        val (origSender, currAckSet) = replicateAcks(id)
        val newAckSet = currAckSet - sender
        if (newAckSet.isEmpty)
          replicateAcks -= id
        else
          replicateAcks = replicateAcks.updated(id, (origSender, newAckSet))
        if(!replicateAcks.contains(id) && !persistAcks.contains(id)) {
          failureGenerators(id).cancel()
          failureGenerators -= id
          origSender ! OperationAck(id)
        }
      }
      
    case Replicas(replicas) =>
      val secondaryReplicas = replicas.filterNot(_ == self)
      val removed = secondaries.keySet -- secondaryReplicas
      val added = secondaryReplicas -- secondaries.keySet

      var addedSecondaries = Map.empty[ActorRef, ActorRef]
      val addedReplicators = added.map { replica =>
        val replicator = context.actorOf(Replicator.props(replica))
        addedSecondaries += replica -> replicator
        replicator
      }

      removed.foreach( replica => secondaries(replica) ! PoisonPill )

      removed.foreach { replica =>
        replicateAcks.foreach { case (id, (origSender, rs)) =>
          if (rs.contains(secondaries(replica))) {
            self.tell(Replicated("", id), secondaries(replica))
          }
        }
      }

      replicators = replicators -- removed.map(secondaries) ++ addedReplicators
      secondaries = secondaries -- removed ++ addedSecondaries

      addedReplicators.foreach { replicator =>
        kv.zipWithIndex.foreach { case ((k,v), idx) =>
          replicator ! Replicate(k, Some(v), idx)
        }
      }
      
    case GenerateFailure(id) =>
      if (failureGenerators.contains(id)) {
        if (persistRepeaters.contains(id)) {
          persistRepeaters(id).cancel()
          persistRepeaters -= id
        }
        failureGenerators -= id
        
        val origSender = if (persistAcks.contains(id)) persistAcks(id)
                         else replicateAcks(id)._1
        persistAcks -= id
        replicateAcks -= id
        origSender ! OperationFailed(id)
      }      
  }

  /* TODO Behavior for the replica role. */
  var expectedSeq = 0L

  override def preStart(): Unit = {}
  override def postStop(): Unit = {}

  var replicaSenders = Map.empty[Long, ActorRef]
  
  val replica: Receive = {
    
    case Get(key, id) =>
//    println(s"replica -> Get($key,$id) <- $sender")
      sender ! GetResult(key, kv.get(key), id)
    
    case Snapshot(key, valueOption, seq) =>
//    println(s"Snapshot($key, $valueOption, $seq)")
      if (seq < snapshotSeq)
        sender ! SnapshotAck(key, seq)

      if (seq == snapshotSeq) {
        valueOption match {
          case None => kv -= key
          case Some(value) => kv += key -> value
        }
        snapshotSeq += 1
        persistAcks += seq -> sender

        persistRepeaters += seq -> context.system.scheduler.schedule(
          0 millis, 100 millis, persistor, Persist(key, valueOption, seq)
        )
      }

    
    case Persisted(key, id) =>
//    println(s"Persisted($key, $id)")
      val sender = persistAcks(id)
      persistAcks -= id
      persistRepeaters(id).cancel()
      persistRepeaters -= id
      sender ! SnapshotAck(key, id)

  }
}
