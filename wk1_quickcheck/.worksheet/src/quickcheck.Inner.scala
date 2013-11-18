package quickcheck

import org.scalacheck._
import Arbitrary._
import Gen._

import week1._

trait Tree
case class Inner(left: Tree, right: Tree) extends Tree
case class Leaf(x: Int) extends Tree

object check {
  println("Welcome to the Scala worksheet")
  lazy val ints = arbitrary[Int]
  ints.sample
  
  val integers = new Generator[Int] {
    def generate = scala.util.Random.nextInt()
  }
  val booleans = integers map { x => x > 0 }

  val pairs = for {
    x <- integers
    y <- integers
  } yield (x,y)
  
  integers.generate
  booleans.generate
  pairs.generate
}
