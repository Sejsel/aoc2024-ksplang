#!/bin/sh

RUSTFLAGS=-Ctarget-cpu=mvp cargo +nightly build --release -Zbuild-std=panic_abort,std --target wasm32-unknown-unknown
cp target/wasm32-unknown-unknown/release/aoc25_1.wasm wasm/aoc25_1.wasm