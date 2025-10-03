FIRRTL version 1.2.0
circuit LFSR :
  module LFSR : @[\\src\\main\\scala\\Main.scala 11:7]
    input clock : Clock @[\\src\\main\\scala\\Main.scala 11:7]
    input reset : UInt<1> @[\\src\\main\\scala\\Main.scala 11:7]
    output io_out : UInt<8> @[\\src\\main\\scala\\Main.scala 12:14]

    reg lfsr : UInt<8>, clock with :
      reset => (UInt<1>("h0"), lfsr) @[\\src\\main\\scala\\Main.scala 19:21]
    node _feedback_T = bits(lfsr, 1, 1) @[\\src\\main\\scala\\Main.scala 22:31]
    node _feedback_T_1 = bits(lfsr, 0, 0) @[\\src\\main\\scala\\Main.scala 22:31]
    node _feedback_T_2 = bits(lfsr, 7, 7) @[\\src\\main\\scala\\Main.scala 22:31]
    node _feedback_T_3 = xor(_feedback_T, _feedback_T_1) @[\\src\\main\\scala\\Main.scala 22:45]
    node feedback = xor(_feedback_T_3, _feedback_T_2) @[\\src\\main\\scala\\Main.scala 22:45]
    node _lfsr_T = bits(lfsr, 6, 0) @[\\src\\main\\scala\\Main.scala 25:19]
    node _lfsr_T_1 = cat(_lfsr_T, feedback) @[\\src\\main\\scala\\Main.scala 25:14]
    io_out <= lfsr @[\\src\\main\\scala\\Main.scala 27:10]
    lfsr <= mux(reset, UInt<8>("h1"), _lfsr_T_1) @[\\src\\main\\scala\\Main.scala 19:{21,21} 25:8]
