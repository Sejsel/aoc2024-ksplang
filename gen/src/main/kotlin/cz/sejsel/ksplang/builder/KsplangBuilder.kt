package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.dsl.core.Instruction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.function
import cz.sejsel.ksplang.std.push

/** Transforms the ksplang DSL tree consisting of [Instruction], [SimpleFunction], and [ComplexFunction]
 * into real ksplang code. */
class KsplangBuilder {
    fun build(instructions: SimpleFunction): String {
        return build(instructions.getInstructions())
    }

    fun build(instructions: List<Instruction>): String {
        return instructions.joinToString(" ") { it.toString() }
    }
}

fun main() {
    val builder = KsplangBuilder()
    val instructions = function {
        function {
            push(0)
            inc()
            inc()
        }
        function {
            inc()
            push(0)
            inc()
        }
    }
    println(builder.build(instructions))
}