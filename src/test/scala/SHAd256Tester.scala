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

      val expected = BigInt("4f8b42c22dd3729b519ba6f68d2da7cc" + "5b2d606d05daed5ad5128cc03e6c6358", 16)
      assert(digest == expected, "SHAd256 digest mismatch!")
    }
  }
}
