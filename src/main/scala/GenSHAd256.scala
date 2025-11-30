import chisel3._

object GenSHAd256 extends App {
  // Generate Verilog into "generated" directory
  emitVerilog(new SHAd256(false), Array("--target-dir", "generated"))
}