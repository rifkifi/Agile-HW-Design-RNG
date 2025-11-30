import chisel3._
import chisel3.util._

object CipherType extends ChiselEnum {
  val AES, CHACHA = Value
}

class CipherIO(maxOutWidth: Int) extends Bundle {
  val start    = Input(Bool())
  val in_key   = Input(UInt(256.W))
  val in_data  = Input(UInt(128.W))     // used by AES
  val in_nonce = Input(UInt(64.W))      // used by ChaCha
  val in_counter = Input(UInt(64.W))    // used by ChaCha
  val out      = Output(UInt(maxOutWidth.W))
  val done     = Output(Bool())
}

class Datapath(cipher: CipherType.Type, val debug: Boolean) extends Module{

  val outWidth = cipher match {
    case CipherType.AES => AES256.outWidth
    case CipherType.CHACHA   => ChaCha.outWidth
  }

  val io = IO(new Bundle {
    val in_data = Input(UInt(8.W))

    val SHAd_a_en = Input(Bool())
    val SHAd_a_done = Output(Bool())

    val SHAd_b_en = Input(Bool())
    val SHAd_b_done = Output(Bool())

    val Pools_writeData = Input(Bool())
    val Pools_readData = Input(Bool())

    val Cipher_en = Input(Bool())
    val Cipher_done = Output(Bool())

    val useStoredSeed = Input(Bool())
    val updateStoredSeed = Input(Bool())

    val out = Output(UInt(outWidth.W))

    val Pools_notEnoughDataFlag = Output(Bool())
  })

  val SHAd256_a = Module(new SHAd256(debug))
  val Pools = Module(new Pools())
  val SHAd256_b = Module(new SHAd256_Multi(debug))


  val CipherIO = cipher match {
    case CipherType.AES => Module(new AES256()).io
    case CipherType.CHACHA  => Module(new ChaCha()).io
  }


  //val AES = Module(new AES256())

  val storedSeed = RegInit(123456789.U(256.W))
  val poolSeed = RegInit(0.U(256.W))

  val cnt = RegInit(0.U(128.W))

  SHAd256_a.io.start := io.SHAd_a_en
  SHAd256_a.io.in := Cat(Pools.io.outUpdateData, io.in_data, 1.U(1.W), 0.U(183.W),264.U(64.W))

  io.SHAd_a_done := SHAd256_a.io.done

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
  SHAd256_b.io.start := io.SHAd_b_en

  io.SHAd_b_done := SHAd256_b.io.done

  when(SHAd256_b.io.done){
    poolSeed := SHAd256_b.io.out
  }

  val state = Mux(io.useStoredSeed, storedSeed, poolSeed)

  when(io.updateStoredSeed){
    storedSeed := Cat(storedSeed(255,128), CipherIO.out)
  }

  CipherIO.start := io.Cipher_en
  CipherIO.in_key := state
  CipherIO.in_data := cnt
  CipherIO.in_counter := cnt
  CipherIO.in_nonce := 0.U

  io.Cipher_done := CipherIO.done
  io.out := CipherIO.out

  when(CipherIO.done){
    cnt := cnt + 1.U
  }

//  AES.io.in_key := state
//  AES.io.in_data := cnt
//  AES.io.start := io.Cipher_en
//
//  io.Cipher_done := AES.io.done
//
//  when(AES.io.done){
//    cnt := cnt + 1.U
//  }
//
//  io.out := AES.io.out
}
