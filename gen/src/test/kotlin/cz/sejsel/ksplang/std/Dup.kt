package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly

class DupTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    context("dup duplicates single value for all CS") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(buildFunction { dup() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, it)
        }
    }

    context("dup duplicates single value for all CS (negative version)") {
        withData(VALUES_PER_DIGIT_SUM.map { -it }) {
            val program = builder.build(buildFunction { dup() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, it)
        }
    }

    context("dup duplicates extremes") {
        withData(listOf(Long.MAX_VALUE, Long.MIN_VALUE).map { it }) {
            val program = builder.build(buildFunction { dup() })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, it)
        }
    }
})

class DupVariationsTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("dupAb duplicates two values") {
        val program = builder.build(buildFunction { dupAb() })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2, 1, 2)
    }

    test("dupSecond duplicates second") {
        val program = builder.build(buildFunction { dupSecond() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 5)
    }
    test("dupThird duplicates third") {
        val program = builder.build(buildFunction { dupThird() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 4)
    }
    test("dupFourth duplicates fourth") {
        val program = builder.build(buildFunction { dupFourth() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 3)
    }
    test("dupFifth duplicates fifth") {
        val program = builder.build(buildFunction { dupFifth() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 2)
    }
    test("dupSixth duplicates sixth") {
        val program = builder.build(buildFunction { dupSixth() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 1)
    }
})

class DupNthTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("dupNth duplicates first") {
        val program = builder.build(buildComplexFunction { dupNth() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 6)
    }

    test("dupNth duplicates second") {
        val program = builder.build(buildComplexFunction { dupNth() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 2)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 5)
    }
    test("dupNth duplicates third") {
        val program = builder.build(buildComplexFunction { dupNth() })
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 3)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 4)
    }
})
