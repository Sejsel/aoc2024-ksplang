package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.benchmarks.BenchmarkResults
import cz.sejsel.ksplang.benchmarks.KsplangInterpreter
import cz.sejsel.ksplang.benchmarks.runBenchmarks

fun main() {
    val ksplangs = buildList {
        add(KsplangInterpreter("ksplang", "ksplang", optimize = false))
        add(KsplangInterpreter("KsplangJIT", "../exyi-ksplang/target/release/ksplang-cli", optimize = true))
        add(KsplangInterpreter("KsplangJIT old", "../exyi-ksplang/ksplang-last-known-working", optimize = true))
    }

    val results = runBenchmarks(ksplangs, enableKotlin = false, Programs, ::AoC25Solutions)
    val markdown = toEnrichedMarkdown(results, Programs.allAoCPrograms())
    println(markdown)
}

fun toEnrichedMarkdown(results: BenchmarkResults, programs: List<AoCBenchmarkProgram>, includeBuildTime: Boolean = false): String {
    val benchmarks = results.resultsByBenchmark.keys.sorted()
    val filteredInterpreters = if (includeBuildTime) {
        results.interpreters
    } else {
        results.interpreters.filter { it != "BUILD" }
    }
    
    // Create a map from benchmark name to AoCBenchmarkProgram
    val programsByName = programs.associateBy { it.name }
    
    val result = StringBuilder()

    // Header row
    result.append("| Program ")
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
    for (benchmarkName in benchmarks) {
        val program = programsByName[benchmarkName]
        
        result.append("| $benchmarkName ")
        
        for (interpreter in filteredInterpreters) {
            val value = results.resultsByBenchmark[benchmarkName]?.get(interpreter)
            if (value != null) {
                val cell = when (interpreter) {
                    "Instructions" -> {
                        val formatted = String.format("%d", value.toInt())
                        // Link Instructions column to ksplang file
                        if (program != null) {
                            "[$formatted](/aoc25/ksplang/${program.ksplangFilename}) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/${program.sourceFilename})"
                        } else {
                            formatted
                        }
                    }
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

