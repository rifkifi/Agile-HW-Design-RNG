import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Arrays
import java.lang.{Character => JCharacter}

class CoSimulationTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AES256 co-simulation with java built-in AES256 implementation"

  it should "generate the same output" in {
    test(new AES256()) { dut =>
      println("Testing AES256 functionality")
      val plainTexts =
        Seq("abc", "hello world!", "Chisel AES256", "Agile HW Design")
      val keyHex =
        "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4"
      val hardware = new AES256Hardware(dut)
      val goldenModel = new AES256GoldenModel()

      for (plainText <- plainTexts) {
        println(s"Testing plaintext: '$plainText'")
        val block = AES256Helper.pkcs7PadMessage(plainText)
        val ptHex = block.toString(16)
        val hardwareResult = hardware.result(keyHex, block)
        val modelResult = goldenModel.result(keyHex, ptHex)
        println(s"PT=$plainText, HW=$hardwareResult, Model=$modelResult")
        assert(
          hardwareResult == modelResult,
          s"Mismatch for plaintext '$plainText': HW=$hardwareResult, Model=$modelResult"
        )
      }
      println("All tests passed!\n\n")
    }
  }

  behavior of "SHA256 co-simulation with java built-in SHA-256 implementation"
  it should "generate the same hash output" in {
    test(new SHA256()) { dut =>
      println("Testing SHA256 functionality")
      val messages =
        Seq("abc", "hello world!", "Chisel SHA256", "Agile HW Design")
      val goldenModel = new SHA256GoldenModel()
      val hardware = new SHA256Hardware(dut)
      for (message <- messages) {
        println(s"Testing message: '$message'")
        val hardwareHash = hardware.hash(message)
        val modelHash = goldenModel.hash(message)
        println(s"PT=$message, HW=$hardwareHash, Model=$modelHash")
        assert(
          hardwareHash == modelHash,
          s"Mismatch for message '$message': HW=$hardwareHash, Model=$modelHash"
        )
      }
      println("All tests passed!\n\n")
    }
  }

  behavior of "SHAd256 co-simulation with java built-in SHA-256 implementation"
  it should "generate the same hash output" in {
    test(new SHAd256()) { dut =>
      println("Testing SHAd256 functionality")
      val messages =
        Seq("abc", "hello world!", "Chisel SHA256", "Agile HW Design")
      val goldenModel = new SHAd256GoldenModel()
      val hardware = new SHAd256Hardware(dut)
      for (message <- messages) {
        println(s"Testing message: '$message'")
        val hardwareHash = hardware.hash(message)
        val modelHash = goldenModel.hash(message)
        println(s"PT=$message, HW=$hardwareHash, Model=$modelHash")
        assert(
          hardwareHash == modelHash,
          s"Mismatch for message '$message': HW=$hardwareHash, Model=$modelHash"
        )
      }
      println("All tests passed!\n\n")
    }
  }
}

// AES256 Implementation trait
trait AES256Impl[T] {
  def result(key32: String, block16: T): String
}

// AES256 Hardware Implementation
class AES256Hardware(dut: AES256) extends AES256Impl[BigInt] {
  def result(key32: String, block16: BigInt): String = {
    // println(s"Running hardware AES256 with block=${block16.toString(16)}")
    // DUT Processing
    while (!dut.io.ready.peek().litToBoolean) { dut.clock.step(1) }
    dut.io.start.poke(true.B)
    dut.io.in_key.poke(
      s"h${key32}".U(
        256.W
      )
    )
    dut.io.in_data.poke(block16.U(128.W))
    dut.clock.step()
    while (!dut.io.done.peek().litToBoolean) { dut.clock.step(1) }
    val res = dut.io.out.peek().litValue.toString(16)
    dut.clock.step()
    dut.io.start.poke(false.B)
    res
  }
}

// AES256 Java Golden Model
class AES256GoldenModel extends AES256Impl[String] {

