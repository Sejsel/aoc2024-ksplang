package cz.sejsel.benchmarks

import cz.sejsel.benchmarks.rust.RustKsplangRunner
import org.openjdk.jmh.runner.options.OptionsBuilder

data class KsplangInterpreter(
    val name: String,
    val pathToInterpreter: String,
    val optimize: Boolean
)

fun main() {
    val ksplangs = listOf(
        KsplangInterpreter("system ksplang", "ksplang", optimize = false),
        KsplangInterpreter("fast funkcia", "../ksplang/target/release/ksplang-cli", optimize = false),
        KsplangInterpreter("exyi", "../exyi-ksplang/target/release/ksplang-cli", optimize = false),
        KsplangInterpreter("exyi optimize", "../exyi-ksplang/target/release/ksplang-cli", optimize = true),
    )

    val enableKotlin = true

    val resultsByBenchmark = mutableMapOf<String, MutableMap<String, Double??>>()

    ksplangs.forEach { (interpreterName, pathToInterpreter, optimize) ->
        val runner = RustKsplangRunner(
            pathToInterpreter = pathToInterpreter,
            optimize = optimize
        )
        val benchmarks = RustBenchmarks(runner)
        benchmarks.ALL_BENCHMARKS.forEach { benchmark ->
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

    runJMHBenchmarks().forEach { (benchmarkName, time) ->
        println("JMH $benchmarkName average time: $time ms")
        resultsByBenchmark.getOrPut(benchmarkName) { mutableMapOf() }["Kotlin"] = time
    }

    val tableHeaders = ksplangs.map { it.name } + if (enableKotlin) listOf("Kotlin") else emptyList()
    printResultsTable(resultsByBenchmark, tableHeaders)
}

private fun runJMHBenchmarks(): Map<String, Double> {
    val options = OptionsBuilder()
        .include("cz.sejsel.benchmarks.*")
        .forks(0)
        .build()

    val runner = org.openjdk.jmh.runner.Runner(options)
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

    fun pad(str: String, width: Int) = str.padEnd(width)
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