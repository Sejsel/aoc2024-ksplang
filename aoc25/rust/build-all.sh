#!/bin/sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# MVP is the most basic WebAssembly target with no extra extensions required
# -Cpanic=immediate-abort is significantly better than panic_abort, panic_abort still uses memory for unwinding info;
#  immediate-abort just puts in an unreachable instruction in wasm
# note that we also have some of these settings in .cargo/config.toml and main workspace Cargo.toml, but these override it

RUSTFLAGS="-Ctarget-cpu=mvp -Zunstable-options -Cpanic=immediate-abort" cargo +nightly build --release -Zbuild-std=panic_abort,std --target wasm32-unknown-unknown
cp target/wasm32-unknown-unknown/release/aoc25_1_1.wasm "$SCRIPT_DIR/wasm/aoc25_1_1.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_1_2.wasm "$SCRIPT_DIR/wasm/aoc25_1_2.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_2_1.wasm "$SCRIPT_DIR/wasm/aoc25_2_1.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_2_2.wasm "$SCRIPT_DIR/wasm/aoc25_2_2.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_3_1.wasm "$SCRIPT_DIR/wasm/aoc25_3_1.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_3_2.wasm "$SCRIPT_DIR/wasm/aoc25_3_2.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_4_1.wasm "$SCRIPT_DIR/wasm/aoc25_4_1.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_4_2.wasm "$SCRIPT_DIR/wasm/aoc25_4_2.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_5_1.wasm "$SCRIPT_DIR/wasm/aoc25_5_1.wasm"
cp target/wasm32-unknown-unknown/release/aoc25_5_2.wasm "$SCRIPT_DIR/wasm/aoc25_5_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_6_1.wasm "$SCRIPT_DIR/wasm/aoc25_6_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_6_2.wasm "$SCRIPT_DIR/wasm/aoc25_6_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_7_1.wasm "$SCRIPT_DIR/wasm/aoc25_7_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_7_2.wasm "$SCRIPT_DIR/wasm/aoc25_7_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_8_1.wasm "$SCRIPT_DIR/wasm/aoc25_8_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_8_2.wasm "$SCRIPT_DIR/wasm/aoc25_8_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_9_1.wasm "$SCRIPT_DIR/wasm/aoc25_9_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_9_2.wasm "$SCRIPT_DIR/wasm/aoc25_9_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_10_1.wasm "$SCRIPT_DIR/wasm/aoc25_10_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_10_2.wasm "$SCRIPT_DIR/wasm/aoc25_10_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_11_1.wasm "$SCRIPT_DIR/wasm/aoc25_11_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_11_1.wasm "$SCRIPT_DIR/wasm/aoc25_11_1.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_12_2.wasm "$SCRIPT_DIR/wasm/aoc25_12_2.wasm"
#cp target/wasm32-unknown-unknown/release/aoc25_12_2.wasm "$SCRIPT_DIR/wasm/aoc25_12_2.wasm"