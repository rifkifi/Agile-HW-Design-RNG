import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AES256Test extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AES256" 
  
  it should "paddes and hashes input data correctly" in {
    test(new AES256()) { dut =>
      val block = AES256Helper.pkcs7PadMessage("abc")
      println(f"in data: ${block}%d")
      assert(
        block == BigInt("6162630d0d0d0d0d0d0d0d0d0d0d0d0d", 16),
        "Padded message does not match expected value"
      )
      while (!dut.io.ready.peek().litToBoolean) { dut.clock.step(1) }
      dut.io.start.poke(true.B)
      dut.io.in_key.poke(
        "h603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4".U(
          256.W
        )
      )
      dut.io.in_data.poke(block.U(128.W))
      dut.clock.step()
      while (!dut.io.done.peek().litToBoolean) { dut.clock.step(1) }
      println(f"Output: ${dut.io.out.peek().litValue}%x")
      
      val expected = "hF0E5A0466A99BCFFBAE804580F1E6A73".U
      dut.io.out.expect(expected)
      dut.clock.step()
      dut.io.start.poke(false.B)
    }
  }
}

// Helper functions
object AES256Helper{
  // Returns PKCS#7 padding length (0<pad<=16) for AES 16-byte blocks
  def pkcs7PadLen(message: String, blockSize: Int = 16): Int = {
    val len = message.getBytes("UTF-8").length
    val rem = len % blockSize
    if (rem == 0) blockSize else blockSize - rem
  }

  // Builds a single 16-byte PKCS#7-padded block as BigInt (MSB-first) for UInt(128.W)
  // Supports messages up to 16 bytes. Throws if longer.
  def pkcs7PadMessage(message: String, blockSize: Int = 16): BigInt = {
    require(blockSize == 16, "AES block size must be 16 bytes")
    val bytes = message.getBytes("UTF-8")
    require(bytes.length <= blockSize, s"Message too long for single block: ${bytes.length} bytes")
    val pad = pkcs7PadLen(message, blockSize)
    val padded = bytes ++ Array.fill(pad)(pad.toByte)
    padded.foldLeft(BigInt(0)) { (acc, b) => (acc << 8) | BigInt(b & 0xff) }
  }
}