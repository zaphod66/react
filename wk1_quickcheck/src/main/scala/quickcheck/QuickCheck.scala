package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {
  
  // if you insert an element into an empty heap, then find the minimum of the resulting heap, you get the element back
  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  // if you insert an element into an empty heap, then remove the minimum you get an empty heap
  property("min2") = forAll { a: Int =>
  val h1 = insert(a, empty)
  val h2 = deleteMin(h1)
  isEmpty(h2)
  }
  
  // If you insert any two elements into an empty heap, finding the minimum of the resulting heap should get the smallest of the two elements back.
  property("min3") = forAll { a: Int =>
    forAll { b: Int =>
      val m = if (a < b) a else b
      findMin(insert(a, insert(b, empty))) == m
    }
  }
  
  property("min3.1") = forAll { a: Int =>
    forAll { b: Int =>
      val m  = if (a < b) a else b
      val h1 = insert(a, insert(b, empty))
      val h2 = insert(b, insert(a, empty))
      val m1 = findMin(h1)
      val m2 = findMin(h2)
      
      (m == m1) && (m1 == m2)
    }
  }
  
  // If you insert an element into an empty heap, then delete the minimum, the resulting heap should be empty.
  property("min4") = forAll { a: Int =>
    val h = insert(a, empty)
    deleteMin(h) == empty
  }
  
  property("min5") = forAll { a: Int =>
    forAll { b: Int =>
      val h1 = insert(a, insert(b, empty))
      val h2 = insert(b, insert(a, empty))
      findMin(h1) == findMin(h2)
    }
  }
  
  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }
  
  // Finding a minimum of the melding of any two heaps should return a minimum of one or the other.
  property("gen2") = forAll { (h1: H) =>
    forAll{ (h2: H) =>
      val m1 = findMin(h1)
      val m2 = findMin(h2)
      val m  = if (m1 < m2) m1 else m2
      val h3 = meld(h1, h2)
      val m3 = findMin(h3)
      m == m3
    }
  }

  // Given any heap, you should get a sorted sequence of elements when continually finding and deleting minima.
  property("gen3") = forAll { (h: H) =>
    checkSorted(h)
  }

  def checkSorted(h: H): Boolean = {
    val l1 = getSortedList(h, Nil)
    val l2 = l1.sorted
    l1.equals(l2)
  }
  
  def getSortedList(h: H, l:List[Int]): List[Int] = {
    if (isEmpty(h)) l
    else {
      val m = findMin(h)
      val t = deleteMin(h)
      getSortedList(t, l :+ m)
    }
  }
  
  property("gen4") = forAll { (h: H) =>
    val m = findMin(h)
    val h2 = deleteMin(h)
    val h3 = insert(m,h2)
    
    val l1 = getSortedList(h, Nil)
    val l2 = getSortedList(h3, Nil)
    l1.equals(l2)
  }
  
  property("gen5") = forAll { a: Int =>
    forAll { (h: H) =>
      val m  = findMin(h)
      val m1 = if (a < m) a else m
      val h2 = insert(a, h)
      val m2 = findMin(h2)
      m1 == m2
    }
  }
  
  // for any heap, get their elements reinsert to an empty heap should give the same contents
  property("gen6") = forAll { (h: H) =>
    val l1 = getSortedList(h, Nil)
    val h2 = insertList(l1, empty)
    val l2 = getSortedList(h2, Nil)
    l1.equals(l2)
  }
  
  def insertList(l: List[Int], h: H): H = {
    if (l.isEmpty) h
    else {
      insertList(l.tail, insert(l.head, h))
    }
  }
  
  lazy val genHeap: Gen[H] = for {
    x <- arbitrary[Int]
    h <- oneOf(value(empty), genHeap)
  } yield insert(x, h)
  
  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)
}
