import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class ChaChaTester  extends AnyFlatSpec with ChiselScalatestTester {
  "ChaCha module " should "pass" in {
    test(new ChaCha).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>


      dut.clock.step(1)
      //testVector 1
//      dut.io.in_key.poke("h0000000000000000000000000000000000000000000000000000000000000000".U(256.W))
//      dut.io.in_nonce.poke("h0000000000000000".U(64.W))
//      dut.io.in_counter.poke("h0000000000000000".U(64.W))

      //testVector 2
//      dut.io.in_key.poke("h0000000006000000000000000000003000000000000000004000000000000000".U(256.W))
//      dut.io.in_nonce.poke("h0000000000000000".U(64.W))
//      dut.io.in_counter.poke("h0000000000000000".U(64.W))

      //testVector 3
//      dut.io.in_key.poke("h00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff".U(256.W))
//      dut.io.in_nonce.poke("h7000000000000000".U(64.W))
//      dut.io.in_counter.poke("h0f1e2d3c4b59687".U(64.W))

      dut.io.in_key.poke("h000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f".U(256.W))
      dut.io.in_nonce.poke("h0000004a00000000".U(64.W))
      dut.io.in_counter.poke("h0000000109000000".U(64.W))

      dut.clock.step(1)
      dut.io.start.poke(1.U)

      while (!dut.io.done.peek().litToBoolean) {
        dut.clock.step(1)
      }

      val block = dut.io.out.peek().litValue
      val expected = BigInt("837778abe238d763a67ae21e5950bb2fc4f2d0c7fc62bb2f8fa018fc3f5ec7b7335271c2f29489f3eabda8fc82e46ebdd19c12b4b04e16de9e83d0cb4e3c50a2", 16)
      assert(block == expected, "ChaCha block mismatch!")
    }
  }
}
