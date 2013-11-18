package simulations


object es {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(77); 
  println("Welcome to the Scala worksheet");$skip(32); 
  val es = new EpidemySimulator;System.out.println("""es  : simulations.EpidemySimulator = """ + $show(es ));$skip(27); 
  val p  = es.persons.head;System.out.println("""p  : simulations.es.es.Person = """ + $show(p ));$skip(13); val res$0 = 
  p.infected;System.out.println("""res0: Boolean = """ + $show(res$0));$skip(9); val res$1 = 
  p.sick;System.out.println("""res1: Boolean = """ + $show(res$1));$skip(9); val res$2 = 
  p.dead;System.out.println("""res2: Boolean = """ + $show(res$2));$skip(11); val res$3 = 
  p.immune;System.out.println("""res3: Boolean = """ + $show(res$3));$skip(21); val res$4 = 

  es.randomBelow(4);System.out.println("""res4: Int = """ + $show(res$4));$skip(20); val res$5 = 
  es.randomBelow(4);System.out.println("""res5: Int = """ + $show(res$5));$skip(20); val res$6 = 
  es.randomBelow(4);System.out.println("""res6: Int = """ + $show(res$6));$skip(20); val res$7 = 
  es.randomBelow(4);System.out.println("""res7: Int = """ + $show(res$7));$skip(20); val res$8 = 
  es.randomBelow(4);System.out.println("""res8: Int = """ + $show(res$8));$skip(20); val res$9 = 
  es.randomBelow(4);System.out.println("""res9: Int = """ + $show(res$9));$skip(20); val res$10 = 
  es.randomBelow(4);System.out.println("""res10: Int = """ + $show(res$10));$skip(20); val res$11 = 
  es.randomBelow(4);System.out.println("""res11: Int = """ + $show(res$11));$skip(20); val res$12 = 
  es.randomBelow(4);System.out.println("""res12: Int = """ + $show(res$12))}
}
