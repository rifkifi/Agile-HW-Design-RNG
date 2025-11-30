import chisel3._

object GenDatapath extends App {
  // Generate Verilog into "generated" directory
  emitVerilog(new Datapath(CipherType.AES, false), Array("--target-dir", "generated"))
}