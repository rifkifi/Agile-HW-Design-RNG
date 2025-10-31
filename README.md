# Fortuna Random Number Generation

**Overview**
- AES‑256, SHA‑256, and SHAd256 (double SHA‑256) hardware cores in Chisel.
- Targets Scala 2.13, Chisel 6.7.0, and chiseltest 6.0.0.

**Project Layout**
- Sources: `Agile-HW-Design-RNG/src/main/scala/`
  - `AES256.scala` — AES‑256 ECB core.
  - `SHA256.scala` — Single‑block SHA‑256 core (iterative 64 rounds).
  - `SHAd256.scala` — Double SHA‑256 core: SHA‑256(SHA‑256(msg)).
  - `Emit.scala` — `runMain` emitters for SystemVerilog generation.
  - `Pools.scala` — Simple seeding pools component for RNG reseeding logic.
- Tests: `Agile-HW-Design-RNG/src/test/scala/`
  - `AES256Test.scala` — AES unit test with PKCS#7 padding and known vector.
  - `CoSimulationTest.scala` — Co‑simulation against Java golden models (AES, SHA256, SHAd256).
  - `SHA256Tester.scala` — Single‑block SHA‑256 “abc” known‑answer test.
  - `SHAd256Tester.scala` — Double SHA‑256 “abc” known‑answer test.
  - `PoolsTester.scala` — Pools module sanity test.

**SHA256 Module**
- IO (file: `Agile-HW-Design-RNG/src/main/scala/SHA256.scala:1`)
  - `io.in: UInt(512.W)` — pre‑padded 512‑bit message block.
  - `io.start: Bool` — start pulse; sample `io.in` and begin hashing.
  - `io.ready: Bool` — high in idle; ready for a new block.
  - `io.done: Bool` — high when digest is valid.
  - `io.out: UInt(256.W)` — hashed message
- Operation
  - Initializes `H` to SHA‑256 IV constants and expands the message schedule `W[0..63]`.
  - Iterative FSM: `sIdle → sRun (round 0..63) → sFinalize → sDone`.
  - Each round updates working vars `a..h` with `Σ0/Σ1`, `Ch`, `Maj`, and `K[i] + W[i]`.
  - Finalizes by adding `a..h` into `H0..H7` and concatenates to `io.out`.

**SHAd256 Module**
- IO (file: `Agile-HW-Design-RNG/src/main/scala/SHAd256.scala:1`)
  - Same interface as `SHA256` (`in/start/ready/done/out`).
- Operation
  - Pass 1: Hash the input block to produce `firstHash` (256 bits).
  - Pass 2: Build a second 512‑bit block as `firstHash ‖ 0x80 ‖ zeros ‖ 0x000...0100` and hash again.
  - FSM: `sIdle → sRun → sFinalize → sSecondSetup → sSecondRun → sSecondFinalize → sDone`.
  - Output is the second pass digest on `io.out`.

**AES256 Module**
- IO (file: `Agile-HW-Design-RNG/src/main/scala/AES256.scala:1`)
  - `io.in_data: UInt(128.W)` — plaintext block.
  - `io.in_key: UInt(256.W)` — cipher key.
  - `io.start: Bool`, `io.ready: Bool`, `io.done: Bool` — handshake.
  - `io.out: UInt(128.W)` — ciphertext block.
- Operation
  - Implements key expansion for AES‑256 and a 14‑round pipeline using standard AES steps that applies SubBytes, ShiftRows, MixColumns, and AddRoundKey function to the input block (plaintext).

**Tests Modules**
- `Agile-HW-Design-RNG/src/test/scala/AES256Test.scala:1`
  - Pads "abc" to a single 16‑byte block via PKCS#7 and checks the ciphertext for a known key.
  - Uses `AES256Helper.pkcs7PadMessage` to produce a `BigInt` suitable for `UInt(128.W)`.
- `Agile-HW-Design-RNG/src/test/scala/SHA256Tester.scala:1`
  - Pokes the padded "abc" block and checks against the known SHA‑256 digest.
- `Agile-HW-Design-RNG/src/test/scala/SHAd256Tester.scala:1`
  - Same flow as SHA256, but compares against the double‑hash digest
- `Agile-HW-Design-RNG/src/test/scala/CoSimulationTest.scala:1`
  - AES256: Compares DUT results to Java built-in AES256 ECB for multiple plaintexts.
  - SHA256: Compares DUT results to Java built-in SHA256 for multiple messages.
  - SHAd256: Runs double hashing and compares to twofold Java built-in SHA256 output.
- `Agile-HW-Design-RNG/src/test/scala/PoolsTester.scala:1`
  - Sanity checks mask‑based pool selection and reseed behavior in `Pools`.

**Emit Examples**
- Run from `Agile-HW-Design-RNG` directory with sbt:
  - `sbt "runMain AES256Emit"` → emits SV for AES256 into `generated/`.
  - `sbt "runMain SHA256Emit"` → emits SV for SHA256 into `generated/`.
  - `sbt "runMain SHAd256Emit"` → emits SV for SHAd256 into `generated/`.
- Emitters are defined in `Agile-HW-Design-RNG/src/main/scala/Emit.scala:1`.

**Build & Test**
- Requirements: JDK 17+, `sbt`.
- Run all tests: `sbt test`
- Run a specific test: `sbt "testOnly *SHA256Tester"`, `sbt "testOnly *SHAd256Tester"`, or `sbt "testOnly *AES256Test"`.


