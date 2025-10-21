
import chisel3._
import chisel3.util.Cat
import scala.math._

/* 
  A n-bit Linear-feedback shift register (LFSR)
  with user-defined taps and seed
*/ 

class LFSR(n: Int = 4, tap_in: Seq[Int] = Seq(0), seed: Int = 1) extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(n.W))  // current state
  })

  require(seed >= 1 && seed < pow(2, n), "seed must be 1..2^n")
  tap_in.foreach(i => require(i >= 0 && i < n, "tap indices must be in 0..n"))
  val taps = tap_in :+ (n - 1)        // always include the MSB as a tap
  val lfsr = RegInit(seed.U(n.W))

  // XOR of selected tap bits for feedback
  val feedback = taps.map(lfsr(_)).reduce(_ ^ _)

  // shift left, insert feedback at LSB
  lfsr := Cat(lfsr(n-2, 0), feedback)

  io.out := lfsr
}