package simulations

import simulations.EpidemySimulator._

object viktor {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(119); 
  println("Welcome to the Scala worksheet");$skip(26); 
  
  val l1 = List(1,2,3);System.out.println("""l1  : List[Int] = """ + $show(l1 ));$skip(30); 
  val l2 = l1.map(x => x * x);System.out.println("""l2  : List[Int] = """ + $show(l2 ));$skip(26); 
  val ra = randomBelow(4);System.out.println("""ra  : <error> = """ + $show(ra ))}
  
}
