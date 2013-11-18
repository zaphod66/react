package simulations

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CircuitSuite extends CircuitSimulator with FunSuite {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5
  
  test("andGate example") {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    in1.setSignal(false)
    in2.setSignal(false)
    run
    
    assert(out.getSignal === false, "and 1")

    in1.setSignal(true)
    run
    
    assert(out.getSignal === false, "and 2")

    in2.setSignal(true)
    run
    
    assert(out.getSignal === true, "and 3")
  }

  //
  // to complete with tests for orGate, demux, ...
  //

  test("orGate example") {
    val in1, in2, out = new Wire
    orGate(in1, in2, out)
    
    in1.setSignal(false)
    in2.setSignal(false)
    run
    
    assert(out.getSignal === false, "or 1")
    
    in1.setSignal(true)
    run
    
    assert(out.getSignal === true, "or 2")
    
    in2.setSignal(true)
    run
    
    assert(out.getSignal === true, "or 3")
  }

  test("orGate2 example") {
    val in1, in2, out = new Wire
    orGate2(in1, in2, out)
    
    in1.setSignal(false)
    in2.setSignal(false)
    run
    
    assert(out.getSignal === false, "or2 1")
    
    in1.setSignal(true)
    run
    
    assert(out.getSignal === true, "or2 2")
    
    in2.setSignal(true)
    run
    
    assert(out.getSignal === true, "or2 3")
  }

  test("demux test 0 c") {
    val in  = new Wire
    val c   = List[Wire]()
    val out = List(new Wire)
    
    demux(in,c,out)
    
    in.setSignal(true)
    run
    
    assert(out.head.getSignal === in.getSignal, "demux 0 0.0")

    in.setSignal(false)
    run
    
    assert(out.head.getSignal === in.getSignal, "demux 0 0.1")
  }
  
  test("demux test 1 c") {
    val in  = new Wire
    val c   = List(new Wire)
    val out = List(new Wire, new Wire)
    
    demux(in, c, out)
    
    in.setSignal(true)
    
    c(0).setSignal(false)
    run
    
    assert(out(0).getSignal === in.getSignal, "demux #c = 1 0.0")
    assert(out(1).getSignal === false,        "demux #c = 1 0.1")

    c(0).setSignal(true)
    run
    
    assert(out(0).getSignal === false,        "demux #c = 1 1.0")
    assert(out(1).getSignal === in.getSignal, "demux #c = 1 1.1")
  }
  
  test("demux test 2 c") {
    val in  = new Wire
    val c   = List(new Wire, new Wire)
    val out = List(new Wire, new Wire, new Wire, new Wire)
    
    demux(in, c, out)
    
    in.setSignal(true)
    
    // 0
    c(0).setSignal(false)
    c(1).setSignal(false)
    run
    
    assert(out(0).getSignal === in.getSignal, "demux #c = 2 0.0")
    assert(out(1).getSignal === false,        "demux #c = 2 0.1")
    assert(out(2).getSignal === false,        "demux #c = 2 0.2")
    assert(out(3).getSignal === false,        "demux #c = 2 0.3")

    // 1
    c(0).setSignal(false)
    c(1).setSignal(true)
    run
    
    assert(out(0).getSignal === false,        "demux #c = 2 1.0")
    assert(out(1).getSignal === in.getSignal, "demux #c = 2 1.1")
    assert(out(2).getSignal === false,        "demux #c = 2 1.2")
    assert(out(3).getSignal === false,        "demux #c = 2 1.3")

    // 2
    c(0).setSignal(true)
    c(1).setSignal(false)
    run
    
    assert(out(0).getSignal === false,        "demux #c = 2 2.0")
    assert(out(1).getSignal === false,        "demux #c = 2 2.1")
    assert(out(2).getSignal === in.getSignal, "demux #c = 2 2.2")
    assert(out(3).getSignal === false,        "demux #c = 2 2.3")

    // 3
    c(0).setSignal(true)
    c(1).setSignal(true)
    run
    
    assert(out(0).getSignal === false,        "demux #c = 2 3.0")
    assert(out(1).getSignal === false,        "demux #c = 2 3.1")
    assert(out(2).getSignal === false,        "demux #c = 2 3.2")
    assert(out(3).getSignal === in.getSignal, "demux #c = 2 3.3")
  }
}