  def result(key32: String, block16: String): String = {
    val keyBytes = hexToBytes(key32)
    val ptBytes = hexToBytes(block16)
    val modelOutBytes = encryptBlock(keyBytes, ptBytes)
    bytesToHex(modelOutBytes)
  }
  // Encrypt exactly one 16-byte block
  def encryptBlock(key32: Array[Byte], block16: Array[Byte]): Array[Byte] = {
    requireLen(key32, 32, "key")
    requireLen(block16, 16, "block")
    val c = Cipher.getInstance("AES/ECB/NoPadding")
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key32, "AES"))
    c.doFinal(block16)
  }

  private def requireLen(b: Array[Byte], len: Int, what: String): Unit = {
    if (b == null || b.length != len)
      throw new IllegalArgumentException(s"$what must be $len bytes")
  }

  // Hex helpers
  def hexToBytes(s: String): Array[Byte] = {
    require((s.length & 1) == 0, "hex length must be even")
    val out = new Array[Byte](s.length / 2)
    var i = 0
    while (i < out.length) {
      val hi = JCharacter.digit(s.charAt(2 * i), 16)
      val lo = JCharacter.digit(s.charAt(2 * i + 1), 16)
      if (hi < 0 || lo < 0) throw new IllegalArgumentException("invalid hex")
      out(i) = ((hi << 4) | lo).toByte
      i += 1
    }
    out
  }

  def bytesToHex(b: Array[Byte]): String = {
    val digits = "0123456789abcdef".toCharArray
    val hex = new Array[Char](b.length * 2)
    var i = 0
    while (i < b.length) {
      val v = b(i) & 0xff
      hex(2 * i) = digits(v >>> 4)
      hex(2 * i + 1) = digits(v & 0x0f)
      i += 1
    }
    new String(hex)
  }
}

// SHA256 Implementation trait
trait SHA256Impl {
  def hash(input: String): String
}

class SHA256GoldenModel extends SHA256Impl {
  def hash(input: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val hashBytes = md.digest(input.getBytes("UTF-8"))
    val hashBigInt = BigInt(1, hashBytes)
    hashBigInt.toString(16)
  }
}

class SHA256Hardware(dut: SHA256) extends SHA256Impl {
  def hash(input: String): String = {
    val paddedMessage = SHA256Helper.padMessage(input)
    dut.io.in.poke(paddedMessage.U(512.W)) // important: 512 bits wide
    dut.io.start.poke(true.B)
    dut.clock.step()
    dut.io.start.poke(false.B)
    while (!dut.io.done.peek().litToBoolean) { dut.clock.step(1) }
    val digest = dut.io.out.peek().litValue
    dut.clock.step()
    digest.toString(16)
  }
}

class SHAd256GoldenModel extends SHA256Impl {
  def hash(input: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val hashBytes = md.digest(input.getBytes("UTF-8"))
    val secondHashBytes = md.digest(hashBytes)
    val hashBigInt = BigInt(1, secondHashBytes)
    hashBigInt.toString(16)
  }
}

class SHAd256Hardware(dut: SHAd256) extends SHA256Impl {
  def hash(input: String): String = {
    val paddedMessage = SHA256Helper.padMessage(input)
    dut.io.in.poke(paddedMessage.U(512.W)) // important: 512 bits wide
    dut.io.start.poke(true.B)
    dut.clock.step()
    dut.io.start.poke(false.B)
    while (!dut.io.done.peek().litToBoolean) { dut.clock.step(1) }
    val digest = dut.io.out.peek().litValue
    dut.clock.step()
    digest.toString(16)
  }
}

object SHA256Helper {
  def padMessage(message: String): BigInt = {
    val messageB = message.getBytes("UTF-8")
    val messageBytes = messageB.toArray
    val messageLenBits = messageBytes.length * 8L
    val paddedHex = new StringBuilder
    messageBytes.foreach(b => paddedHex.append(f"${b & 0xff}%02x"))
    paddedHex.append("80")
    val currentLenBytes = paddedHex.length / 2
    val zerosNeeded = 56 - currentLenBytes
    paddedHex.append("00" * zerosNeeded)
    paddedHex.append(f"${messageLenBits}%016x")
    BigInt(paddedHex.toString, 16)
  }
}
