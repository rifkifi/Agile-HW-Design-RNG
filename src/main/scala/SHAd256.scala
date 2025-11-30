import chisel3._
import chisel3.util._

class SHAd256(val debug: Boolean) extends Module {
  val io = IO(new Bundle {
    val in    = Input(UInt(512.W))
    val start = Input(Bool())
    val done  = Output(Bool())
    val out   = Output(UInt(256.W))
  })

  // Initial hash values
  val Hinit = VecInit(Seq(
    "h6a09e667".U(32.W), "hbb67ae85".U(32.W), "h3c6ef372".U(32.W), "ha54ff53a".U(32.W),
    "h510e527f".U(32.W), "h9b05688c".U(32.W), "h1f83d9ab".U(32.W), "h5be0cd19".U(32.W)
  ))

  // Round constants
  val K = VecInit(Seq(
    "h428a2f98".U, "h71374491".U, "hb5c0fbcf".U, "he9b5dba5".U,
    "h3956c25b".U, "h59f111f1".U, "h923f82a4".U, "hab1c5ed5".U,
    "hd807aa98".U, "h12835b01".U, "h243185be".U, "h550c7dc3".U,
    "h72be5d74".U, "h80deb1fe".U, "h9bdc06a7".U, "hc19bf174".U,
    "he49b69c1".U, "hefbe4786".U, "h0fc19dc6".U, "h240ca1cc".U,
    "h2de92c6f".U, "h4a7484aa".U, "h5cb0a9dc".U, "h76f988da".U,
    "h983e5152".U, "ha831c66d".U, "hb00327c8".U, "hbf597fc7".U,
    "hc6e00bf3".U, "hd5a79147".U, "h06ca6351".U, "h14292967".U,
    "h27b70a85".U, "h2e1b2138".U, "h4d2c6dfc".U, "h53380d13".U,
    "h650a7354".U, "h766a0abb".U, "h81c2c92e".U, "h92722c85".U,
    "ha2bfe8a1".U, "ha81a664b".U, "hc24b8b70".U, "hc76c51a3".U,
    "hd192e819".U, "hd6990624".U, "hf40e3585".U, "h106aa070".U,
    "h19a4c116".U, "h1e376c08".U, "h2748774c".U, "h34b0bcb5".U,
    "h391c0cb3".U, "h4ed8aa4a".U, "h5b9cca4f".U, "h682e6ff3".U,
    "h748f82ee".U, "h78a5636f".U, "h84c87814".U, "h8cc70208".U,
    "h90befffa".U, "ha4506ceb".U, "hbef9a3f7".U, "hc67178f2".U
  ))

  // State & registers
  val H = RegInit(Hinit)
  val W = Reg(Vec(64, UInt(32.W)))
  val a = Reg(UInt(32.W)); val b = Reg(UInt(32.W)); val c = Reg(UInt(32.W))
  val d = Reg(UInt(32.W)); val e = Reg(UInt(32.W)); val f = Reg(UInt(32.W))
  val g = Reg(UInt(32.W)); val h = Reg(UInt(32.W))
  val round = RegInit(0.U(6.W))
  val firstHash = Reg(UInt(256.W))

  val sIdle :: sRun :: sFinalize :: sSecondSetup :: sSecondRun :: sSecondFinalize :: sDone :: Nil = Enum(7)
  val state = RegInit(sIdle)

  io.done := false.B
  io.out := Cat(H(0), H(1), H(2), H(3), H(4), H(5), H(6), H(7))

