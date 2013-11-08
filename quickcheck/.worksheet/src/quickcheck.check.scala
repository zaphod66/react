package quickcheck

import org.scalacheck._

import Arbitrary._
import Gen._
import Prop._

import week1._

object check {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(166); 
  println("Welcome to the Scala worksheet");$skip(33); 
  lazy val ints = arbitrary[Int];System.out.println("""ints: => org.scalacheck.Gen[Int]""");$skip(14); val res$0 = 
  ints.sample;System.out.println("""res0: Option[Int] = """ + $show(res$0));$skip(39); 

  lazy val bools = arbitrary[Boolean];System.out.println("""bools: => org.scalacheck.Gen[Boolean]""");$skip(15); val res$1 = 
  bools.sample;System.out.println("""res1: Option[Boolean] = """ + $show(res$1));$skip(172); 
  
  lazy val genMap: Gen[Map[Int,Int]] = for {
    k <- arbitrary[Int]
    v <- arbitrary[Int]
    m <- oneOf(value(Map.empty[Int,Int]), genMap)
  } yield m.updated(k, v);System.out.println("""genMap: => org.scalacheck.Gen[Map[Int,Int]]""");$skip(17); val res$2 = 

  genMap.sample;System.out.println("""res2: Option[Map[Int,Int]] = """ + $show(res$2));$skip(92); 
  
  def integers = new Generator[Int] {
    def generate = scala.util.Random.nextInt()
  };System.out.println("""integers: => week1.Generator[Int]""");$skip(94); 
  def choose(lo: Int,hi: Int):Generator[Int] = for ( x <- integers ) yield lo + x % (hi - lo);System.out.println("""choose: (lo: Int, hi: Int)week1.Generator[Int]""");$skip(51); 
  def booleans = for ( x <- integers ) yield x > 0;System.out.println("""booleans: => week1.Generator[Boolean]""");$skip(72); 
  def pairs = for {
    x <- integers
    y <- integers
  } yield (x,y);System.out.println("""pairs: => week1.Generator[(Int, Int)]""");$skip(79); 
  
  def leafs: Generator[Leaf] = for {
    x <- choose(0,9)
  } yield Leaf(x);System.out.println("""leafs: => week1.Generator[week1.Leaf]""");$skip(93); 
  
  def inners: Generator[Inner] = for {
    l <- trees
    r <- trees
  } yield Inner(l,r);System.out.println("""inners: => week1.Generator[week1.Inner]""");$skip(120); 
  
  def trees: Generator[Tree] = for {
    isLeaf <- booleans
    tree <- if (isLeaf) leafs else inners
  } yield tree;System.out.println("""trees: => week1.Generator[week1.Tree]""");$skip(23); val res$3 = 
  
  integers.generate;System.out.println("""res3: Int = """ + $show(res$3));$skip(20); val res$4 = 
  booleans.generate;System.out.println("""res4: Boolean = """ + $show(res$4));$skip(17); val res$5 = 
  pairs.generate;System.out.println("""res5: (Int, Int) = """ + $show(res$5));$skip(17); val res$6 = 
  trees.generate;System.out.println("""res6: week1.Tree = """ + $show(res$6))}
}
