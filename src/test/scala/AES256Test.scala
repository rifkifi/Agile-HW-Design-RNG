import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AES256Test extends AnyFlatSpec with ChiselScalatestTester {
  "AESR256" should "generate sequence of number" in {
    test(new AES256()) { dut =>
      println("Testing AES256 functionality")
      dut.io.in_key.poke(
        "h603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4".U(
          256.W
        )
      )
      dut.io.in_data.poke("h00000000000000000000000000616262".U(128.W))
      dut.io.start.poke(true.B)
      dut.clock.step()
      while (!dut.io.done.peek().litToBoolean) { dut.clock.step(1) }
      println(f"Output: ${dut.io.out.peek().litValue}%x")
    }
  }
}
