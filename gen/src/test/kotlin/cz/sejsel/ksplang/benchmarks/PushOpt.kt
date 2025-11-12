package cz.sejsel.ksplang.benchmarks

import cz.sejsel.ksplang.dsl.core.Instruction
import cz.sejsel.ksplang.dsl.core.extract
import cz.sejsel.ksplang.std.push

fun main() {
    // This program mainly measures how short (op count) pushes are.
    // This is mainly useful in control flow, but also constants
    var total = 0L
    var instructions = 0L
    val max = 10_000_000
    val histogram = mutableMapOf<Int, Int>()
    val instructionList = mutableListOf<Instruction>()
    (0..max).forEach { i ->
        if (i % 1_000_000 == 0) {
            println("Progress: $i / $max")
        }
        extract { push(i) }.appendInstructions(instructionList)
        val size = instructionList.size
        instructionList.clear()

        total += size
        instructions += 1
        histogram[size] = (histogram[size] ?: 0) + 1
    }
    println("Tested pushes in range 0 to $max")
    println("Average instructions per push: ${total.toDouble() / instructions}")
    println("Histogram of instruction counts:")
    histogram.toSortedMap().forEach { (k, v) ->
        println("  $k instructions: $v times")
    }
}
