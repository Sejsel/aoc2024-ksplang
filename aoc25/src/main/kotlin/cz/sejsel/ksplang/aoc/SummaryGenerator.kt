package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.benchmarks.KsplangInterpreter
import cz.sejsel.ksplang.benchmarks.runBenchmarks

fun main() {
    val ksplangs = buildList {
        add(KsplangInterpreter("ksplang", "ksplang", optimize = false))
        add(KsplangInterpreter("KsplangJIT", "../exyi-ksplang/target/release/ksplang-cli", optimize = true))
        add(KsplangInterpreter("KsplangJIT old", "../exyi-ksplang/ksplang-last-known-working", optimize = true))
    }

    val results = runBenchmarks(ksplangs, enableKotlin = false, Programs, ::AoC25Solutions)
    val markdown = results.toMarkdown()
    val linkedMarkdown = injectLinksToMarkdown(markdown)
    println(linkedMarkdown)
}

fun injectLinksToMarkdown(markdown: String): String {
    val lines = markdown.lines()
    val result = StringBuilder()
    
    for ((index, line) in lines.withIndex()) {
        // Skip header (first 2 lines) and empty lines
        if (index < 2 || line.isBlank()) {
            result.appendLine(line)
            continue
        }
        
        // Parse the row
        val columns = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (columns.isEmpty()) {
            result.appendLine(line)
            continue
        }
        
        // Extract day and part from first column (e.g., "Day 1 - part 1")
        val benchmarkName = columns[0]
        val dayPartRegex = """Day (\d+) - part (\d+)""".toRegex()
        val match = dayPartRegex.find(benchmarkName)
        
        if (match != null) {
            val day = match.groupValues[1]
            val part = match.groupValues[2]
            
            // Create linked version of columns
            val linkedColumns = columns.toMutableList()
            
            // Link first column to Day$day.kt
            linkedColumns[0] = "[$benchmarkName](aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day$day.kt)"
            
            // Find Instructions column and link to ksplang file
            // The Instructions column should be at index 1 (after Benchmark column)
            if (columns.size > 1) {
                // Link the Instructions column (assuming it's the second column)
                val instructionsValue = columns[1]
                linkedColumns[1] = "[$instructionsValue](aoc25/ksplang/$day-$part.ksplang)"
            }
            
            // Reconstruct the row
            result.append("| ")
            result.append(linkedColumns.joinToString(" | "))
            result.appendLine(" |")
        } else {
            // If the row doesn't match the pattern, keep it as-is
            result.appendLine(line)
        }
    }
    
    return result.toString()
}

