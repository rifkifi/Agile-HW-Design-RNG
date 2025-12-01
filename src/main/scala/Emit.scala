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
  emitVerilog(new SHAd256(false), Array("--target-dir", "generated"))
}
object SHA256Emit extends App {
  emitVerilog(new SHA256, Array("--target-dir", "generated"))
}
object PoolsEmit extends App {
  emitVerilog(new Pools, Array("--target-dir", "generated"))
}
object DatapathEmit extends App {
  emitVerilog(new Datapath(CipherType.AES, false), Array("--target-dir", "generated"))
}
object FSMEmit extends App {
  emitVerilog(new FSM, Array("--target-dir", "generated"))
}
object SHAd256_MultiEmit extends App {
  emitVerilog(new SHAd256_Multi(false), Array("--target-dir", "generated"))
}
object ChaChaEmit extends App {
  emitVerilog(new ChaCha, Array("--target-dir", "generated"))
}
object Salsa20Emit extends App {
  emitVerilog(new Salsa20, Array("--target-dir", "generated"))
}