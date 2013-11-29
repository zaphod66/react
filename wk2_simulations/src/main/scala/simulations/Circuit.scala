package simulations

import common._

class Wire {
  private var sigVal = false
  private var actions: List[Simulator#Action] = List()

  def getSignal: Boolean = sigVal
  
  def setSignal(s: Boolean) {
    if (s != sigVal) {
      sigVal = s
      actions.foreach(action => action())
    }
  }

  def addAction(a: Simulator#Action) {
    actions = a :: actions
    a()
  }
  
  override def toString() = "Wire(" + sigVal + ")"  
}

abstract class CircuitSimulator extends Simulator {

  val InverterDelay: Int
  val AndGateDelay: Int
  val OrGateDelay: Int

  def probe(name: String, wire: Wire) {
    wire addAction {
      () => afterDelay(0) {
        println(
          "  " + currentTime + ": " + name + " -> " +  wire.getSignal)
      }
    }
  }

  def inverter(input: Wire, output: Wire) {
    def invertAction() {
      val inputSig = input.getSignal
      afterDelay(InverterDelay) { output.setSignal(!inputSig) }
    }
    input addAction invertAction
  }

  def andGate(a1: Wire, a2: Wire, output: Wire) {
    def andAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(AndGateDelay) { output.setSignal(a1Sig & a2Sig) }
    }
    a1 addAction andAction
    a2 addAction andAction
  }

  //
  // to complete with orGates and demux...
  //

  def orGate(a1: Wire, a2: Wire, output: Wire) {
    def orAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(OrGateDelay) { output.setSignal(a1Sig | a2Sig) }
    }

    a1 addAction orAction
    a2 addAction orAction
  }
  
  def orGate2(a1: Wire, a2: Wire, output: Wire) {
    def orAction() {
      val a1Inv = new Wire
      val a2Inv = new Wire
      val orVal = new Wire
      inverter(a1, a1Inv)
      inverter(a2, a2Inv)
      andGate(a1Inv, a2Inv, orVal)
      inverter(orVal, output)
    }
    
    a1 addAction orAction
    a2 addAction orAction
  }

  def demux(in: Wire, c: List[Wire], out: List[Wire]) {
    c match {
      case Nil => andGate(in, in, out(0))
      case x::xs => {
        // Refer to the diagram
        val inL, inR, notX = new Wire
        
        andGate(in, x, inL)
        inverter(x, notX)
        andGate(in, notX, inR)

        val n = out.length / 2
        demux(inL, xs, out take n)
        demux(inR, xs, out drop n)
      }
    }
  }
  
//  def calcIdx(c: List[Wire], idx: Int): Int = {
//    if (c.isEmpty) {
//      idx
//    } else {
//      val iSig = c.head.getSignal
//      if (iSig) {
//        calcIdx(c.tail, 2 * idx + 1)
//      } else {
//        calcIdx(c.tail, 2 * idx + 0)
//      }      
//    }
//  }
//
//  def demux(in: Wire, c: List[Wire], out: List[Wire]) {
//    def demuxAction() {
//      val inSig = in.getSignal
//      val idx = calcIdx(c, 0)
//      val siz = out.size - 1
//      for (i <- 0 to siz) {
//        if (i == idx) {
//          afterDelay(0) { out(siz - i).setSignal(inSig) }
//        } else {
//          afterDelay(0) { out(siz - i).setSignal(false) }
//        }
//      }
//    }
//    
//    in addAction demuxAction
//    c  foreach {_ addAction demuxAction}
//  }
}

object Circuit extends CircuitSimulator {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5

  def andGateExample {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    probe("in1", in1)
    probe("in2", in2)
    probe("out", out)
    in1.setSignal(false)
    in2.setSignal(false)
    run

    in1.setSignal(true)
    run

    in2.setSignal(true)
    run
  }

  def demuxGateExample {
    val in  = new Wire
    val c   = List(new Wire, new Wire)
    val out = List(new Wire, new Wire, new Wire, new Wire)
    
    demux(in, c, out)
    probe("out(0)", out(0))
    probe("out(1)", out(1))
    probe("out(2)", out(2))
    probe("out(3)", out(3))
    in.setSignal(true)

    println("1:0 ============")
    c(0).setSignal(false)
    c(1).setSignal(false)
    run
    printOut(out)
    
    println("1:3 ============")
    c(0).setSignal(true)
    c(1).setSignal(true)
    run
    printOut(out)
    
    println("0:3 ============")
    in.setSignal(false)
    run
    printOut(out)

    println("1:3 ============")
    in.setSignal(true)
    run
    printOut(out)
  }
  //
  // to complete with orGateExample and demuxExample...
  //

  def printOut(out: List[Wire]): Unit = {
    println("---------")
    println("out(0)" + out(0).getSignal)
    println("out(1)" + out(1).getSignal)
    println("out(2)" + out(2).getSignal)
    println("out(3)" + out(3).getSignal)
    println("---------")  
  }
}

object CircuitMain extends App {
  // You can write tests either here, or better in the test class CircuitSuite.
//  Circuit.andGateExample
  Circuit.demuxGateExample
}
