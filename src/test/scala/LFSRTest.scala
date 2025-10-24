import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LFSRTest extends AnyFlatSpec with ChiselScalatestTester {
  "LFSR" should "generate sequence of number" in {
    test(new LFSR(8, Seq(1,0), 1)) { dut =>
      println("Testing LFSR functionality")
       for (i <- 0 until 20) {
        println(f"cycle=$i%2d  state=${dut.io.out.peek().litValue}%d")
        dut.clock.step()
      }
    }
  }
  
}
