import chisel3._
import chisel3.util._

class Pools extends Module {
  val io = IO(new Bundle {
    val inData = Input(UInt(256.W))              // new hashed pool value
    val writePool = Input(Bool())
    val readPool = Input(Bool())
    val notEnoughDataFlag = Output(Bool())
    val outPoolsCount = Output(UInt(5.W))        // number of pools included in output
    val outSeedingData = Output(Vec(32, UInt(256.W)))  // masked pool data
    val outUpdateData = Output(UInt(32.W))
  })

  val nextUpdatedPool = RegInit(0.U(5.W))
  val pool0WriteCounter = RegInit(0.U(32.W))
  val pools = Reg(Vec(32, UInt(256.W)))
  val reseedCounter     = RegInit(0.U(32.W))

  // --- Status flags & update data ---
  io.notEnoughDataFlag := (pool0WriteCounter < 64.U)
  io.outUpdateData := pools(nextUpdatedPool)


  // --- Determine which pools are included (2^i divides counter) ---
  val maskVec = Wire(Vec(32, Bool()))
  for (i <- 0 until 32) {
    if (i == 0)
      maskVec(i) := true.B
    else
      maskVec(i) := (reseedCounter =/= 0.U) && (reseedCounter(i-1, 0) === 0.U)
  }

  // Count how many are true = number of pools included
  val poolsCount = PopCount(maskVec)
  io.outPoolsCount := poolsCount

  // --- Output the data of included pools ---
  for (i <- 0 until 32) {
    io.outSeedingData(i) := Mux(maskVec(i), pools(i), 0.U)
  }

  // --- Clear used pools when reseeding ---
  when(io.readPool) {
    for (i <- 0 until 32) {
      when(maskVec(i)) {
        pools(i) := 0.U
      }
    }
    reseedCounter := reseedCounter + 1.U
    pool0WriteCounter := 0.U
    // --- Write to next pool ---
  }.elsewhen(io.writePool) {
    pools(nextUpdatedPool) := io.inData
    nextUpdatedPool := nextUpdatedPool + 1.U

    when(nextUpdatedPool === 0.U) {
      pool0WriteCounter := pool0WriteCounter + 1.U
    }
  }
}


