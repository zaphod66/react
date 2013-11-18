package s

import simulations._

object viktor {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(92); 
  println("Welcome to the Scala worksheet");$skip(26); 
  
  val l1 = List(1,2,3);System.out.println("""l1  : List[Int] = """ + $show(l1 ));$skip(30); 
  val l2 = l1.map(x => x * x);System.out.println("""l2  : List[Int] = """ + $show(l2 ));$skip(33); 

  val es = new EpidemySimulator;System.out.println("""es  : simulations.EpidemySimulator = """ + $show(es ))}
}
