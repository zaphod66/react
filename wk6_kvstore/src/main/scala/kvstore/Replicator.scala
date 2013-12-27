package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.language.postfixOps

object Replicator {
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)
  
  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  case class Retry(seq: Long)
  case class Timeout(seq: Long)
  
  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}

class Replicator(val replica: ActorRef) extends Actor {
  import Replicator._
  import Replica._
  import context.dispatcher
  
  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate)]
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]
  
  var _seqCounter = 0L
  def nextSeq = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }
  
  /* TODO Behavior for the Replicator. */
  def receive: Receive = {
    case Replicate(key, valueOption, id) =>
//      println(s"Replicate($key, $valueOption, $id)")
      val actSeq = nextSeq
      acks += (actSeq -> (sender, Replicate(key, valueOption, id)))
      replica ! Snapshot(key, valueOption, actSeq)
      context.system.scheduler.scheduleOnce(1 second, self, Timeout(actSeq))
      context.system.scheduler.scheduleOnce(100 millis, self, Retry(actSeq))
    case SnapshotAck(key, seq) =>
//      println(s"SnapshotAck($key,$seq)")
      val (s, r) = acks(seq)
      acks -= seq
      s ! Replicated(key, seq)
    case Retry(seq) =>
//      println(s"Retry($seq) retrying = $retrying")
      if (acks.contains(seq)) {
        val (s, r) = acks(seq)
//        println(s" -> Snapshot($r)")
        replica ! Snapshot(r.key, r.valueOption, r.id)
        context.system.scheduler.scheduleOnce(100 millis, self, Retry(r.id))
      }
    case Timeout(seq) =>
//      println(s"Timeout($seq)")
      acks -= seq
  }

}
