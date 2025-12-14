package cz.sejsel.ksplang.aoc.days.wasm

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import java.io.File
import kotlin.io.path.Path

// Day 5
// https://adventofcode.com/2025/day/5
fun main() {
    // Rebuild WASM files just to be sure
    rebuildAllWasm()

    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(wasmDay5Part1())
    File("aoc25/ksplang/wasm/5-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/wasm/5-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 5 part 1")

    val program2 = builder.buildAnnotated(wasmDay5Part2())
    File("aoc25/ksplang/wasm/5-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/wasm/5-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 5 part 2")
}

fun wasmDay5Part1(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_5_1.wasm"), "solve")
fun wasmDay5Part2(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_5_2.wasm"), "solve")