import chisel3._
import chisel3.util._

class FortunaTop(val debug: Boolean) extends Module{
  val io = IO(new Bundle {
    val add_data = Input(Bool())
    val generate_data = Input(Bool())

    val in_random_data = Input(UInt(8.W))
    val out_rng_data = Output(UInt(128.W))

    val busy = Output(Bool())
    val valid_data = Output(Bool())
  })


  val Datapath = Module(new Datapath(CipherType.AES, debug))
  val FSM = Module(new FSM())

  io.busy := FSM.io.busy
  io.valid_data := FSM.io.valid_data

  FSM.io.add_data := io.add_data
  FSM.io.generate_data := io.generate_data

  Datapath.io.in_data := io.in_random_data
  io.out_rng_data := Datapath.io.out

  Datapath.io.SHAd_a_en := FSM.io.SHAd_a_en
  Datapath.io.SHAd_b_en := FSM.io.SHAd_b_en
  Datapath.io.Cipher_en := FSM.io.Cipher_en

  FSM.io.SHAd_a_done := Datapath.io.SHAd_a_done
  FSM.io.SHAd_b_done := Datapath.io.SHAd_b_done
  FSM.io.Cipher_done := Datapath.io.Cipher_done

  Datapath.io.Pools_writeData := FSM.io.Pools_writeData
  Datapath.io.Pools_readData := FSM.io.Pools_readData

  Datapath.io.updateStoredSeed := FSM.io.updateStoredSeed
  Datapath.io.useStoredSeed := FSM.io.useStoredSeed
  Datapath.io.updatePoolSeed := FSM.io.updatePoolSeed
  Datapath.io.displayData := FSM.io.displayData

  FSM.io.Pools_notEnoughDataFlag := Datapath.io.Pools_notEnoughDataFlag
}
