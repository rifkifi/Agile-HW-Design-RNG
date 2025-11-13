import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class test_top_fsm extends AnyFlatSpec with ChiselScalatestTester {
  "top_fsm " should "pass" in {
    test(new top_fsm) { dut =>
      

    }
  }
}
