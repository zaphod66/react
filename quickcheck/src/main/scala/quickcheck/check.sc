package quickcheck

import org.scalacheck._

import Arbitrary._
import Gen._
import Prop._

import week1._

object check {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  lazy val ints = arbitrary[Int]                  //> ints: => org.scalacheck.Gen[Int]
  ints.sample                                     //> res0: Option[Int] = Some(1112335521)

  lazy val bools = arbitrary[Boolean]             //> bools: => org.scalacheck.Gen[Boolean]
  bools.sample                                    //> res1: Option[Boolean] = Some(true)
  
  lazy val genMap: Gen[Map[Int,Int]] = for {
    k <- arbitrary[Int]
    v <- arbitrary[Int]
    m <- oneOf(value(Map.empty[Int,Int]), genMap)
  } yield m.updated(k, v)                         //> genMap: => org.scalacheck.Gen[Map[Int,Int]]

  genMap.sample                                   //> res2: Option[Map[Int,Int]] = Some(Map(-1 -> 2147483647))
  
  def integers = new Generator[Int] {
    def generate = scala.util.Random.nextInt()
  }                                               //> integers: => week1.Generator[Int]
  def choose(lo: Int,hi: Int):Generator[Int] = for ( x <- integers ) yield lo + x % (hi - lo)
                                                  //> choose: (lo: Int, hi: Int)week1.Generator[Int]
  def booleans = for ( x <- integers ) yield x > 0//> booleans: => week1.Generator[Boolean]
  def pairs = for {
    x <- integers
    y <- integers
  } yield (x,y)                                   //> pairs: => week1.Generator[(Int, Int)]
  
  def leafs: Generator[Leaf] = for {
    x <- choose(0,9)
  } yield Leaf(x)                                 //> leafs: => week1.Generator[week1.Leaf]
  
  def inners: Generator[Inner] = for {
    l <- trees
    r <- trees
  } yield Inner(l,r)                              //> inners: => week1.Generator[week1.Inner]
  
  def trees: Generator[Tree] = for {
    isLeaf <- booleans
    tree <- if (isLeaf) leafs else inners
  } yield tree                                    //> trees: => week1.Generator[week1.Tree]
  
  integers.generate                               //> res3: Int = -564585662
  booleans.generate                               //> res4: Boolean = true
  pairs.generate                                  //> res5: (Int, Int) = (2072838527,-2081857597)
  trees.generate                                  //> res6: week1.Tree = Leaf(1)
}