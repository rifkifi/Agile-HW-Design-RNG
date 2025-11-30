import chisel3._
import chisel3.util._

/*
   AES256 implementation
   based on https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197-upd1.pdf
 */

class AES256 extends Module {
  val io = IO(new CipherIO(128))

  // AES S-box table
  private val sbox = VecInit(Seq(
      "h63".U(8.W), "h7c".U(8.W), "h77".U(8.W), "h7b".U(8.W), "hf2".U(8.W), "h6b".U(8.W), "h6f".U(8.W), "hc5".U(8.W),
      "h30".U(8.W), "h01".U(8.W), "h67".U(8.W), "h2b".U(8.W), "hfe".U(8.W), "hd7".U(8.W), "hab".U(8.W), "h76".U(8.W),
      "hca".U(8.W), "h82".U(8.W), "hc9".U(8.W), "h7d".U(8.W), "hfa".U(8.W), "h59".U(8.W), "h47".U(8.W), "hf0".U(8.W),
      "had".U(8.W), "hd4".U(8.W), "ha2".U(8.W), "haf".U(8.W), "h9c".U(8.W), "ha4".U(8.W), "h72".U(8.W), "hc0".U(8.W),
      "hb7".U(8.W), "hfd".U(8.W), "h93".U(8.W), "h26".U(8.W), "h36".U(8.W), "h3f".U(8.W), "hf7".U(8.W), "hcc".U(8.W),
      "h34".U(8.W), "ha5".U(8.W), "he5".U(8.W), "hf1".U(8.W), "h71".U(8.W), "hd8".U(8.W), "h31".U(8.W), "h15".U(8.W),
      "h04".U(8.W), "hc7".U(8.W), "h23".U(8.W), "hc3".U(8.W), "h18".U(8.W), "h96".U(8.W), "h05".U(8.W), "h9a".U(8.W),
      "h07".U(8.W), "h12".U(8.W), "h80".U(8.W), "he2".U(8.W), "heb".U(8.W), "h27".U(8.W), "hb2".U(8.W), "h75".U(8.W),
      "h09".U(8.W), "h83".U(8.W), "h2c".U(8.W), "h1a".U(8.W), "h1b".U(8.W), "h6e".U(8.W), "h5a".U(8.W), "ha0".U(8.W),
      "h52".U(8.W), "h3b".U(8.W), "hd6".U(8.W), "hb3".U(8.W), "h29".U(8.W), "he3".U(8.W), "h2f".U(8.W), "h84".U(8.W),
      "h53".U(8.W), "hd1".U(8.W), "h00".U(8.W), "hed".U(8.W), "h20".U(8.W), "hfc".U(8.W), "hb1".U(8.W), "h5b".U(8.W),
      "h6a".U(8.W), "hcb".U(8.W), "hbe".U(8.W), "h39".U(8.W), "h4a".U(8.W), "h4c".U(8.W), "h58".U(8.W), "hcf".U(8.W),
      "hd0".U(8.W), "hef".U(8.W), "haa".U(8.W), "hfb".U(8.W), "h43".U(8.W), "h4d".U(8.W), "h33".U(8.W), "h85".U(8.W),
      "h45".U(8.W), "hf9".U(8.W), "h02".U(8.W), "h7f".U(8.W), "h50".U(8.W), "h3c".U(8.W), "h9f".U(8.W), "ha8".U(8.W),
      "h51".U(8.W), "ha3".U(8.W), "h40".U(8.W), "h8f".U(8.W), "h92".U(8.W), "h9d".U(8.W), "h38".U(8.W), "hf5".U(8.W),
      "hbc".U(8.W), "hb6".U(8.W), "hda".U(8.W), "h21".U(8.W), "h10".U(8.W), "hff".U(8.W), "hf3".U(8.W), "hd2".U(8.W),
      "hcd".U(8.W), "h0c".U(8.W), "h13".U(8.W), "hec".U(8.W), "h5f".U(8.W), "h97".U(8.W), "h44".U(8.W), "h17".U(8.W),
      "hc4".U(8.W), "ha7".U(8.W), "h7e".U(8.W), "h3d".U(8.W), "h64".U(8.W), "h5d".U(8.W), "h19".U(8.W), "h73".U(8.W),
      "h60".U(8.W), "h81".U(8.W), "h4f".U(8.W), "hdc".U(8.W), "h22".U(8.W), "h2a".U(8.W), "h90".U(8.W), "h88".U(8.W),
      "h46".U(8.W), "hee".U(8.W), "hb8".U(8.W), "h14".U(8.W), "hde".U(8.W), "h5e".U(8.W), "h0b".U(8.W), "hdb".U(8.W),
      "he0".U(8.W), "h32".U(8.W), "h3a".U(8.W), "h0a".U(8.W), "h49".U(8.W), "h06".U(8.W), "h24".U(8.W), "h5c".U(8.W),
      "hc2".U(8.W), "hd3".U(8.W), "hac".U(8.W), "h62".U(8.W), "h91".U(8.W), "h95".U(8.W), "he4".U(8.W), "h79".U(8.W),
      "he7".U(8.W), "hc8".U(8.W), "h37".U(8.W), "h6d".U(8.W), "h8d".U(8.W), "hd5".U(8.W), "h4e".U(8.W), "ha9".U(8.W),
      "h6c".U(8.W), "h56".U(8.W), "hf4".U(8.W), "hea".U(8.W), "h65".U(8.W), "h7a".U(8.W), "hae".U(8.W), "h08".U(8.W),
      "hba".U(8.W), "h78".U(8.W), "h25".U(8.W), "h2e".U(8.W), "h1c".U(8.W), "ha6".U(8.W), "hb4".U(8.W), "hc6".U(8.W),
      "he8".U(8.W), "hdd".U(8.W), "h74".U(8.W), "h1f".U(8.W), "h4b".U(8.W), "hbd".U(8.W), "h8b".U(8.W), "h8a".U(8.W),
      "h70".U(8.W), "h3e".U(8.W), "hb5".U(8.W), "h66".U(8.W), "h48".U(8.W), "h03".U(8.W), "hf6".U(8.W), "h0e".U(8.W),
      "h61".U(8.W), "h35".U(8.W), "h57".U(8.W), "hb9".U(8.W), "h86".U(8.W), "hc1".U(8.W), "h1d".U(8.W), "h9e".U(8.W),
      "he1".U(8.W), "hf8".U(8.W), "h98".U(8.W), "h11".U(8.W), "h69".U(8.W), "hd9".U(8.W), "h8e".U(8.W), "h94".U(8.W),
      "h9b".U(8.W), "h1e".U(8.W), "h87".U(8.W), "he9".U(8.W), "hce".U(8.W), "h55".U(8.W), "h28".U(8.W), "hdf".U(8.W),
      "h8c".U(8.W), "ha1".U(8.W), "h89".U(8.W), "h0d".U(8.W), "hbf".U(8.W), "he6".U(8.W), "h42".U(8.W), "h68".U(8.W),
      "h41".U(8.W), "h99".U(8.W), "h2d".U(8.W), "h0f".U(8.W), "hb0".U(8.W), "h54".U(8.W), "hbb".U(8.W), "h16".U(8.W)
    ))

