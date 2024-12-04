package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.aoc.summary.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.Block
import java.io.File

data class Day(
    val name: String,
    val gen1: (() -> Block)?,
    val gen2: (() -> Block)?,
    val textInput: Boolean
)

val DAYS = listOf(
    Day(
        name = "1",
        gen1 = { day1Part1() },
        gen2 = null,
        textInput = false
    ),
)

fun getInput(dayName: String): String = File("inputs/$dayName.txt").readText()

fun main() {
    val builder = KsplangBuilder()
    val runner = KsplangRunner()

    // Markdown table output, two rows per day (one for gen1 and one for gen2)
    // Columns: Day, program instruction count, runtime, executed instructions
    println("| Day | Instructions | Input mode | Runtime | Executed instructions |")
    println("|-----|--------------|------------|---------|-----------------------|")


    DAYS.forEach { day ->
        listOf("1" to day.gen1, "2" to day.gen2).forEach { (part, gen) ->
            if (gen == null) {
                return@forEach
            }
            val partName = "${day.name}-$part"

            val program = builder.build(gen())
            val instructionCount = program.trim().split("\\s+".toRegex()).count()
            val input = getInput(day.name)
            val result = runner.run(program, input, inputText = day.textInput)

            val executionTime = "${result.executionTime.toSeconds()}.${result.executionTime.toMillis() % 1000}s"
            val mode = if (day.textInput) "text" else "numeric"
            println("| $partName | [$instructionCount](/ksplang/$partName.ksplang) | $mode | $executionTime | ${result.instructionsExecuted} |")
        }
    }
}
