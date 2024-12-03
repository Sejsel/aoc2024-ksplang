package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.doWhileZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.orIfNonZero
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class IfZeroBuilderTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        ifZero {
            inc()
        } orIfNonZero {
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
