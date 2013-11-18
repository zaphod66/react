package simulations

import math.random

class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  protected[simulations] object SimConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8

    // to complete: additional parameters of simulation
    val prevalenceRate: Double = 0.01
    val transRate = 0.4
    val dieRate = 0.25
    val prevalenceNum: Int = (population * prevalenceRate).toInt
    
    val incubationTime = 6
    val dieTime = 14
    val immuneTime = 16
    val healTime = 18
  }

  import SimConfig._

  val persons: List[Person] = constructPersons(300, 0, Nil)
  
  def constructPersons(num: Int, numInfect: Int, pers: List[Person]): List[Person] = {
    if (num == 0) {
      pers
    } else {
      var actNum = numInfect
      var p = new Person(num)
      val prevalence = randomBelow(100) <= 100
      if (prevalence && actNum < prevalenceNum) {
        p.infectedAction
        actNum += 1
      }
      p.action()
      constructPersons(num - 1, actNum, p :: pers)
    }
  }
  
  class Person (val id: Int) {
    var infected = false
    var sick = false
    var immune = false
    var dead = false

    // demonstrates random number generation
    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    //
    // to complete with simulation logic
    //
    
    def action() {
      if (!dead) {
          moveAction
      }
    }
    
    def moveAction() {
      val moveDirection = randomBelow(3)
      val moveDelay = randomBelow(5)
      afterDelay(moveDelay){ move(moveDirection) }
    }
    
    def infectedAction() {
      infected = true
      afterDelay(incubationTime){ this.sick = true }
      val personDies = (randomBelow(100) <= 100 * dieRate)
      if (personDies) {
        afterDelay(dieTime){ this.infected = true; this.sick = true; this.immune = false; this.dead = true }
      } else {
        if (!dead) {
          afterDelay(immuneTime){ this.infected = true;  this.sick = false; this.immune = true;  this.dead = false }
          afterDelay(healTime){   this.infected = false; this.sick = false; this.immune = false; this.dead = false }
        }
      }      
    }
    
    def move(moveDirection: Int) {
      var nextRow = row
      var nextCol = col
      
      if (!dead) {
          if (moveDirection == 0) {
            nextRow = ( row - 1 ) % roomRows    // up
            nextCol = col
          } else if (moveDirection == 1) {
            nextRow = row                       // right
            nextCol = ( col + 1 ) % roomColumns
          } else if (moveDirection == 2) {
            nextRow = ( row + 1 ) % roomRows    // down
            nextCol = col
          } else if (moveDirection == 3) {
            nextRow = row                       // left
            nextCol = ( col - 1 ) % roomColumns        
          }
         
          if (nextRow < 0) nextRow = roomRows - 1
          if (nextCol < 0) nextCol = roomColumns -1
    
          if (!isRoomSick(nextRow, nextCol)) {
            row = nextRow
            col = nextCol
          }
          
          if (isRoomInfected(row, col)) {
            val transmission = randomBelow(100) <= 100 * transRate
            if (transmission) {
              infectedAction
            }
          }
          
          action
      }
    }
    
    def isRoomInfected(row:Int, col: Int): Boolean = {
      val infectedPersons = for {
        p <- persons
        if (p.infected && p.row == row && p.col == col)
      } yield p
      
      !infectedPersons.isEmpty
    }
    
    def isRoomSick(row:Int, col: Int): Boolean = {
      val sickPersons = for {
        p <- persons
        if (p.sick && p.row == row && p.col == col)
      } yield p
      
      !sickPersons.isEmpty
    }
    
    override def toString(): String = {
      "Person: " + id + "infected : $infected, sick: $sick, immune: $immune, dead: $dead"
    }
  }
}