  switch(state) {
    is(sIdle) {
      when(io.start) {
        for(i <- 0 until 8) { H(i) := Hinit(i) }
        for(i <- 0 until 16) { W(i) := io.in(511 - i*32, 480 - i*32) }
        for(i <- 16 until 64) { W(i) := 0.U }
        a := Hinit(0); b := Hinit(1); c := Hinit(2); d := Hinit(3)
        e := Hinit(4); f := Hinit(5); g := Hinit(6); h := Hinit(7)
        round := 0.U
        state := sRun
      }
    }

    is(sRun) {
      // Round i
      val Wi = Wire(UInt(32.W))
      when(round < 16.U) {
        Wi := W(round)
      }.otherwise {
        val s0 = (W(round - 15.U).rotateRight(7)) ^ (W(round - 15.U).rotateRight(18)) ^ (W(round - 15.U) >> 3).asUInt
        val s1 = (W(round - 2.U).rotateRight(17)) ^ (W(round - 2.U).rotateRight(19)) ^ (W(round - 2.U) >> 10).asUInt
        val newW = W(round - 16.U) + s0 + W(round - 7.U) + s1
        Wi := newW
        W(round) := newW
      }

      // Compute first round
      val S1a = e.rotateRight(6) ^ e.rotateRight(11) ^ e.rotateRight(25)
      val Cha = (e & f) ^ ((~e).asUInt & g)
      val T1a = h + S1a + Cha + K(round) + Wi
      val S0a = a.rotateRight(2) ^ a.rotateRight(13) ^ a.rotateRight(22)
      val Maja = (a & b) ^ (a & c) ^ (b & c)
      val T2a = S0a + Maja

      val na = T1a + T2a
      val nb = a
      val nc = b
      val nd = c
      val ne = d + T1a
      val nf = e
      val ng = f
      val nh = g

      // ---- Round i+1 ----
      val nextRound = round + 1.U
      val Wi1 = Wire(UInt(32.W))
      when(nextRound < 16.U) {
        Wi1 := W(nextRound)
      }.otherwise {
        val s0b = (W(nextRound - 15.U).rotateRight(7)) ^ (W(nextRound - 15.U).rotateRight(18)) ^ (W(nextRound - 15.U) >> 3).asUInt
        val s1b = (W(nextRound - 2.U).rotateRight(17)) ^ (W(nextRound - 2.U).rotateRight(19)) ^ (W(nextRound - 2.U) >> 10).asUInt
        val newW1 = W(nextRound - 16.U) + s0b + W(nextRound - 7.U) + s1b
        Wi1 := newW1
        W(nextRound) := newW1
      }

      val S1b = ne.rotateRight(6) ^ ne.rotateRight(11) ^ ne.rotateRight(25)
      val Chb = (ne & nf) ^ ((~ne).asUInt & ng)
      val T1b = nh + S1b + Chb + K(nextRound) + Wi1
      val S0b = na.rotateRight(2) ^ na.rotateRight(13) ^ na.rotateRight(22)
      val Majb = (na & nb) ^ (na & nc) ^ (nb & nc)
      val T2b = S0b + Majb

      val a2 = T1b + T2b
      val b2 = na
      val c2 = nb
      val d2 = nc
      val e2 = nd + T1b
      val f2 = ne
      val g2 = nf
      val h2 = ng

      // Register update after two rounds
      a := a2; b := b2; c := c2; d := d2
      e := e2; f := f2; g := g2; h := h2

      // Debug code for simulation
      if (debug) {
        printf(p"round=$round a=${Hexadecimal(a)} b=${Hexadecimal(b)} c=${Hexadecimal(c)} d=${Hexadecimal(d)} e=${Hexadecimal(e)} f=${Hexadecimal(f)} g=${Hexadecimal(g)} h=${Hexadecimal(h)}\n")
      }

      when(round >= 62.U) {
        state := sFinalize
      }.otherwise {
        round := round + 2.U
      }
    }

    is(sFinalize) {
      firstHash := Cat(
        H(0) + a,
        H(1) + b,
        H(2) + c,
        H(3) + d,
        H(4) + e,
        H(5) + f,
        H(6) + g,
        H(7) + h
      )
      state := sSecondSetup
    }
    is(sSecondSetup) {
      // store the first hash result

      // prepare padded block for second hash
      val secondInput = Cat(firstHash, "h8000000000000000000000000000000000000000000000000000000000000100".U(256.W))
      for(i <- 0 until 8) { H(i) := Hinit(i) }
      for(i <- 0 until 16) { W(i) := secondInput(511 - i*32, 480 - i*32) }
      for(i <- 16 until 64) { W(i) := 0.U }
      a := Hinit(0); b := Hinit(1); c := Hinit(2); d := Hinit(3)
      e := Hinit(4); f := Hinit(5); g := Hinit(6); h := Hinit(7)
      round := 0.U
      state := sSecondRun
    }

    is(sSecondRun) {
      // Round i
      val Wi = Wire(UInt(32.W))
      when(round < 16.U) {
        Wi := W(round)
      }.otherwise {
        val s0 = (W(round - 15.U).rotateRight(7)) ^ (W(round - 15.U).rotateRight(18)) ^ (W(round - 15.U) >> 3).asUInt
        val s1 = (W(round - 2.U).rotateRight(17)) ^ (W(round - 2.U).rotateRight(19)) ^ (W(round - 2.U) >> 10).asUInt
        val newW = W(round - 16.U) + s0 + W(round - 7.U) + s1
        Wi := newW
        W(round) := newW
      }

      // Compute first round
      val S1a = e.rotateRight(6) ^ e.rotateRight(11) ^ e.rotateRight(25)
      val Cha = (e & f) ^ ((~e).asUInt & g)
      val T1a = h + S1a + Cha + K(round) + Wi
      val S0a = a.rotateRight(2) ^ a.rotateRight(13) ^ a.rotateRight(22)
      val Maja = (a & b) ^ (a & c) ^ (b & c)
      val T2a = S0a + Maja

      val na = T1a + T2a
      val nb = a
      val nc = b
      val nd = c
      val ne = d + T1a
      val nf = e
      val ng = f
      val nh = g

      // ---- Round i+1 ----
      val nextRound = round + 1.U
      val Wi1 = Wire(UInt(32.W))
      when(nextRound < 16.U) {
        Wi1 := W(nextRound)
      }.otherwise {
        val s0b = (W(nextRound - 15.U).rotateRight(7)) ^ (W(nextRound - 15.U).rotateRight(18)) ^ (W(nextRound - 15.U) >> 3).asUInt
        val s1b = (W(nextRound - 2.U).rotateRight(17)) ^ (W(nextRound - 2.U).rotateRight(19)) ^ (W(nextRound - 2.U) >> 10).asUInt
        val newW1 = W(nextRound - 16.U) + s0b + W(nextRound - 7.U) + s1b
        Wi1 := newW1
        W(nextRound) := newW1
      }

      val S1b = ne.rotateRight(6) ^ ne.rotateRight(11) ^ ne.rotateRight(25)
      val Chb = (ne & nf) ^ ((~ne).asUInt & ng)
      val T1b = nh + S1b + Chb + K(nextRound) + Wi1
      val S0b = na.rotateRight(2) ^ na.rotateRight(13) ^ na.rotateRight(22)
      val Majb = (na & nb) ^ (na & nc) ^ (nb & nc)
      val T2b = S0b + Majb

      val a2 = T1b + T2b
      val b2 = na
      val c2 = nb
      val d2 = nc
      val e2 = nd + T1b
      val f2 = ne
      val g2 = nf
      val h2 = ng

      // Register update after two rounds
      a := a2; b := b2; c := c2; d := d2
      e := e2; f := f2; g := g2; h := h2

      // Debug code for simulation
      if (debug) {
        printf(p"round=$round a=${Hexadecimal(a)} b=${Hexadecimal(b)} c=${Hexadecimal(c)} d=${Hexadecimal(d)} e=${Hexadecimal(e)} f=${Hexadecimal(f)} g=${Hexadecimal(g)} h=${Hexadecimal(h)}\n")
      }

      when(round >= 62.U) {
        state := sSecondFinalize
      }.otherwise {
        round := round + 2.U
      }
    }


    is(sSecondFinalize) {
      for(i <- 0 until 8) { H(i) := H(i) + Seq(a,b,c,d,e,f,g,h)(i) }
      state := sDone
    }

    is(sDone) {
      io.done := true.B
      when(!io.start) { state := sIdle }
    }
  }
}
