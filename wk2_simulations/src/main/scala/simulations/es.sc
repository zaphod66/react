package simulations


object es {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val es = new EpidemySimulator                   //> es  : simulations.EpidemySimulator = simulations.EpidemySimulator@4144e4e0
  val p  = es.persons.head                        //> p  : simulations.es.es.Person = simulations.EpidemySimulator$Person@120e4f9a
                                                  //| 
  p.infected                                      //> res0: Boolean = false
  p.sick                                          //> res1: Boolean = false
  p.dead                                          //> res2: Boolean = false
  p.immune                                        //> res3: Boolean = false

  es.randomBelow(4)                               //> res4: Int = 3
  es.randomBelow(4)                               //> res5: Int = 0
  es.randomBelow(4)                               //> res6: Int = 1
  es.randomBelow(4)                               //> res7: Int = 3
  es.randomBelow(4)                               //> res8: Int = 3
  es.randomBelow(4)                               //> res9: Int = 1
  es.randomBelow(4)                               //> res10: Int = 2
  es.randomBelow(4)                               //> res11: Int = 2
  es.randomBelow(4)                               //> res12: Int = 2
}