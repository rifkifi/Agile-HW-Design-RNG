import chisel3._
import chisel3.util._

class ChaCha extends Module {
  val io = IO(new CipherIO(512))

  ///-------------------------------------------Helper functions--------------------------------------------------------
  ///--------------------------------outside class allowed from scala 3-------------------------------------------------

  def swap32(u: UInt) = Cat(u(7, 0), u(15, 8), u(23, 16), u(31, 24))


  private def RoL32(x: UInt, n: Int): UInt = {
    require(x.getWidth == 32, s"rol32 expects a 32-bit input, got ${x.getWidth}")
    ((x << n) | (x >> (32 - n)))(31, 0)
  }

  private def quater_round(a: UInt, b: UInt, c: UInt, d: UInt): (UInt, UInt, UInt, UInt) = {

    val a_in = a(31, 0)
    val b_in = b(31, 0)
    val c_in = c(31, 0)
    val d_in = d(31, 0)

    val temp_a_1 = (a_in + b_in)
    val d_out_1 = (RoL32((d_in ^ temp_a_1), 16))

    val temp_c_1 = (c_in + d_out_1)
    val b_out_1 = (RoL32(b_in ^ temp_c_1, 12))

    val temp_a =  (temp_a_1 + b_out_1)
    val d_out = (RoL32((d_out_1 ^ temp_a), 8))

    val temp_c = temp_c_1 + d_out
    val b_out = (RoL32((b_out_1 ^ temp_c), 7))

    (temp_a, b_out, temp_c, d_out)
  }

  ///-------------------------------------------loading values--------------------------------------------------------

  // Build 4x4 matrix of 32-bit registers
  val matrix_4x4 = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(4)(0.U(32.W))))))
  val ctr_itr = RegInit(0.U(4.W))

  // Endianess kept in mind
  //chisel is UInt while salsa20 requires little endian
  val key7 = swap32(io.in_key(31, 0))
  val key6 = swap32(io.in_key(63, 32))
  val key5 = swap32(io.in_key(95, 64))
  val key4 = swap32(io.in_key(127, 96))
  val key3 = swap32(io.in_key(159, 128))
  val key2 = swap32(io.in_key(191, 160))
  val key1 = swap32(io.in_key(223, 192))
  val key0 = swap32(io.in_key(255, 224))

  // 2 instances for nonce
  val nonce1 = swap32(io.in_nonce(31, 0))
  val nonce0 = swap32(io.in_nonce(63, 32))

  // 2 instances for ctr
  val ctr1 = io.in_counter(31, 0)
  val ctr0 = io.in_counter(63, 32)

  // 4 instances of reg32 for constant words
  private val constant_word_0 = "h61707865".U(32.W) // "expa"
  private val constant_word_1 = "h3320646E".U(32.W) // "nd 3"
  private val constant_word_2 = "h79622D32".U(32.W)// "2-by"
  private val constant_word_3 = "h6B206574".U(32.W) // "te k"


  ///-------------------------------------------FSM--------------------------------------------------------

  val sIdle :: sIterate :: sDone :: Nil = Enum(3)
  val state = RegInit(sIdle)

  //default value
  io.done := false.B

  val flattened = Seq(
    matrix_4x4(0)(0), matrix_4x4(0)(1), matrix_4x4(0)(2), matrix_4x4(0)(3), // column 0
    matrix_4x4(1)(0), matrix_4x4(1)(1), matrix_4x4(1)(2), matrix_4x4(1)(3), // column 1
    matrix_4x4(2)(0), matrix_4x4(2)(1), matrix_4x4(2)(2), matrix_4x4(2)(3), // column 2
    matrix_4x4(3)(0), matrix_4x4(3)(1), matrix_4x4(3)(2), matrix_4x4(3)(3)  // column 3
  )

  io.out := Cat(flattened)  // reverse to match MSB→LSB ordering in io.out

  //fsm
  switch(state) {

    is(sIdle) {
      when(io.start) {
        //row0
        matrix_4x4(0)(0) := constant_word_0
        matrix_4x4(0)(1) := constant_word_1
        matrix_4x4(0)(2) := constant_word_2
        matrix_4x4(0)(3) := constant_word_3
        //row1
        matrix_4x4(1)(0) := key0
        matrix_4x4(1)(1) := key1
        matrix_4x4(1)(2) := key2
        matrix_4x4(1)(3) := key3
        //row2
        matrix_4x4(2)(0) := key4
        matrix_4x4(2)(1) := key5
        matrix_4x4(2)(2) := key6
        matrix_4x4(2)(3) := key7
        //row3
        matrix_4x4(3)(0) := ctr0
        matrix_4x4(3)(1) := ctr1
        matrix_4x4(3)(2) := nonce0
        matrix_4x4(3)(3) := nonce1

        state := sIterate
      }
    }



    is(sIterate) {
      //for coloumn first
      val (y0, y4, y8, y12) = quater_round(matrix_4x4(0)(0), matrix_4x4(1)(0), matrix_4x4(2)(0), matrix_4x4(3)(0))
      val (y1, y5, y9, y13) = quater_round(matrix_4x4(0)(1), matrix_4x4(1)(1), matrix_4x4(2)(1), matrix_4x4(3)(1))
      val (y2, y6, y10, y14) = quater_round(matrix_4x4(0)(2), matrix_4x4(1)(2), matrix_4x4(2)(2), matrix_4x4(3)(2))
      val (y3, y7, y11, y15) = quater_round(matrix_4x4(0)(3), matrix_4x4(1)(3), matrix_4x4(2)(3), matrix_4x4(3)(3))

      //for now rows
      /*
        Row 0 → (x0,  x1,  x2,  x3)
        Row 1 → (x5,  x6,  x7,  x4)
        Row 2 → (x10, x11, x8,  x9)
        Row 3 → (x15, x12, x13, x14)
         */

      val (x0_new, x5_new, x10_new, x15_new) = quater_round(y0, y5, y10, y15)
      val (x1_new, x6_new, x11_new, x12_new) = quater_round(y1, y6, y11, y12)
      val (x2_new, x7_new, x8_new, x13_new) = quater_round(y2, y7, y8, y13)
      val (x3_new, x4_new, x9_new, x14_new) = quater_round(y3, y4, y9, y14)


      matrix_4x4(0)(0) := x0_new
      matrix_4x4(0)(1) := x1_new
      matrix_4x4(0)(2) := x2_new
      matrix_4x4(0)(3) := x3_new
      //row1
      matrix_4x4(1)(0) := x4_new
      matrix_4x4(1)(1) := x5_new
      matrix_4x4(1)(2) := x6_new
      matrix_4x4(1)(3) := x7_new
      //row2
      matrix_4x4(2)(0) := x8_new
      matrix_4x4(2)(1) := x9_new
      matrix_4x4(2)(2) := x10_new
      matrix_4x4(2)(3) := x11_new
      //row3
      matrix_4x4(3)(0) := x12_new
      matrix_4x4(3)(1) := x13_new
      matrix_4x4(3)(2) := x14_new
      matrix_4x4(3)(3) := x15_new

      ctr_itr := ctr_itr + 1.U

      when(ctr_itr === 9.U){
        state := sDone
      }
    }


    is(sDone) {
      io.done := true.B
      ctr_itr := 0.U
      when(!io.start) { state := sIdle }
    }
  }
}

object ChaCha {
  val outWidth = 128
}