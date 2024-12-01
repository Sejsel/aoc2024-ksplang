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

class AddTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("Add zero does not change value") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 0L }) { (a, b) ->
            val program = builder.build(function { add() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a + b)
        }
    }

    context("Add 1 adds 1") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 1L }) { (a, b) ->
            val program = builder.build(function { add() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a + b)
        }
    }

    context("Add -1 subtracts 1") {
        withData(VALUES_PER_DIGIT_SUM.map { it to -1L }) { (a, b) ->
            val program = builder.build(function { add() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a + b)
        }
    }
})

class AbsSubTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("abssub zero does not change positive value") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 0L }) { (a, b) ->
            val program = builder.build(function { subabs() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(abs(a - b))
        }
    }

    context("abssub zero negates negative value") {
        withData(VALUES_PER_DIGIT_SUM.map { -it to 0L }) { (a, b) ->
            val program = builder.build(function { subabs() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(abs(a - b))
        }
    }

    context("abssub 1 subtracts 1 for positive values") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 1L }) { (a, b) ->
            val program = builder.build(function { subabs() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(abs(a - b))
        }
    }
})

class NegateTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("negate zero results in zero") {
        val program = builder.build(function { negate() })
        runner.run(program, listOf(0)) shouldContainExactly listOf(0)
    }

    context("negate negates positive value") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(function { negate() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(-it)
        }
    }

    context("negate negates negative value") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(function { negate() })
            runner.run(program, listOf(-it)) shouldContainExactly listOf(it)
        }
    }
})
