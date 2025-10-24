**Overview**
- AES‑256 core in Chisel with basic round functions (SubBytes, ShiftRows, MixColumns, AddRoundKey).
- Includes a parametric Linear‑Feedback Shift Register (LFSR) and a small emitter utility.
- Uses Scala 2.13, Chisel 6.7.0, and chiseltest 6.0.0.

**Project Layout**
- Sources: `src/main/scala/`
  - `AES256.scala` — AES‑256 module and helpers.
  - `LFSR.scala` — parametric LFSR module.
  - `Emit.scala` — simple SV emission for LFSR.
- Tests: `src/test/scala/`
  - `AES256Test.scala` — basic AES test harness.
  - `LFSRTest.scala` — LFSR sequence sanity test.

**AES256 Module**
- IO (file: `src/main/scala/AES256.scala`)
  - `io.in_data: UInt(128.W)` — plaintext block.
  - `io.in_key: UInt(256.W)` — cipher key.
  - `io.start: Bool` — start pulse.
  - `io.done: Bool` — completion flag.
  - `io.out: UInt(256.W)` — debug/output bus (top packs state bytes when done).
  - `io.dt: UInt(8.W)` — small debug tap.
- Notes
  - Key expansion and round/state are registered; reads reflect updates on the next cycle.
  - Use combinational helpers (e.g., `shiftRows`, `mixColumns`) to compute next‑state, then assign to the state `Reg` in the FSM.

**Build & Test**
- Requirements: JDK 11+, `sbt`.
- Run all tests from repository root:
  - `sbt test`
- Run a specific test:
  - `sbt "testOnly *AES256Test"`
- Test tips (file: `src/test/scala/AES256Test.scala`)
  - If the test times out waiting for `done`, extend or disable the timeout:
    - `dut.clock.setTimeout(100000)` or `dut.clock.setTimeout(0)`.
  - Prefer a bounded wait to avoid infinite loops (poll `io.done` with a max iteration count).

**LFSR Module**
- Class: `LFSR(n: Int = 4, tap_in: Seq[Int] = Seq(0), seed: Int = 1)` (file: `src/main/scala/LFSR.scala`).
- IO: `io.out: UInt(n.W)` — current state.
- Behavior: Fibonacci form; shifts left by 1 each cycle, XOR feedback (MSB always tapped).

**Emit SystemVerilog (LFSR)**
- Object: `LFSREmit` (file: `src/main/scala/Emit.scala`).
- Examples:
  - `sbt "runMain LFSREmit --n 4 --taps 1 --seed 1 --target-dir generated"`
  - `sbt "runMain LFSREmit --n 16 --taps 1,2,4,6 --seed 1 --target-dir out"`
- Output: SystemVerilog in `--target-dir` (top: `LFSR.v`).

