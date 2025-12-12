package cz.sejsel.ksplang.aoc.days.wasm

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import java.io.File
import kotlin.io.path.Path

// Day 1
// https://adventofcode.com/2025/day/1
fun main() {
    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(wasmDay1Part1())
    File("aoc25/ksplang/wasm/1-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/wasm/1-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 1 part 1")

    val program2 = builder.buildAnnotated(wasmDay1Part2())
    File("aoc25/ksplang/wasm/1-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/wasm/1-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 1 part 2")
}

fun wasmDay1Part1(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_1_1.wasm"), "solve")
fun wasmDay1Part2(): KsplangProgram = buildWasmSingleValueProgram(Path("aoc25/rust/wasm/aoc25_1_2.wasm"), "solve")
