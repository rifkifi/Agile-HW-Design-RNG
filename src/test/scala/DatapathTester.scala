import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class DatapathTester extends AnyFlatSpec with ChiselScalatestTester {

  "AES part of Datapath" should "produce output and change with counter" in {
    test(new Datapath()).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      // Use the storedSeed for AES key
      c.io.updateStoredSeed.poke(true.B)
      c.io.Cipher_en.poke(false.B)

      // Reset step
      c.clock.step(1)

      // Start AES
      c.io.Cipher_en.poke(true.B)

      // Wait until AES signals done
      while (!c.io.Cipher_done.peek().litToBoolean) {
        c.clock.step(1)
      }

      // Read AES output
      val out1 = c.io.out.peek().litValue
      println(s"AES output with storedSeed: 0x${out1.toString(16)}")
      c.io.Cipher_en.poke(false.B)
      c.clock.step(1)

      // Trigger AES again (counter increments)
      c.io.Cipher_en.poke(true.B)

      while (!c.io.Cipher_done.peek().litToBoolean) {
        c.clock.step(1)
      }

      val out2 = c.io.out.peek().litValue
      println(s"AES output next counter: 0x${out2.toString(16)}")

      assert(out1 != out2, "AES output should change with counter")
    }
  }
}
