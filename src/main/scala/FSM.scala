import chisel3._
import chisel3.util._

class FSM extends Module{
  val io = IO(new Bundle {
    val add_data = Input(Bool())
    val generate_data = Input(Bool())

    //val out_ciphered_text = Output(UInt(512.W))
    val busy = Output(Bool())
    val valid_data = Output(Bool())

    // Controls signals for Datapath
    val SHAd_a_en = Output(Bool())
    val SHAd_a_done = Input(Bool())

    val SHAd_b_en = Output(Bool())
    val SHAd_b_done = Input(Bool())

    val Pools_writeData = Output(Bool())
    val Pools_readData = Output(Bool())

    val Cipher_en = Output(Bool())
    val Cipher_done = Input(Bool())

    val useStoredSeed = Output(Bool())
    val updateStoredSeed = Output(Bool())
    val updatePoolSeed = Output(Bool())

    val displayData = Output(Bool())

    val Pools_notEnoughDataFlag = Input(Bool())
  })

  io.busy := false.B
  io.useStoredSeed := true.B
  io.updateStoredSeed := false.B
  io.updatePoolSeed := false.B
  io.SHAd_a_en := false.B
  io.SHAd_b_en := false.B
  io.Cipher_en := false.B
  io.Pools_readData := false.B
  io.Pools_writeData := false.B
  io.displayData := true.B

  val idle :: addDataToPool :: generateData :: generateKey :: updateKey :: Nil = Enum(5)

  val state = RegInit(idle)
  val cnt = RegInit(0.U(8.W))

  val i = RegInit(0.U(1.W))
  val cipherFinished = RegInit(false.B)

  io.valid_data := false.B

  switch(state){

    is(idle){
      io.valid_data := true.B
      when(io.add_data){
        state := addDataToPool
        io.busy := true.B
        io.SHAd_a_en := true.B
      }.elsewhen(io.generate_data){
        io.busy := true.B
        io.valid_data := false.B
        when(io.Pools_notEnoughDataFlag){
          state := updateKey
          io.Cipher_en := true.B
        }.otherwise{
          state := generateKey
          io.SHAd_b_en := true.B
        }
      }
    }

    is(addDataToPool){
      io.busy := true.B
      when(io.SHAd_a_done){
        io.Pools_writeData := true.B
        state := idle
      }
    }

    is(generateData){
      io.busy := true.B
      when(io.Cipher_done){
        when(cnt === 127.U){
          io.updateStoredSeed := true.B
        }
        state := idle
        io.valid_data := true.B
        cnt := cnt + 1.U
      }
    }

    is(generateKey){
      io.busy := true.B
      when(io.SHAd_b_done){
        io.useStoredSeed := false.B
        io.Pools_readData := true.B
        state := generateData
      }
    }

    is(updateKey){
      io.displayData := false.B
      io.busy := true.B
      cipherFinished := io.Cipher_done
      when(cipherFinished){
        io.updatePoolSeed := true.B
        i := !i
        io.Cipher_en := true.B
        when(i === 1.U){
          state := generateData
        }
      }
    }
  }
}
