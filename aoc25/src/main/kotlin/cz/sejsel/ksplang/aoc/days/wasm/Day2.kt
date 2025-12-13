package cz.sejsel.ksplang.aoc.days.wasm

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import java.io.File
import kotlin.io.path.Path

// Day 2
// https://adventofcode.com/2025/day/2
fun main() {
    // Rebuild WASM files just to be sure
    rebuildAllWasm()

    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(wasmDay2Part1())
    File("aoc25/ksplang/wasm/2-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/wasm/2-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 2 part 1")

    /*
    val program2 = builder.buildAnnotated(wasmDay2Part2())
    File("aoc25/ksplang/wasm/2-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/wasm/2-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 2 part 2")
     */
}

fun wasmDay2Part1(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_2_1.wasm"), "solve")
fun wasmDay2Part2(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_2_2.wasm"), "solve")