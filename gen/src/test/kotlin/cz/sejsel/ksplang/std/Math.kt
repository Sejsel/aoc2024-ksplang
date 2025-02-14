package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.KsplangRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.math.abs
import kotlin.math.min

class AddTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("Add zero does not change value") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 0L }) { (a, b) ->
            val program = builder.build(buildFunction { add() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a + b)
        }
    }

    context("Add 1 adds 1") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 1L }) { (a, b) ->
            val program = builder.build(buildFunction { add() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a + b)
        }
    }

    context("Add -1 subtracts 1") {
        withData(VALUES_PER_DIGIT_SUM.map { it to -1L }) { (a, b) ->
            val program = builder.build(buildFunction { add() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a + b)
        }
    }
})

class SubTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("Sub zero does not change value") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 0L }) { (a, b) ->
            val program = builder.build(buildFunction { sub() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a - b)
        }
    }

    context("Sub 1 subtracts 1") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 1L }) { (a, b) ->
            val program = builder.build(buildFunction { sub() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a - b)
        }
    }

    context("Sub -1 adds 1") {
        withData(VALUES_PER_DIGIT_SUM.map { it to -1L }) { (a, b) ->
            val program = builder.build(buildFunction { sub() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a - b)
        }
    }
})

class MulTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val digitSumCases =
        VALUES_PER_DIGIT_SUM.map { it to 0L } + VALUES_PER_DIGIT_SUM.map { it to -1L } + VALUES_PER_DIGIT_SUM.map { it to 1L }
    val extraCases = listOf<Pair<Long, Long>>(
        0L to 0,
        Long.MIN_VALUE to 0,
        Long.MAX_VALUE to 0,
        0L to Long.MIN_VALUE,
        0L to Long.MAX_VALUE,
        10L to 10,
        1000L to 1000,
        -1000L to 1000
    )

    context("mul") {
        withData(digitSumCases + extraCases) { (a, b) ->
            val program = builder.build(buildFunction { mul() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(a * b)
        }
    }
})

class DivTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val digitSumCases =
        VALUES_PER_DIGIT_SUM.map { 1L to it } + VALUES_PER_DIGIT_SUM.map { 2L to it } + VALUES_PER_DIGIT_SUM.map { 3L to it }
    val extraCases = listOf<Pair<Long, Long>>(
        Long.MIN_VALUE to 1,
        Long.MAX_VALUE to 2,
        Long.MIN_VALUE to 0,
        Long.MAX_VALUE to 0,
        10L to 10,
        1000L to 1000,
        -1000L to 1000
    )

    context("div") {
        withData(digitSumCases + extraCases) { (a, b) ->
            val program = builder.build(buildFunction { div() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(b / a)
        }
    }
})

class AbsSubTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("abssub zero does not change positive value") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 0L }) { (a, b) ->
            val program = builder.build(buildFunction { subabs() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(abs(a - b))
        }
    }

    context("abssub zero negates negative value") {
        withData(VALUES_PER_DIGIT_SUM.map { -it to 0L }) { (a, b) ->
            val program = builder.build(buildFunction { subabs() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(abs(a - b))
        }
    }

    context("abssub 1 subtracts 1 for positive values") {
        withData(VALUES_PER_DIGIT_SUM.map { it to 1L }) { (a, b) ->
            val program = builder.build(buildFunction { subabs() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(abs(a - b))
        }
    }
})

class NegateTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("negate zero results in zero") {
        val program = builder.build(buildFunction { negate() })
        runner.run(program, listOf(0)) shouldContainExactly listOf(0)
    }

    context("negate negates positive value") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(buildFunction { negate() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(-it)
        }
    }

    context("negate negates negative value") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(buildFunction { negate() })
            runner.run(program, listOf(-it)) shouldContainExactly listOf(it)
        }
    }
})

class SgnTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("sgn(1) = 1") {
        val program = builder.build(buildFunction { sgn() })
        runner.run(program, listOf(1)) shouldContainExactly listOf(1)
    }
    test("sgn(-1) = -1") {
        val program = builder.build(buildFunction { sgn() })
        runner.run(program, listOf(-1)) shouldContainExactly listOf(-1)
    }
    test("sgn(0) = 0") {
        val program = builder.build(buildFunction { sgn() })
        runner.run(program, listOf(0)) shouldContainExactly listOf(0)
    }
    test("sgn(2^63-1) = 1") {
        val program = builder.build(buildFunction { sgn() })
        runner.run(program, listOf(Long.MAX_VALUE)) shouldContainExactly listOf(1)
    }
    test("sgn(-2^63) = -1") {
        val program = builder.build(buildFunction { sgn() })
        runner.run(program, listOf(Long.MIN_VALUE)) shouldContainExactly listOf(-1)
    }
})

class AbsTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("abs(1) = 1") {
        val program = builder.build(buildFunction { abs() })
        runner.run(program, listOf(1)) shouldContainExactly listOf(1)
    }
    test("abs(-1) = 1") {
        val program = builder.build(buildFunction { abs() })
        runner.run(program, listOf(-1)) shouldContainExactly listOf(1)
    }
    test("abs(0) = 0") {
        val program = builder.build(buildFunction { abs() })
        runner.run(program, listOf(0)) shouldContainExactly listOf(0)
    }
    test("abs(2^63-1) = 2^63-1") {
        val program = builder.build(buildFunction { abs() })
        runner.run(program, listOf(Long.MAX_VALUE)) shouldContainExactly listOf(Long.MAX_VALUE)
    }
    test("abs(-(2^63-1)) = 2^63-1") {
        val program = builder.build(buildFunction { abs() })
        runner.run(program, listOf(Long.MIN_VALUE + 1)) shouldContainExactly listOf(Long.MAX_VALUE)
    }
})

class Min2Tests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val digitSumCases =
        VALUES_PER_DIGIT_SUM.map { it to 0L } + VALUES_PER_DIGIT_SUM.map { it to -1L } + VALUES_PER_DIGIT_SUM.map { it to 1L }
    val extraCases = listOf<Pair<Long, Long>>(
        0L to 0,
        Long.MIN_VALUE to 0,
        Long.MAX_VALUE to 0,
        Long.MAX_VALUE to Long.MIN_VALUE,
        Long.MIN_VALUE to Long.MAX_VALUE,
        0L to Long.MIN_VALUE,
        0L to Long.MAX_VALUE,
        10L to 10,
        1000L to 1000,
        -1000L to 1000
    )

    context("min2") {
        withData(digitSumCases + extraCases) { (a, b) ->
            val program = builder.build(buildFunction { min2() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(min(a, b))
        }
    }
})

class DecTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("dec subtracts 1") {
        withData(VALUES_PER_DIGIT_SUM + listOf(-1, Long.MIN_VALUE + 1, Long.MAX_VALUE)) {
            val program = builder.build(buildFunction { dec() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it - 1)
        }
    }

    context("decPositive subtracts 1") {
        withData(VALUES_PER_DIGIT_SUM.filter { it > 0 } + listOf(Long.MAX_VALUE)) {
            val program = builder.build(buildFunction { dec() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it - 1)
        }
    }
})

class Bitor32Tests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildFunction { bitor32() })
    println(program)

    test("bitor32 all combinations") {
        runner.run(program, listOf(0b1100, 0b1010)) shouldContainExactly listOf(0b1110)
    }
    test("bitor32 removes extra bits") {
        runner.run(program, listOf(0xFF_FF_FF_FF, 0x7F_FF_FF_FF_FF_FF_FF_FF)) shouldContainExactly listOf(0xFF_FF_FF_FF)
    }
})

class Bitnot32Tests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildFunction { bitnot32() })

    test("bitnot32 - basic") {
        runner.run(program, listOf(0b1010)) shouldContainExactly listOf(0b11111111_11111111_11111111_11110101)
    }
    test("bitnot32 removes extra bits") {
        runner.run(program, listOf(0x7F_FF_FF_FF_FF_FF_FF_FF)) shouldContainExactly listOf(0)
        runner.run(program, listOf(0xFF_FF_FF_FF)) shouldContainExactly listOf(0)
    }
})
