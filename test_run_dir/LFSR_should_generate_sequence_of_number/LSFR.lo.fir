FIRRTL version 1.2.0
circuit LSFR :
  module LSFR : @[\\src\\main\\scala\\Main.scala 6:7]
    input clock : Clock @[\\src\\main\\scala\\Main.scala 6:7]
    input reset : UInt<1> @[\\src\\main\\scala\\Main.scala 6:7]
    input io_en : UInt<1> @[\\src\\main\\scala\\Main.scala 7:14]
    input io_seed : UInt<4> @[\\src\\main\\scala\\Main.scala 7:14]
    output io_out : UInt<4> @[\\src\\main\\scala\\Main.scala 7:14]

    reg res : UInt<4>, clock with :
      reset => (UInt<1>("h0"), res) @[\\src\\main\\scala\\Main.scala 13:20]
    node _nxt_res_T = bits(res, 1, 1) @[\\src\\main\\scala\\Main.scala 17:26]
    node _nxt_res_T_1 = bits(res, 3, 3) @[\\src\\main\\scala\\Main.scala 17:33]
    node _nxt_res_T_2 = xor(_nxt_res_T, _nxt_res_T_1) @[\\src\\main\\scala\\Main.scala 17:29]
    node _nxt_res_T_3 = bits(res, 3, 1) @[\\src\\main\\scala\\Main.scala 17:41]
    node nxt_res = cat(_nxt_res_T_2, _nxt_res_T_3) @[\\src\\main\\scala\\Main.scala 17:22]
    node _GEN_0 = mux(io_en, nxt_res, io_seed) @[\\src\\main\\scala\\Main.scala 16:16 14:7 18:9]
    io_out <= res @[\\src\\main\\scala\\Main.scala 20:10]
    res <= mux(reset, UInt<4>("h1"), _GEN_0) @[\\src\\main\\scala\\Main.scala 13:{20,20}]
