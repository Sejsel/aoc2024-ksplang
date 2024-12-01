package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.function
import cz.sejsel.ksplang.KsplangRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.math.abs
import kotlin.math.absoluteValue

class DupTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("dup duplicates single value for all CS") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(function { dup() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, it)
        }
    }

    context("dup duplicates single value for all CS (negative version)") {
        withData(VALUES_PER_DIGIT_SUM.map { -it }) {
            val program = builder.build(function { dup() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, it)
        }
    }

    context("dup duplicates extremes") {
        withData(listOf(Long.MAX_VALUE, Long.MIN_VALUE).map { it }) {
            val program = builder.build(function { dup() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, it)
        }
    }
})
