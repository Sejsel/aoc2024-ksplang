package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 1
// https://adventofcode.com/2025/day/1
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day1Part1())
    File("aoc25/ksplang/1-1.ksplang").writeText(program)
    println("Generated program for day 1 part 1")
    //val program2 = builder.build(day1Part2())
    //File("aoc25/ksplang/1-2.ksplang").writeText(program2)
    //println("Generated program for day 1 part 2")
}

fun day1Part1() = buildComplexFunction("day1") {
    TODO()
}
