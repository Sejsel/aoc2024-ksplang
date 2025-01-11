package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonNegative
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.doWhileZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.whileNonZero
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.std.swap2
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class IfZeroBuilderTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        ifZero {
            inc()
        } otherwise {
            inc()
            inc()
        }
    }

    val program = builder.build(a)
    test("if zero - 0") {
        runner.run(program, listOf(0)) shouldBe listOf(1)
    }
    test("if zero - 1") {
        runner.run(program, listOf(1)) shouldBe listOf(3)
    }
})

class DoWhileZeroBuilderTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        doWhileZero {
            pop()
            CS()
        }
    }

    val program = builder.build(a)
    context("do while zero pop will pop all zeros at the end") {
        withData(1..20) {
            val stack = listOf(42L) + (0..<it).map { 0L }
            runner.run(program, stack) shouldBe listOf(42)
        }
    }
})

class DoWhileNonZeroBuilderTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        doWhileNonZero {
            pop()
            CS()
        }
    }

    val program = builder.build(a)
    context("do while nonzero zero pop will pop all nonzeros at the end") {
        withData(1..20) {
            val stack = listOf(0L) + (1L..it)
            runner.run(program, stack) shouldBe listOf(0)
        }
    }
})

class DoWhileNonNegativeBuilderTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        doWhileNonNegative {
            pop()
            dup()
        }
    }

    val program = builder.build(a)
    context("do while nonnegative pop will pop all non-negatives at the end") {
        withData(1..20) {
            val stack = listOf(-1L) + (0L..it)
            runner.run(program, stack) shouldBe listOf(-1)
        }
    }
})

class WhileNonZeroTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("while nonzero") {
        val tree = buildComplexFunction {
            // c x
            whileNonZero {
                dec()
                swap2()
                inc()
                swap2()
                // c+1 x-1
            }
        }
        val program = builder.build(tree)

        runner.run(program, listOf<Long>(0, 10)) shouldBe listOf<Long>(10)
    }

    context("while nonnegative pop will pop all non-negatives at the end") {
        val tree = buildComplexFunction {
            whileNonZero {
                pop()
            }
        }
        val program = builder.build(tree)
        withData(0..20) {
            val stack = listOf(-1L) + (0L..it)
            runner.run(program, stack) shouldBe listOf(-1)
        }
    }
})

