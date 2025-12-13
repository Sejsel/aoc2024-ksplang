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
    val optimize: Boolean,
    val envVariables: Map<String, String> = emptyMap()
)

data class BenchmarkResults(
    val resultsByBenchmark: Map<String, Map<String, Double?>>,
    val interpreters: List<String>
) {
    fun printPretty() {
        val benchmarks = resultsByBenchmark.keys.sorted()

        // Table header
        val colWidths = mutableListOf<Int>()
        colWidths.add(benchmarks.maxOf { it.length }.coerceAtLeast("Benchmark".length))
        interpreters.forEach { name ->
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
                    val cell = when (interpreter) {
                        "Instructions" -> String.format("%d", value.toInt())
                        else -> String.format("%.2f ms", value)
                    }
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

    fun toMarkdown(includeBuildTime: Boolean = false): String {
        val benchmarks = resultsByBenchmark.keys.sorted()
        val filteredInterpreters = if (includeBuildTime) {
            interpreters
        } else {
            interpreters.filter { it != "BUILD" }
        }
        val result = StringBuilder()

        // Header row
        result.append("| Benchmark ")
        filteredInterpreters.forEach { name ->
            result.append("| $name ")
        }
        result.appendLine("|")

        // Separator row
        result.append("| --- ")
        filteredInterpreters.forEach { _ ->
            result.append("| ---: ")
        }
        result.appendLine("|")

        // Data rows
        for (benchmark in benchmarks) {
            result.append("| $benchmark ")
            for (interpreter in filteredInterpreters) {
                val value = resultsByBenchmark[benchmark]?.get(interpreter)
                if (value != null) {
                    val cell = when (interpreter) {
                        "Instructions" -> String.format("%d", value.toInt())
                        else -> String.format("%.2f ms", value)
                    }
                    result.append("| $cell ")
                } else {
                    result.append("| ERROR ")
                }
            }
            result.appendLine("|")
        }
        
        return result.toString()
    }
}

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
            add(KsplangInterpreter("ksplang", "ksplang", optimize = false))
            add(
                KsplangInterpreter(
                    "KsplangJIT", "../exyi-ksplang/target/release/ksplang-cli", optimize = true, envVariables = mapOf(
                        "KSPLANGJIT_VERBOSITY" to "0",
                        "KSPLANGJIT_TRIGGER_COUNT" to "500",
                    )
                )
            )
            add(
                KsplangInterpreter(
                    "KsplangJIT no tracing", "../exyi-ksplang/target/release/ksplang-cli", optimize = true, envVariables = mapOf(
                        "KSPLANGJIT_VERBOSITY" to "0",
                        "KSPLANGJIT_TRIGGER_COUNT" to "500",
                        "KSPLANGJIT_TRACE_LIMIT" to "0"
                    )
                )
            )
        }

        val results = runBenchmarks(ksplangs, enableKotlin, Programs) { RustBenchmarks(it) }
        results.printPretty()
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

        Programs.allPrograms().forEach { program ->
            val programFile = outputDir.resolve("${program.name}.ksplang")
            programFile.writeText(program.program)
            echo("Wrote ${program.name}.ksplang")
            val inputFile = outputDir.resolve("${program.name}.ksplang.input")
            inputFile.writeText(program.inputStack.joinToString("\n"))
        }

        echo("Dumped programs to $outputDir")
    }
}

fun runBenchmarks(ksplangs: List<KsplangInterpreter>, enableKotlin: Boolean, programs: ProgramList, benchmarksFactory: (RustKsplangRunner) -> Benchmarks = { RustBenchmarks(it) }): BenchmarkResults {
    val resultsByBenchmark = mutableMapOf<String, MutableMap<String, Double??>>()

    ksplangs.forEach { (interpreterName, pathToInterpreter, optimize, params) ->
        val runner = RustKsplangRunner(
            pathToInterpreter = pathToInterpreter,
            optimize = optimize,
            environmentVariables = params,
        )
        val benchmarks = benchmarksFactory(runner)
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

    programs.allPrograms().forEach {
        resultsByBenchmark.getOrPut(it.name) { mutableMapOf() }["Instructions"] =
            it.ops.size.toDouble()
        resultsByBenchmark.getOrPut(it.name) { mutableMapOf() }["BUILD"] =
            it.buildDuration?.toMillis()?.toDouble() ?: -1.0
    }

    val tableHeaders = listOf("Instructions", "BUILD") + ksplangs.map { it.name } + if (enableKotlin) listOf("Kotlin") else emptyList()
    return BenchmarkResults(resultsByBenchmark, tableHeaders)
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
