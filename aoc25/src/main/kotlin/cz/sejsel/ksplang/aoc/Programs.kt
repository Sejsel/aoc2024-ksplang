package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.aoc.days.pure.day1Part1
import cz.sejsel.ksplang.aoc.days.pure.day1Part2
import cz.sejsel.ksplang.aoc.days.pure.day2Part1
import cz.sejsel.ksplang.aoc.days.pure.day2Part2
import cz.sejsel.ksplang.aoc.days.pure.day3Part1
import cz.sejsel.ksplang.aoc.days.wasm.wasmDay1Part1
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
        expectedResult = listOf(1034),
        runs = 3,
        ksplangFilename = "1-1.ksplang",
        sourceFilename = "days/pure/Day1.kt"
    )
    val day1Part2 = AoCBenchmarkProgram(
        name = "Day 1 - part 2",
        lazyProgram = measuredLazy { builder.build(day1Part2()) },
        inputStack = loadInput(1).map { it.code.toLong() },
        expectedResult = listOf(6166),
        runs = 3,
        ksplangFilename = "1-2.ksplang",
        sourceFilename = "days/pure/Day1.kt"
    )
    val day2Part1 = AoCBenchmarkProgram(
        name = "Day 2 - part 1",
        lazyProgram = measuredLazy { builder.build(day2Part1()) },
        inputStack = loadInput(2).map { it.code.toLong() },
        expectedResult = listOf(19605500130),
        runs = 1, // Fairly slow at 14s or so
        ksplangFilename = "2-1.ksplang",
        sourceFilename = "days/pure/Day2.kt"
    )
    val day2Part2 = AoCBenchmarkProgram(
        name = "Day 2 - part 2",
        lazyProgram = measuredLazy { builder.build(day2Part2()) },
        inputStack = loadInput(2).map { it.code.toLong() },
        expectedResult = listOf(36862281418),
        runs = 1, // Even more slow than part 1
        ksplangFilename = "2-2.ksplang",
        sourceFilename = "days/pure/Day2.kt"
    )
    val day3Part1 = AoCBenchmarkProgram(
        name = "Day 3 - part 1",
        lazyProgram = measuredLazy { builder.build(day3Part1()) },
        inputStack = loadInput(3).map { it.code.toLong() },
        expectedResult = listOf(17085),
        runs = 4, // This one is fast
        ksplangFilename = "3-1.ksplang",
        sourceFilename = "days/pure/Day3.kt"
    )
    val day1Part1Wasm = AoCBenchmarkProgram(
        name = "Day 1 - part 1 WASM (Rust)",
        lazyProgram = measuredLazy { builder.build(wasmDay1Part1()) },
        inputStack = loadInput(1).map { it.code.toLong() },
        expectedResult = listOf(1034),
        runs = 3,
        ksplangFilename = "wasm/1-1.ksplang",
        sourceFilename = "days/wasm/Day1.kt"
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
    val expectedResult: List<Long>?,
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