  // Round constants 
  private val rconBytes = VecInit(Seq(
    "h00".U(8.W), // dummy for index 0
    "h01".U(8.W), "h02".U(8.W), "h04".U(8.W), "h08".U(8.W),
    "h10".U(8.W), "h20".U(8.W), "h40".U(8.W), "h80".U(8.W),
    "h1b".U(8.W), "h36".U(8.W), "h6c".U(8.W), "hd8".U(8.W),
    "hab".U(8.W), "h4d".U(8.W)
  ))

  // Helper functions
  private def seqToMatx(seq: UInt): Vec[Vec[UInt]] = {
    val bytes = VecInit((0 until 16).map(i => seq(127 - 8*i, 120 - 8*i)))
    VecInit((0 until 4).map(r => VecInit((0 until 4).map(c => bytes(4*c + r)))))
  }
  private def matxToSeq(matx: Vec[Vec[UInt]]): UInt = {
    val bytes = (0 until 16).map { i =>
      val c = i / 4
      val r = i % 4
      matx(r)(c)
    }
    Cat(bytes)
  }
  private def rotWord(word: UInt): UInt = Cat(word(23, 0), word(31, 24))
  private def subWord(word: UInt): UInt = Cat(
    sbox(word(31, 24)),
    sbox(word(23, 16)),
    sbox(word(15, 8)),
    sbox(word(7, 0))
  )
  private def rconWord(b: UInt): UInt = Cat(rconBytes(b), 0.U(24.W))
  private def subBytes(state: Vec[Vec[UInt]]): Vec[Vec[UInt]] = {
    val out = Wire(Vec(4, Vec(4, UInt(8.W))))
    for (c <- 0 until 4; r <- 0 until 4) {
      out(r)(c) := sbox(state(r)(c))
    }
    out
  }
  private def addRoundKey(state: Vec[Vec[UInt]], rk: UInt): Vec[Vec[UInt]] = {
    val seqState = matxToSeq(state)
    val out = Wire(Vec(4, Vec(4, UInt(8.W))))
    out := seqToMatx(seqState ^ rk)
    out
  }
  private def xtime(x: UInt): UInt = {
    val x2 = (x << 1)(7,0)
    Mux(x(7), x2 ^ "h1b".U(8.W), x2)
  }
  private def mul2(x: UInt): UInt = xtime(x)
  private def mul3(x: UInt): UInt = xtime(x) ^ x
  private def mixSingleColumn(col: Vec[UInt]): Vec[UInt] = {
    val x0 = col(0); val x1 = col(1); val x2 = col(2); val x3 = col(3)
    val y0 = mul2(x0) ^ mul3(x1) ^ x2 ^ x3
    val y1 = x0 ^ mul2(x1) ^ mul3(x2) ^ x3
    val y2 = x0 ^ x1 ^ mul2(x2) ^ mul3(x3)
    val y3 = mul3(x0) ^ x1 ^ x2 ^ mul2(x3)
    VecInit(Seq(y0, y1, y2, y3))
  }
  private def mixColumns(inState: Vec[Vec[UInt]]): Vec[Vec[UInt]] = {
    val out = Wire(Vec(4, Vec(4, UInt(8.W))))
    for (c <- 0 until 4) {
      val colIn  = Wire(Vec(4, UInt(8.W)))
      for (r <- 0 until 4) colIn(r) := inState(r)(c)
      val colOut = mixSingleColumn(colIn)
      for (r <- 0 until 4) out(r)(c) := colOut(r)
    }
    out
  }
  private def rotateRow(row: Vec[UInt], k: Int): Vec[UInt] =
    VecInit((0 until 4).map(i => row((i + k) % 4)))
  private def shiftRows(inState: Vec[Vec[UInt]]): Vec[Vec[UInt]] =
    VecInit(
      Seq(
        inState(0),
        rotateRow(inState(1), 1),
        rotateRow(inState(2), 2),
        rotateRow(inState(3), 3)
      )
    )

