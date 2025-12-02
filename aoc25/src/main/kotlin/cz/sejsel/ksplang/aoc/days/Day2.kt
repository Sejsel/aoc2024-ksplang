package cz.sejsel.ksplang.aoc.days

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import java.io.File

// Day 2
// https://adventofcode.com/2025/day/2
fun main() {
    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(day2Part1())
    File("aoc25/ksplang/2-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/2-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 2 part 1")
    val program2 = builder.buildAnnotated(day2Part2())
    File("aoc25/ksplang/2-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/2-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 2 part 2")
}

fun day2Part1(): ComplexFunction = TODO()
fun day2Part2(): ComplexFunction = TODO()
