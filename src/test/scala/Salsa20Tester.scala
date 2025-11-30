import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class Salsa20Tester  extends AnyFlatSpec with ChiselScalatestTester {
  "test_Salsa20 " should "pass" in {
    test(new Salsa20) { dut =>


      dut.clock.step(1)
      dut.io.in_start.poke(1.U)
      //testVector 1
      //dut.io.in_key.poke("h0000000000000000000000000000000000000000000000000000000000000000".U(256.W))
      //testVector 2
      dut.io.in_key.poke("h0000000006000000000000000000003000000000000000004000000000000000".U(256.W))


      dut.io.in_nonce.poke("h0000000000000000".U(64.W))
      dut.io.in_counter.poke("h0000000000000000".U(64.W))

      if(dut.io.out_ready == true.B){
        //answer TestVector 1
//        dut.io.out_Decoding_key.expect("h9f07e7be5551387a98b6b0b15b5d4a5f1a6a4a0b5f8eecdea0c3f00b12399c5ec06a17a17c1a6f2cf17835c79e1e0c1f5551f6f504a5a8ec18d6c1a33e4b8b5f".U)
        //answer TestVector 2
        dut.io.out_Decoding_key.expect("h6ea3a92733386ffabd772b55c0603427bc8f26d358f6e8904ae7760980165aa354194d05d17e4555c97ce1b505ba7c4fda825e9e356d23bcc768842842c0514".U(512.W))

      }
    }
  }
}