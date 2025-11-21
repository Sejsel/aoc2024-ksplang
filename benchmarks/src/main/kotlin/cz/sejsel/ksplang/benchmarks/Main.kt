package cz.sejsel.ksplang.benchmarks

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import cz.sejsel.ksplang.benchmarks.rust.RustKsplangRunner
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.nio.file.Path
import kotlin.io.path.writeText

data class KsplangInterpreter(
    val name: String,
    val pathToInterpreter: String,
    val optimize: Boolean
)

fun main(args: Array<String>) {
    BenchmarksCli()
        .subcommands(BenchmarkCommand(), DumpProgramsCommand())
        .main(args)
}

class BenchmarksCli : CliktCommand(name = "benchmarks") {
    override fun run() {}
}

class BenchmarkCommand : CliktCommand(
    name = "benchmark",
) {
    private val enableKotlin by option("--enable-kotlin", help = "Enable Kotlin JMH benchmarks").flag(default = false)

    override fun run() {
        val ksplangs = buildList {
            add(KsplangInterpreter("system ksplang", "ksplang", optimize = false))
            //add(KsplangInterpreter("exyi", "../exyi-ksplang/target/release/ksplang-cli", optimize = false))
            add(KsplangInterpreter("exyi optimize", "../exyi-ksplang/target/release/ksplang-cli", optimize = true))
        }

        runBenchmarks(ksplangs, enableKotlin)
    }
}

class DumpProgramsCommand : CliktCommand(
    name = "dump-programs",
) {
    private val outputDir by option("--output-dir", "-o", help = "Output directory for programs")
        .path(canBeFile = false)
        .default(Path.of("."))

    override fun run() {
        outputDir.toFile().mkdirs()

        val programs = listOf(
            Programs.stacklen10000,
            Programs.sort100,
            Programs.sumloop10000,
            Programs.wasmaoc24day2,
            Programs.wasmksplangpush1,
            Programs.wasmi32factorial10000,
            Programs.wasmi64factorial10000,
        )

        programs.forEach { program ->
            val programFile = outputDir.resolve("${program.name}.ksplang")
            programFile.writeText(program.program)
            echo("Wrote ${program.name}.ksplang")
            val inputFile = outputDir.resolve("${program.name}.ksplang.input")
            inputFile.writeText(program.inputStack.joinToString("\n"))
        }

        echo("Dumped ${programs.size} programs to $outputDir")
    }
}

fun runBenchmarks(ksplangs: List<KsplangInterpreter>, enableKotlin: Boolean) {
    val resultsByBenchmark = mutableMapOf<String, MutableMap<String, Double??>>()

    ksplangs.forEach { (interpreterName, pathToInterpreter, optimize) ->
        val runner = RustKsplangRunner(
            pathToInterpreter = pathToInterpreter,
            optimize = optimize,
            environmentVariables = mapOf(
                "KSPLANGJIT_VERBOSITY" to "0",
                "KSPLANGJIT_TRIGGER_COUNT" to "500"
            )
        )
        val benchmarks = RustBenchmarks(runner)
        benchmarks.allBenchmarks.forEach { benchmark ->
            try {
                val result = benchmarks.runBenchmark(benchmark)
                val mean = result.map { it.toMillis() }.average()
                println("$interpreterName ${benchmark.name} average time: $mean ms")
                resultsByBenchmark.getOrPut(benchmark.name) { mutableMapOf() }[interpreterName] = mean
            } catch (e: Exception) {
                println("$interpreterName ${benchmark.name} had an error: ${e.message}")
                resultsByBenchmark.getOrPut(benchmark.name) { mutableMapOf() }[interpreterName] = null
            }
        }
    }

    if (enableKotlin) {
        runJMHBenchmarks().forEach { (benchmarkName, time) ->
            println("JMH $benchmarkName average time: $time ms")
            resultsByBenchmark.getOrPut(benchmarkName) { mutableMapOf() }["Kotlin"] = time
        }
    }

    val tableHeaders = ksplangs.map { it.name } + if (enableKotlin) listOf("Kotlin") else emptyList()
    printResultsTable(resultsByBenchmark, tableHeaders)
}

private fun runJMHBenchmarks(): Map<String, Double> {
    val options = OptionsBuilder()
        .include("cz.sejsel.benchmarks.*")
        .forks(0)
        .build()

    val runner = Runner(options)
    val results = runner.run()
    return results.associate {
        it.primaryResult.label to it.primaryResult.score
    }
}

fun printResultsTable(
    resultsByBenchmark: Map<String, Map<String, Double?>>,
    interpreters: List<String>
) {
    val benchmarks = resultsByBenchmark.keys.sorted()

    // Table header
    val colWidths = mutableListOf<Int>()
    colWidths.add(benchmarks.maxOf { it.length }.coerceAtLeast("Benchmark".length))
    interpreters.forEachIndexed { idx, name ->
        colWidths.add(name.length.coerceAtLeast(12))
    }

    fun pad(str: String, width: Int) = str.padStart(width)
    val RED = "\u001B[31m"
    val RESET = "\u001B[0m"

    // Top border
    print("╔")
    print("═".repeat(colWidths[0] + 2))
    interpreters.forEachIndexed { idx, _ ->
        print("╦" + "═".repeat(colWidths[idx + 1] + 2))
    }
    println("╗")

    // Header row
    print("║ " + pad("Benchmark", colWidths[0]) + " ")
    interpreters.forEachIndexed { idx, name ->
        print("║ " + pad(name, colWidths[idx + 1]) + " ")
    }
    println("║")

    // Header separator
    print("╠")
    print("═".repeat(colWidths[0] + 2))
    interpreters.forEachIndexed { idx, _ ->
        print("╬" + "═".repeat(colWidths[idx + 1] + 2))
    }
    println("╣")

    // Data rows
    for (benchmark in benchmarks) {
        print("║ " + pad(benchmark, colWidths[0]) + " ")
        for ((idx, interpreter) in interpreters.withIndex()) {
            val value = resultsByBenchmark[benchmark]?.get(interpreter)
            if (value != null) {
                val cell = String.format("%.2f ms", value)
                print("║ " + pad(cell, colWidths[idx + 1]) + " ")
            } else {
                val cell = pad("ERROR", colWidths[idx + 1])
                print("║ $RED$cell$RESET ")
            }
        }
        println("║")
    }

    // Bottom border
    print("╚")
    print("═".repeat(colWidths[0] + 2))
    interpreters.forEachIndexed { idx, _ ->
        print("╩" + "═".repeat(colWidths[idx + 1] + 2))
    }
    println("╝")
}