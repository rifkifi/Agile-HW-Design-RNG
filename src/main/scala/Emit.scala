import chisel3._

/**
  * Emit SystemVerilog.
  *
  * Usage examples:
  *   sbt "runMain AES256Emit"
  *   sbt "runMain SHAd256Emit"
  *   sbt "runMain SHA256Emit"
  *   
  */

object AES256Emit extends App {
  emitVerilog(new AES256, Array("--target-dir", "generated"))
}

object SHAd256Emit extends App {
  emitVerilog(new SHAd256, Array("--target-dir", "generated"))
}
object SHA256Emit extends App {
  emitVerilog(new SHA256, Array("--target-dir", "generated"))
}