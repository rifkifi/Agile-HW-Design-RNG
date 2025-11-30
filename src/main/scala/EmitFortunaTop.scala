import chisel3._

object EmitFortunaTop extends App {
  // Generate Verilog into "generated" directory
  emitVerilog(new FortunaTop(false), Array("--target-dir", "generated"))
}
