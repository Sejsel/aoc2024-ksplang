package cz.sejsel.ksplang.benchmarks

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.interpreter.VMOptions
import cz.sejsel.ksplang.interpreter.parseProgram
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.permute
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.sort
import cz.sejsel.ksplang.std.stacklen
import cz.sejsel.ksplang.std.swap2

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

    val sumloop10000 = BenchmarkProgram(
        name = "sumloop10000",
        program = builder.build(buildComplexFunction { sum() }),
        inputStack = (1..10000L) + 10000L
    )
    val stacklen10000 = BenchmarkProgram(
        name = "stacklen10000",
        program = builder.build(buildComplexFunction { stacklen() }),
        inputStack = (1..10000L).toList()
    )

    val sort100 = BenchmarkProgram(
        name = "sort100",
        program = builder.build(buildComplexFunction { sort() }),
        inputStack = (1..100L).reversed() + 100L
    )
}

private fun ComplexBlock.sum() {
    // n
    push(0); swap2()
    // 0 n
    doWhileNonZero {
        // [...] x sum i
        permute("x sum i", "i x sum")
        add()
        // i x+sum
        swap2()
        // x+sum i
        dec()
        // sum i-1
        CS()
    }
    // sum 0
    pop()
}