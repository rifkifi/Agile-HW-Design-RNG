import chisel3._

object GenDatapath extends App {
  // Generate Verilog into "generated" directory
  emitVerilog(new Datapath(), Array("--target-dir", "generated"))
}