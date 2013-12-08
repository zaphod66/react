package simulations

import math.random

class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  protected[simulations] object SimConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8

    val prevalenceRate: Double = 0.01
    val transmissibilityRate: Double = 0.4
    // to complete: additional parameters of simulation
  }

  import SimConfig._

  val numInitiallySick = (prevalenceRate * population).toInt
  val numHealthy = (population - numInitiallySick).toInt

  val persons: List[Person] =
    List.fill(numInitiallySick)(new Person(0) { infect() }) ++
    List.fill(numHealthy)(new Person(0))

  def findPeopleByRowCol(r: Int, c: Int): List[Person] = {
    val tr = (r + roomRows   ) % roomRows
    val tc = (c + roomColumns) % roomColumns
    persons filter ((p) => (tr, tc) == (p.row, p.col))
  }
  
  def deadOrSick(p: Person) = p.dead || p.sick

  class Person (val id: Int) {
    var infected = false
    var sick = false
    var immune = false
    var dead = false

    // demonstrates random number generation
    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    def moveNextRoom() {
      val nextCandidates: List[(Int, Int)] =
        List((1, 0), (0, 1), (-1, 0), (0, -1))
          .filter { case (r, c) =>
            !(findPeopleByRowCol(r + row, c + col).exists(deadOrSick)) }

      if(!nextCandidates.isEmpty) {
        val (dx, dy) = nextCandidates(randomBelow(nextCandidates.size))
        row = (row + roomRows + dx) % roomRows
        col = (col + roomColumns + dy) % roomColumns
      }
    }

    def infectInRoom() {
      val peopleInCurrentRoom = findPeopleByRowCol(row, col)
      if (peopleInCurrentRoom.exists((p) => p.infected) && !infected) {
        if (random < transmissibilityRate) infect()
      }
    }

    def setupNextMovement() {
      afterDelay(1 + randomBelow(4)) {
        if(!dead) {
          moveNextRoom()
          if(!infected && !immune) {
            infectInRoom()
          }
          setupNextMovement()
        }
      }
    }

    def infect() {
      infected = true
      afterDelay(6) { sick = true }
      afterDelay(14) { if (randomBelow(4) == 0) { dead = true } }
      afterDelay(16) { if(!dead) { sick = false ; immune = true } }
      afterDelay(18) {
        if(!dead && immune) {
          immune = false
          infected = false
        }
      }
    }


    setupNextMovement()
  }
}

// ======================================================================================
// ======================================================================================
// ======================================================================================

//package simulations
//
//import math.random
//
//class EpidemySimulator extends Simulator {
//
//  def randomBelow(i: Int) = (random * i).toInt
//
//  protected[simulations] object SimConfig {
//    val population: Int = 300
//    val roomRows: Int = 8
//    val roomColumns: Int = 8
//
//    // to complete: additional parameters of simulation
//    val prevalenceRate: Double = 0.01
//    val transRate = 0.4
//    val dieRate = 0.25
//    val prevalenceNum: Int = (population * prevalenceRate).toInt
//    
//    val incubationTime = 6
//    val dieTime = 14
//    val immuneTime = 16
//    val healTime = 18
//  }
//
//  import SimConfig._
//
//  val persons: List[Person] = constructPersons(300, 0, Nil)
//  
//  def constructPersons(num: Int, numInfect: Int, pers: List[Person]): List[Person] = {
//    if (num == 0) {
//      pers
//    } else {
//      var actNum = numInfect
//      var p = new Person(num)
//      val prevalence = randomBelow(100) <= 100
//      if (prevalence && actNum < prevalenceNum) {
//        p.infectedAction
//        actNum += 1
//      }
//      p.action()
//      constructPersons(num - 1, actNum, p :: pers)
//    }
//  }
//  
//  class Person (val id: Int) {
//    var infected = false
//    var sick = false
//    var immune = false
//    var dead = false
//
//    // demonstrates random number generation
//    var row: Int = randomBelow(roomRows)
//    var col: Int = randomBelow(roomColumns)
//
//    //
//    // to complete with simulation logic
//    //
//    
//    def action() {
//      if (!dead) {
//          moveAction
//      }
//    }
//    
//    def moveAction() {
//      val moveDirection = randomBelow(3)
//      val moveDelay = randomBelow(5)
//      afterDelay(moveDelay + 1){ move(moveDirection) }
//    }
//    
//    def infectedAction() {
//      infected = true
//      afterDelay(incubationTime){ this.sick = true }
//      val personDies = (randomBelow(100) <= 100 * dieRate)
//      if (personDies) {
//        afterDelay(dieTime){ this.infected = true; this.sick = true; this.immune = false; this.dead = true }
//      } else {
//        if (!dead) {
//          afterDelay(immuneTime){ this.infected = true;  this.sick = false; this.immune = true;  this.dead = false }
//          afterDelay(healTime){   this.infected = false; this.sick = false; this.immune = false; this.dead = false }
//        }
//      }      
//    }
//    
//    def move(moveDirection: Int) {
//      var nextRow = row
//      var nextCol = col
//      
//      var healthRooms = List[(Int,Int)]()
//      
//      // up
//      nextRow = decRow(row)
//      nextCol = col
//      if (!isRoomSick(nextRow,nextCol)) healthRooms = (nextRow,nextCol) :: healthRooms
//      
//      // right
//      nextRow = row
//      nextCol = incCol(col)
//      if (!isRoomSick(nextRow,nextCol)) healthRooms = (nextRow,nextCol) :: healthRooms
//      
//      // down
//      nextRow = incRow(row)
//      nextCol = col
//      if (!isRoomSick(nextRow,nextCol)) healthRooms = (nextRow,nextCol) :: healthRooms
//      
//      // left
//      nextRow = row
//      nextCol = decCol(col)
//      if (!isRoomSick(nextRow,nextCol)) healthRooms = (nextRow,nextCol) :: healthRooms
//
//      if (!healthRooms.isEmpty) {
//        val r = randomBelow(healthRooms.size)
//        val d = healthRooms(r)
//        
//        row = d._1
//        col = d._2
//        
//          if (isRoomInfected(row, col)) {
//            val transmission = randomBelow(100) < 100 * transRate
//            if (transmission) {
//              infectedAction
//            }
//          }
//      }
//      
//      action
//    }
//    
//    def isRoomInfected(row:Int, col: Int): Boolean = {
//      val infectedPersons = for {
//        p <- persons
//        if (p.infected && p.row == row && p.col == col)
//      } yield p
//      
//      !infectedPersons.isEmpty
//    }
//    
//    def isRoomSick(row:Int, col: Int): Boolean = {
//      val sickPersons = for {
//        p <- persons
//        if (p.sick && p.row == row && p.col == col)
//      } yield p
//      
//      !sickPersons.isEmpty
//    }
//    
//    def decRow(row: Int): Int = {
//      var nextRow = (row - 1)
//      if (nextRow < 0) nextRow = roomRows - 1
//
//      nextRow
//    }
//    
//    def incRow(row: Int): Int = {
//      (row + 1) % roomRows
//    }
//    
//    def decCol(col: Int): Int = {
//      var nextCol = col - 1
//      if (nextCol < 0) nextCol = roomColumns - 1
//
//      nextCol
//    }
//    
//    def incCol(col: Int): Int = {
//      (col + 1) % roomColumns
//    }
//    
//    override def toString(): String = {
//      "Person: " + id + "infected : $infected, sick: $sick, immune: $immune, dead: $dead"
//    }
//  }
//}
