import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class FortunaTopTester extends AnyFlatSpec with ChiselScalatestTester {

  "Top module" should "work properly" in {
    test(new FortunaTop(true)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      // Generate Data
      c.io.generate_data.poke(true.B)

      c.clock.step(1)
      c.io.generate_data.poke(false.B)

      var maxcycles = 20
      var cyclesCounter = maxcycles

      // Wait until Generator done
      while (c.io.busy.peek().litToBoolean && cyclesCounter > 0) {
        c.clock.step(1)
        cyclesCounter = cyclesCounter-1
      }

      // Read AES output
      val out1 = c.io.out_rng_data.peek().litValue
      println(s"RNG Data output: 0x${out1.toString(16)}")

      c.clock.step(1)

      // Trigger RNG Gen again
      c.io.generate_data.poke(true.B)
      c.clock.step(1)
      c.io.generate_data.poke(false.B)

      var cyclesCounter2 = maxcycles

      while (c.io.busy.peek().litToBoolean  && cyclesCounter2 > 0) {
        c.clock.step(1)
        cyclesCounter2 = cyclesCounter2-1
      }

      val out2 = c.io.out_rng_data.peek().litValue
      println(s"RNG Data Output 2: 0x${out2.toString(16)}")

      assert(out1 != out2, "RNG output should change with counter")


      // TEST OF SHAd256 AND POOLS UPDATE (VCD STUDY)
      c.clock.step(1)
      c.io.add_data.poke(true.B)
      c.io.in_random_data.poke(255.U)
      c.clock.step(1)
      c.io.add_data.poke(false.B)


      while (c.io.busy.peek().litToBoolean) {
        c.clock.step(1)
      }
      c.clock.step(1)
    }
  }
}
