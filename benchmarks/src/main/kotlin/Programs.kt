package cz.sejsel.benchmarks

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.interpreter.VMOptions
import cz.sejsel.ksplang.interpreter.parseProgram
import cz.sejsel.ksplang.std.stacklen

class BenchmarkProgram(val name: String, val program: String, val inputStack: List<Long>) {
    val ops = parseProgram(program)
    val vmOptions = VMOptions(
        initialStack = inputStack,
        maxStackSize = 1_000_000,
        piDigits = listOf(3, 1, 4),
        maxOpCount = Long.MAX_VALUE,
    )
}

object Programs {
    private val builder = KsplangBuilder()

    val stacklen = BenchmarkProgram(
        name = "stacklen10000",
        program = builder.build(buildComplexFunction { stacklen() }),
        inputStack = (1..10000).map { it.toLong() }
    )
}