  // Registers
  val W = Wire(Vec(60, UInt(32.W)))
  val round = RegInit(0.U(4.W))
  val roundKey = RegInit(VecInit(Seq.fill(15)(0.U(128.W))))
//   val dataState = Reg(Vec(4, Vec(4, UInt(8.W)))) // state(row)(col)
  val dataState = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(4)(0.U(8.W))))))
  W := VecInit(Seq.fill(60)(0.U(32.W)))

  // FSM states
  val sIdle :: sRound0 :: sRoundCore :: sFinalRound :: sDone :: Nil = Enum(5)
  val state = RegInit(sIdle)
  
  io.done := false.B
  io.out := matxToSeq(dataState)

  switch(state) {
    is(sIdle) {
      when(io.start) {
        // KeyExpansion Process
        for (i <- 0 until 8) { 
          W(i) := io.in_key(255 - i * 32, 224 - i * 32) 
        }
        for (i <- 8 until 60) {
          if (i % 8 == 0) {
            W(i) := W(i - 8) ^ subWord(rotWord(W(i - 1))) ^ rconWord((i / 8).U)
          } else if (i % 8 == 4) {
            val sub = subWord(W(i - 1))
            W(i) := W(i - 8) ^ subWord(W(i - 1))
          } else {
            W(i) := W(i - 8) ^ W(i - 1)
          }
        }

        // Map words into Roundkey
        for (i <- 0 until 15) {
          roundKey(i) := Cat(W(i * 4), W(i * 4 + 1), W(i * 4 + 2), W(i * 4 + 3))
        }

        // map data into state matrix 4x4 byte 
        dataState := seqToMatx(io.in_data)
        
        io.done := false.B
        round := 0.U
        state := sRound0
      }
    }
    is(sRound0) {
      // round 0 process
      dataState := addRoundKey(dataState, roundKey(0))
      round := 1.U
      state := sRoundCore
    }
    is(sRoundCore) {
      // round 1-14 process consist of subBytes, shiftRows, mixColumns and addRoundKey
      val sb = subBytes(dataState)
      val sr = shiftRows(sb)
      val mix = mixColumns(sr)
      dataState := addRoundKey(mix, roundKey(round))
      when (round === 13.U) {
        round := 14.U
        state := sFinalRound
      }.otherwise {
        round := round + 1.U   
      } 
    }
    is(sFinalRound) {
      // final round process consist of subBytes, shiftRows and addRoundKey
      val rk = roundKey(round)
      val sb = subBytes(dataState)
      val sr = shiftRows(sb)
      dataState := addRoundKey(sr, rk)
      state := sDone
    }
    is(sDone) {
      // convert state matrix to sequence and assign it to output
      io.done := true.B
      when(!io.start) { state := sIdle }
    }
  }
}

object AES256 {
  val outWidth = 128
}