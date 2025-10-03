**Overview**
- Parametric Linear-Feedback Shift Register (LFSR) in Chisel.
- Fibonacci form, shifts left by 1 each cycle and inserts XOR feedback into the LSB.
- Always includes the MSB as a tap; you pass additional taps relative to bit 0.

**Module**
- Class: `LFSR(n: Int = 4, tap_in: Seq[Int] = Seq(0), seed: Int = 1)`
- File: `lfsr/src/main/scala/Main.scala`
- I/O:
  - `io.out: UInt(n.W)` — current state.
- Parameters:
  - `n` — width in bits.
  - `tap_in` — extra tap indices; MSB (`n-1`) is always included.
  - `seed` — non-zero initial state; must satisfy `1 <= seed < 2^n`.

**Next-State Rule**
- Feedback: XOR of `lfsr(i)` for all `i` in `tap_in ∪ {n-1}`.
- Update: `lfsr := Cat(lfsr(n-2, 0), feedback)`

**Example (n = 4, taps = Seq(1))**
- Effective taps: `{3, 1}` (MSB 3 is added automatically).
- From `seed = 1`, sequence: `1, 2, 5, 10, 4, 8, 1, 2, ...`

**Project Layout**
- Sources: `lfsr/src/main/scala/Main.scala`
- Test/driver: `lfsr/src/test/scala/LFSRTest.scala`

**Build & Test**
- Requirements: JDK 11+, `sbt`.
- Run tests and print the first 20 states:
  - `cd lfsr`
  - `sbt test`
- The test constructs the module (`new LFSR(16, Seq(1), 1)` by default), steps the clock, and prints the observed sequence.

**Customize**
- Change width/taps/seed by editing the instantiation in `lfsr/src/test/scala/LFSRTest.scala:7`.
- For a 4-bit LFSR with taps at 1 (plus MSB) and seed 1: `new LFSR(4, Seq(1), 1)`.

**Emit SystemVerilog**
- A generator is provided at `lfsr/src/main/scala/Emit.scala`.
- Examples (from `lfsr`):
  - `sbt "runMain LFSREmit --n 4 --taps 1 --seed 1 --target-dir generated"`
  - `sbt "runMain LFSREmit --n 16 --taps 1,2,4,6 --seed 1 --target-dir out"`
- Output: SystemVerilog files under the specified `--target-dir` (one `.sv` per module).
