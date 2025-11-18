import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PoolsTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Pools"

  it should "update pools and show correct outPoolsCount" in {
    test(new Pools) { c =>
      // Initially, pool0WriteCounter = 0, reseedCounter = 0
      // Only pool 0 should be included
      c.io.outPoolsCount.expect(1.U)
      c.io.outSeedingData(0).expect(0.U)

      // Write something to pool 0
      c.io.inData.poke(123.U)
      c.io.writePool.poke(true.B)
      c.clock.step(1)
      c.io.writePool.poke(false.B)

      // Check that pool0 is updated
      c.io.outSeedingData(0).expect(123.U)

      // Trigger first reseed
      c.io.readPool.poke(true.B)
      c.clock.step(1)
      c.io.readPool.poke(false.B)

      // After first reseed, pool0 should be cleared
      c.io.outSeedingData(0).expect(0.U)
      c.io.outPoolsCount.expect(1.U)  // still 1 pool active for reseed=1

      // Write to pool 1
      c.io.inData.poke(200.U)
      c.io.writePool.poke(true.B)
      c.clock.step(1)
      c.io.writePool.poke(false.B)

      // Trigger second reseed (reseedCounter = 2)
      c.io.readPool.poke(true.B)
      c.clock.step(1)
      c.io.readPool.poke(false.B)

      // Only pool 0 and 1 should be included because reseedCounter=2
      c.io.outPoolsCount.expect(2.U)
    }
  }
}