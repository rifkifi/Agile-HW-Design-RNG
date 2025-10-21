import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest.simulator.WriteVcdAnnotation

class SHAd256Tester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SHAd256"

  it should "SHAd256 single block hash" in {
    test(new SHAd256()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Example: "abc" padded into 512 bits
      // Real SHA-256 requires proper padding, but here we just test with one block
      val abcPadded = BigInt("61626380000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000018", 16)

      dut.io.in.poke(abcPadded.U(512.W))   // important: 512 bits wide
      dut.io.start.poke(true.B)
      dut.clock.step()
      dut.io.start.poke(false.B)
      while (!dut.io.done.peek().litToBoolean) { dut.clock.step(1) }

      val digest = dut.io.out.peek().litValue
      println(f"Digest = 0x$digest%064x")


      val expected = BigInt("efc26353a4b5b1e1b6bcf1be982db4dd" + "0c845adc7d7fc896db0931aedc1799b5", 16)
      assert(digest == expected, "SHAd256 digest mismatch!")
    }
  }
}
