package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.aoc.days.day1Part1
import cz.sejsel.ksplang.benchmarks.Benchmarks
import cz.sejsel.ksplang.benchmarks.ProgramList
import cz.sejsel.ksplang.benchmarks.RustBenchmark
import cz.sejsel.ksplang.benchmarks.measuredLazy
import cz.sejsel.ksplang.benchmarks.MeasuredLazy
import cz.sejsel.ksplang.benchmarks.rust.RustKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import java.io.File
import kotlin.reflect.KProperty

@Suppress("unused")
object Programs : ProgramList {
    private val builder = KsplangBuilder()

    val day1Part1 = AoCBenchmarkProgram(
        name = "Day 1 - part 1",
        lazyProgram = measuredLazy { builder.build(day1Part1()) },
        inputStack = loadInput(1).map { it.code.toLong() },
        expectedResult = listOf(1034L),
        runs = 3,
        ksplangFilename = "1-1.ksplang",
        sourceFilename = "days/Day1.kt"
    )

    private fun loadInput(day: Int) = File("aoc25/inputs/$day.txt").readText()

    fun allAoCPrograms(): List<AoCBenchmarkProgram> =
        Programs::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.returnType.classifier == AoCBenchmarkProgram::class }
            .map { it.getter.call(this) as AoCBenchmarkProgram }

    override fun allPrograms() = allAoCPrograms().map { it.toBenchmarkProgram() }
}

class AoC25Solutions(override val runner: RustKsplangRunner) : Benchmarks {
    override val allBenchmarks = Programs.allAoCPrograms().map { it.toRustBenchmark() }
}

data class AoCBenchmarkProgram(
    val name: String,
    val lazyProgram: MeasuredLazy<String>,
    val inputStack: List<Long>,
    val expectedResult: List<Long>,
    val runs: Int,
    val ksplangFilename: String,
    val sourceFilename: String
) {
    fun toBenchmarkProgram() = cz.sejsel.ksplang.benchmarks.BenchmarkProgram(
        name = name,
        lazyProgram = lazyProgram,
        inputStack = inputStack,
        expectedResult = expectedResult
    )

    fun toRustBenchmark() = RustBenchmark(
        program = toBenchmarkProgram(),
        runs = runs
    )
}

