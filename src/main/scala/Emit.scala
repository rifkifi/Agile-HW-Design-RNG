import chisel3._

/**
  * Emit SystemVerilog for the LFSR module.
  *
  * Usage examples:
  *   sbt "runMain LFSREmit --n 4 --taps 1 --seed 1 --target-dir generated"
  *   sbt "runMain LFSREmit --n 16 --taps 1,2,4,6 --seed 1 --target-dir out"
  */
object LFSREmit extends App {
  def get(name: String, default: String): String = {
    val idx = args.indexOf(s"--$name")
    if (idx >= 0 && idx + 1 < args.length) args(idx + 1) else default
  }

  val n         = get("n", "4").toInt
  val tapsStr   = get("taps", "1")
  val seed      = get("seed", "1").toInt
  val targetDir = get("target-dir", "generated")

  val taps: Seq[Int] =
    if (tapsStr.trim.isEmpty) Seq.empty
    else tapsStr.split(",").toSeq.map(_.trim).filter(_.nonEmpty).map(_.toInt)

  val stageArgs = Array("-td", targetDir)
  val verilog = chisel3.emitVerilog(new LFSR(n, taps, seed), stageArgs)
  println(s"Verilog emitted to '$targetDir' (top: LFSR.v). Length=${verilog.length}")
}
