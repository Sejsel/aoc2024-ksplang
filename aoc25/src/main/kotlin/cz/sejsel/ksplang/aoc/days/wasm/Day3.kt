package cz.sejsel.ksplang.aoc.days.wasm

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import java.io.File
import kotlin.io.path.Path

// Day 3
// https://adventofcode.com/2025/day/3
fun main() {
    // Rebuild WASM files just to be sure
    rebuildAllWasm()

    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(wasmDay3Part1())
    File("aoc25/ksplang/wasm/3-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/wasm/3-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 3 part 1")

    val program2 = builder.buildAnnotated(wasmDay3Part2())
    File("aoc25/ksplang/wasm/3-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/wasm/3-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 3 part 2")
}

fun wasmDay3Part1(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_3_1.wasm"), "solve")
fun wasmDay3Part2(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_3_2.wasm"), "solve")