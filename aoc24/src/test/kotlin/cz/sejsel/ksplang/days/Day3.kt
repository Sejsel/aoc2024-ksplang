package cz.sejsel.ksplang.days

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.aoc.checkDo
import cz.sejsel.ksplang.aoc.checkDont
import cz.sejsel.ksplang.aoc.checkMul
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MulTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { checkMul() })

    val prefix = listOf<Long>(4, 2)

    val validInputs = listOf(
        "mul(123,456)",
        "mul(789,012)",
        "mul(000,0)",
        "mul(000,00)",
        "mul(000,000)",
        "mul(00,0)",
        "mul(00,00)",
        "mul(00,000)",
        "mul(0,0)",
        "mul(0,00)",
        "mul(0,000)",
    )

    val invalidInputs = listOf(
        "a",
        "a",
        "mua",
        "mula",
        "mul(123!456)",
        "mul(789,012]",
        "mul[000,000)",
        "mul(0,)",
        "mul(00,)",
        "mul(000,)",
        "mul(,0)",
        "mul(,00)",
        "mul(,000)",
        "mul(123,12a)",
        "mul(123,02a)",
    )

    validInputs.forEach {
        test("checkMul - valid $it") {
            val input = prefix + it.map { it.code.toLong() }
            val params = listOf<Long>(prefix.size.toLong())
            runner.run(program, input + params) shouldBe input + listOf(1L)
        }
    }

    invalidInputs.forEach {
        test("checkMul - invalid - $it") {
            val input = prefix + it.map { it.code.toLong() }
            val params = listOf<Long>(prefix.size.toLong())
            runner.run(program, input + params) shouldBe input + listOf(0L)
        }
    }
})

class DoTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { checkDo() })

    val prefix = listOf<Long>(4, 2)

    val validInputs = listOf(
        "do()",
    )

    val invalidInputs = listOf(
        "da",
        "do(a",
        "don't()",
        "a",
        "mua",
        "mula",
        "mul(123!456)",
        "mul(789,012]",
        "mul[000,000)",
        "mul(0,)",
        "mul(00,)",
        "mul(000,)",
        "mul(,0)",
        "mul(,00)",
        "mul(,000)",
        "mul(123,12a)",
        "mul(123,02a)",
        "mul(123,456)",
        "mul(789,012)",
        "mul(000,0)",
        "mul(000,00)",
        "mul(000,000)",
        "mul(00,0)",
        "mul(00,00)",
        "mul(00,000)",
        "mul(0,0)",
        "mul(0,00)",
        "mul(0,000)",
    )

    validInputs.forEach {
        test("checkDo - valid $it") {
            val input = prefix + it.map { it.code.toLong() }
            val params = listOf<Long>(prefix.size.toLong())
            runner.run(program, input + params) shouldBe input + listOf(1L)
        }
    }

    invalidInputs.forEach {
        test("checkDo - invalid - $it") {
            val input = prefix + it.map { it.code.toLong() }
            val params = listOf<Long>(prefix.size.toLong())
            runner.run(program, input + params) shouldBe input + listOf(0L)
        }
    }
})

class DontTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { checkDont() })

    val prefix = listOf<Long>(4, 2)

    val validInputs = listOf(
        "don't()",
    )

    val invalidInputs = listOf(
        "do()",
        "da",
        "do(a",
        "don't(a",
        "don't(!",
        "dont()",
        "don't(!",
        "a",
        "mua",
        "mula",
        "mul(123!456)",
        "mul(789,012]",
        "mul[000,000)",
        "mul(0,)",
        "mul(00,)",
        "mul(000,)",
        "mul(,0)",
        "mul(,00)",
        "mul(,000)",
        "mul(123,12a)",
        "mul(123,02a)",
        "mul(123,456)",
        "mul(789,012)",
        "mul(000,0)",
        "mul(000,00)",
        "mul(000,000)",
        "mul(00,0)",
        "mul(00,00)",
        "mul(00,000)",
        "mul(0,0)",
        "mul(0,00)",
        "mul(0,000)",
    )

    validInputs.forEach {
        test("checkDo - valid $it") {
            val input = prefix + it.map { it.code.toLong() }
            val params = listOf<Long>(prefix.size.toLong())
            runner.run(program, input + params) shouldBe input + listOf(1L)
        }
    }

    invalidInputs.forEach {
        test("checkDo - invalid - $it") {
            val input = prefix + it.map { it.code.toLong() }
            val params = listOf<Long>(prefix.size.toLong())
            runner.run(program, input + params) shouldBe input + listOf(0L)
        }
    }
})
