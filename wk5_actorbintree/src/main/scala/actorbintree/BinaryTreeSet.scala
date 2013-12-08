/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import akka.actor._
import scala.collection.immutable.Queue

object BinaryTreeSet {

  trait Operation {
    def requester: ActorRef
    def id: Int
    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /** Request with identifier `id` to insert an element `elem` into the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to check whether an element `elem` is present
    * in the tree. The actor at reference `requester` should be notified when
    * this operation is completed.
    */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to remove the element `elem` from the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection*/
  case object GC

  /** Holds the answer to the Contains request with identifier `id`.
    * `result` is true if and only if the element is present in the tree.
    */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply
  
  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}


class BinaryTreeSet extends Actor {
  import BinaryTreeSet._
  import BinaryTreeNode._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = normal

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = {
    case Insert(requester, id, elem)   => root ! Insert(requester, id, elem)
    case Contains(requester, id, elem) => root ! Contains(requester, id, elem)
    case Remove(requester, id, elem)   => root ! Remove(requester, id, elem)
    case GC => {
      val newRoot = createRoot
      context.become(garbageCollecting(newRoot))
      root ! CopyTo(newRoot)
    }
  }

  // optional
  /** Handles messages while garbage collection is performed.
    * `newRoot` is the root of the new binary tree where we want to copy
    * all non-removed elements into.
    */
  def garbageCollecting(newRoot: ActorRef): Receive = {
    case msg: Operation => pendingQueue = pendingQueue.enqueue(msg)
  
    case GC => ()
    
    case CopyFinished => {
      root ! PoisonPill
      pendingQueue.foreach { newRoot ! _ }
      pendingQueue = Queue.empty[Operation]
      root = newRoot
      context.unbecome()
    }
  }  
}

object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode],  elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  def copyOpId = -1
  
  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = {
    case Insert(requester, id, e) =>
      {
        if (e == elem) {
          removed = false;
          requester ! OperationFinished(id)
        } else {
          val nextPos = nextBranch(e: Int)  // Left or Right
          if (subtrees.contains(nextPos)) {
            subtrees(nextPos) ! Insert(requester, id, e)
          } else {
            val newAct = context.actorOf(props(e, initiallyRemoved = false))
            subtrees = subtrees.updated(nextPos, newAct)
            requester ! OperationFinished(id)
          }
        }
      }
    case Contains(requester, id, e) =>
      {
        if (e == elem) {
          requester ! ContainsResult(id, !removed)
        } else {
          val nextPos = nextBranch(e: Int)  // Left or Right
          if (subtrees.contains(nextPos)) {
            subtrees(nextPos) ! Contains(requester, id, e)
          } else {
            requester ! ContainsResult(id, result = false)
          }
        }
      }
    case Remove(requester, id, e) =>
      {
        if (e == elem) {
          removed = true
          requester ! OperationFinished(id)
        } else {
           val nextPos = nextBranch(e: Int)  // Left or Right
          if (subtrees.contains(nextPos)) {
            subtrees(nextPos) ! Remove(requester, id, e)
          } else {
            requester ! OperationFinished(id)
          }
        }        
      }
    case CopyTo(newRoot) =>
      {
        val children = subtrees.values
        
        if (!removed) {
          newRoot ! Insert(self, copyOpId, elem)
          context.become(copying(children.toSet, false))
        } else if (children.isEmpty) {
          context.parent ! CopyFinished
        } else {
          context.become(copying(children.toSet, true))
        }

        children.foreach { _ ! CopyTo(newRoot) }        
      }
    case _ => ???
  }

  def nextBranch(e: Int) = if (e < elem) Left else Right
  
  // optional
  /** `expected` is the set of ActorRefs whose replies we are waiting for,
    * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
    */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = {
    case OperationFinished(copyOpId) =>
      checkFinished(expected, true)
    case CopyFinished =>
      checkFinished(expected - sender, insertConfirmed)    
  }

  def checkFinished(expected: Set[ActorRef], insertConfirmed: Boolean): Unit = {
    if(expected.isEmpty && insertConfirmed) {
      context.unbecome()
      context.parent ! CopyFinished
      subtrees.values.foreach { _ ! PoisonPill }
    } else {
      context.become(copying(expected, insertConfirmed))
    }
  }    
}
