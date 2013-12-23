package kvstore

import akka.actor.{ OneForOneStrategy, Props, ActorRef, Actor }
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

  var persistor = context.actorOf(Persistence.props(false))
  
  arbiter ! Join
  
  def receive = {
    case JoinedPrimary   ⇒ context.become(leader)
    case JoinedSecondary ⇒ context.become(replica)
  }

  /* TODO Behavior for  the leader role. */
  val leader: Receive = {
    case Insert(key, value, id) =>
      kv += (key -> value)
      replicators foreach { rep => rep ! Replicate(key, Some(value), id) }
      sender ! OperationAck(id)
    case Remove(key, id) =>
      kv -= key
      replicators foreach { rep => rep ! Replicate(key, None, id) }
      sender ! OperationAck(id)
    case Get(key, id) =>
      println(s"leader -> Get($key,$id) <- $sender")
      sender ! GetResult(key, kv.get(key), id)
    case Replicated(key, id) =>
      
    case Replicas(replicas) =>
      replicas foreach (r =>
        if (r != self && !secondaries.contains(r)) {    // add new secondary
          val replicator = context.actorOf(Replicator.props(r))

          kv.foreach( keyValue => replicator ! Replicate(keyValue._1, Some(keyValue._2), 0L) )

          replicators += replicator
          secondaries += (r -> replicator)
        }
      )
      
      secondaries foreach (r => {   // there are registered secondaries which are not in the replica set
        if (!replicas.contains(r._1)) {
          secondaries -= r._1
          replicators -= r._2
          context.stop(r._2)    // stop replicator
        }
      })
  }

  /* TODO Behavior for the replica role. */
  var expectedSeq = 0L

  override def preStart(): Unit = {}
  override def postStop(): Unit = {}

  var replicaSenders = Map.empty[Long, ActorRef]
  
  val replica: Receive = {
    
    case Get(key, id) =>
      println(s"replica -> Get($key,$id) <- $sender")
      sender ! GetResult(key, kv.get(key), id)
    
    case Snapshot(key, valueOption, seq) =>
      println(s"Snapshot($key, $valueOption, $seq)")
      if (seq < expectedSeq) {
        replicaSenders += (seq -> sender)
        persistor ! Persist(key, valueOption, seq)
        
//      sender !  SnapshotAck(key, seq)
      } else if (seq == expectedSeq) {
        replicaSenders += (seq -> sender)
        persistor ! Persist(key, valueOption, seq)
        valueOption match {
          case Some(value) => kv += (key -> value)
          case None        => kv -= key
        }
        sender ! SnapshotAck(key, seq)
        expectedSeq = seq + 1
      }
    
    case Persisted(key, id) =>
      println(s"Persisted($key, $id)")
      val req = replicaSenders(id)
      req ! SnapshotAck(key, id)
      replicaSenders -= id
  }
}
