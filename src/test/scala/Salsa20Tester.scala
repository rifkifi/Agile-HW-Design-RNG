import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class Salsa20Tester  extends AnyFlatSpec with ChiselScalatestTester {
  "Salsa20 module" should "run" in {
    test(new Salsa20) { dut =>

      dut.clock.step(1)

      dut.io.in_key.poke("h000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f".U(256.W))
      dut.io.in_nonce.poke("h0000004a00000000".U(64.W))
      dut.io.in_counter.poke("h0000000109000000".U(64.W))

      dut.clock.step(1)
      dut.io.start.poke(1.U)

      while (!dut.io.done.peek().litToBoolean) {
        dut.clock.step(1)
      }
    }
  }
}