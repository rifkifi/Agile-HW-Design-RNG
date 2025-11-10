import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class test_ChaCha  extends AnyFlatSpec with ChiselScalatestTester {
  "test_ChaCha " should "pass" in {
    test(new ChaCha) { dut =>


      dut.clock.step(1)
      dut.io.in_start.poke(1.U)
      //testVector 1
//      dut.io.in_key.poke("h0000000000000000000000000000000000000000000000000000000000000000".U(256.W))
//      dut.io.in_nonce.poke("h0000000000000000".U(64.W))
//      dut.io.in_counter.poke("h0000000000000000".U(64.W))

      //testVector 2
//      dut.io.in_key.poke("h0000000006000000000000000000003000000000000000004000000000000000".U(256.W))
//      dut.io.in_nonce.poke("h0000000000000000".U(64.W))
//      dut.io.in_counter.poke("h0000000000000000".U(64.W))

      //testVector 3
      dut.io.in_key.poke("h00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff".U(256.W))
      dut.io.in_nonce.poke("h7000000000000000".U(64.W))
      dut.io.in_counter.poke("h0f1e2d3c4b59687".U(64.W))




      if(dut.io.out_ready == true.B){
        //answer TestVector 1
        //dut.io.out_Decoding_key.expect("h76b8e0ada0f13d90405d6ae55386bd28bde72c7c3eafb0f9b735f0f1a7b3f4b705a15d3b6a3f24d31d6b93d3d7e99f554f4b3f0aa8f2c0b0c2e5d23f7b4c3c0b".U(512.W))
        //answer TestVector 2
        //dut.io.out_Decoding_key.expect("36f0b3c0d96d0f093c7c1a691c2b7658d154cf3a7c92e8c10b12a7f2499e0f362f68b2a17e1d4c855d3f9a0c81bc7f141627c9b08c3e1a2f49b0f3c70a5d6e91".U)
        //answer TestVector 3
        dut.io.out_Decoding_key.expect("hf967649634e03569c6a79b7487b4d0db2852f6cfa36d604cf25d111c43b67ddd4344f0d64ab210b36290c34ddd65e072b3fa34e4c88d2c5c3e811f9abe4285be".U(512.W))

      }
    }
  }
}
