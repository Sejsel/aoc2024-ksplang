#!/bin/sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

RUSTFLAGS=-Ctarget-cpu=mvp cargo +nightly build --release -Zbuild-std=panic_abort,std --target wasm32-unknown-unknown
cp target/wasm32-unknown-unknown/release/aoc25_1.wasm "$SCRIPT_DIR/wasm/aoc25_1.wasm"
