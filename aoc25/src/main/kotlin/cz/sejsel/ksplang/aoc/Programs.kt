package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.aoc.days.day1Part1
import cz.sejsel.ksplang.benchmarks.BenchmarkProgram
import cz.sejsel.ksplang.benchmarks.Benchmarks
import cz.sejsel.ksplang.benchmarks.ProgramList
import cz.sejsel.ksplang.benchmarks.RustBenchmark
import cz.sejsel.ksplang.benchmarks.measuredLazy
import cz.sejsel.ksplang.benchmarks.rust.RustKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.stacklen
import java.io.File
import kotlin.reflect.KProperty

object Programs : ProgramList {
    private val builder = KsplangBuilder()

    val day1Part1 = BenchmarkProgram(
        name = "aoc25-day1-part1",
        lazyProgram = measuredLazy { builder.build(day1Part1()) },
        inputStack = loadInput(1).map { it.code.toLong() },
        expectedResult = listOf(1034L),
    )

    private fun loadInput(day: Int) = File("aoc25/inputs/$day.txt").readText()

    // Use reflection to get all BenchmarkProgram properties so we don't have to maintain a separate list
    override fun allPrograms(): List<BenchmarkProgram> =
        Programs::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.returnType.classifier == BenchmarkProgram::class }
            .map { it.getter.call(this) as BenchmarkProgram }
}

class AoC25Solutions(override val runner: RustKsplangRunner) : Benchmarks {
    override val allBenchmarks = listOf(
        RustBenchmark(Programs.day1Part1, runs = 1),
    )
}
