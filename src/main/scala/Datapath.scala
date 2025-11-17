import chisel3._
import chisel3.util._

class Datapath extends Module{
  val io = IO(new Bundle {
    val in_data = Input(UInt(8.W))
    val SHAd_1_en = Input(Bool())
    val Pools_writeData = Input(Bool())
    val Pools_readData = Input(Bool())
    val updateStoredSeed = Input(Bool())

    val out = Output(UInt(256.W))
    val SHAd_1_done = Output(Bool())
    val Pools_notEnoughDataFlag = Output(Bool())
  })

  val SHAd256_a = Module(new SHAd256())
  val Pools = Module(new Pools())
  val SHAd256_b = Module(new SHAd256_Multi())
  val AES = Module(new AES256())

  val storedSeed = RegInit(0.U(256.W))
  val poolSeed = RegInit(0.U(256.W))

  val cnt = RegInit(0.U(128.W))

  SHAd256_a.io.start := io.SHAd_1_en
  SHAd256_a.io.in := Cat(Pools.io.outUpdateData, io.in_data, 1.U(1.W), 0.U(183.W),264.U(64.W))

  io.SHAd_1_done := SHAd256_a.io.done

  Pools.io.inData := SHAd256_a.io.out
  Pools.io.writePool := io.Pools_writeData
  Pools.io.readPool := io.Pools_readData

  io.Pools_notEnoughDataFlag := Pools.io.notEnoughDataFlag

  val validPools = (0 until 32).map { i =>
    Mux(i.U < Pools.io.outPoolsCount, Pools.io.outSeedingData(i), 0.U(256.W))
  }
  val msg = Cat(validPools)
  val msgLenBits = (Pools.io.outPoolsCount << 8)

  val nbMsgBlocks = (Pools.io.outPoolsCount + 1.U ) >> 1.U

  val shaBlocks = Mux(
    Pools.io.outPoolsCount(0),
    Cat(msg, msgLenBits.pad(256)),
    Cat(msg, msgLenBits.pad(512)),
  )

  SHAd256_b.io.shaBlocks := shaBlocks
  SHAd256_b.io.nbMsgBlocks := nbMsgBlocks.asUInt

  when(SHAd256_b.io.done){
    poolSeed := SHAd256_b.io.out
  }

  val seed = Mux(io.updateStoredSeed, storedSeed, poolSeed)

  AES.io.in_key := seed
  AES.io.in_data := cnt

  when(AES.io.done){
    cnt := cnt + 1.U
  }

  io.out := AES.io.out
}
