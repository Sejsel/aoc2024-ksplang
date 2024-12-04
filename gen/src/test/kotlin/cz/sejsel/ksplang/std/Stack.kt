package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly

class RollTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("roll(0, 0) does nothing") {
        val program = builder.build(buildFunction { roll(0, 0) })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2)
    }

    test("roll(1, 1) does nothing") {
        val program = builder.build(buildFunction { roll(1, 1) })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2)
    }

    context("roll(i, 0) does nothing") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, 0) })
            runner.run(program, stack) shouldContainExactly stack
        }
    }

    context("roll(i, 1) rotates one right") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, 1) })
            runner.run(program, stack) shouldContainExactly listOf(stack[stack.size - 1]) + stack.dropLast(1)
        }
    }

    context("roll(i, -1) rotates one left") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, -1) })
            runner.run(program, stack) shouldContainExactly stack.drop(1) + stack[0]
        }
    }

    context("roll(i, i) does nothing") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, it) })
            runner.run(program, stack) shouldContainExactly stack
        }
    }
})

class StacklenTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("stacklen on non-empty stack") {
        val program = builder.build(buildComplexFunction { stacklen() })
        val input = listOf(1L, 2L, 3L)
        runner.run(program, input) shouldContainExactly input + listOf(input.size.toLong())
    }

    test("stacklen on all CS values") {
        val program = builder.build(buildComplexFunction { stacklen() })
        val input = VALUES_PER_DIGIT_SUM
        runner.run(program, input) shouldContainExactly input + listOf(input.size.toLong())
    }

    test("stacklen on max and min values") {
        val program = builder.build(buildComplexFunction { stacklen() })
        val input = listOf(Long.MAX_VALUE, Long.MIN_VALUE)
        runner.run(program, input) shouldContainExactly input + listOf(2)
    }
})


class YoinkTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("yoink index $i") {
            val program = builder.build(buildComplexFunction { yoink() })
            runner.run(program, (1L..10L).toList() + listOf(i)) shouldContainExactly (1L..10L).toList() + listOf(i + 1)
        }
    }

    // TODO: test yoinkDestructive
})

class YeetTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("yeet index $i") {
            val program = builder.build(buildComplexFunction { yeet() })
            val input = (1L..10L).toList() + listOf(-1, i)
            val output = (1L..10L).toMutableList().apply {
                set(i.toInt(), -1)
            }.toList()
            runner.run(program, input) shouldContainExactly output
        }
    }
})

class LeaveTopTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("leaveTop $i") {
            val program = builder.build(buildComplexFunction { leaveTop() })
            val input = (0L..i).toList() + listOf(42L)
            runner.run(program, input) shouldContainExactly listOf(42L)
        }
    }
})

class PopNTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("popN $i") {
            val program = builder.build(buildComplexFunction { popN() })
            val input = (1..10).map { 42L } + listOf(i)
            runner.run(program, input) shouldContainExactly (1..(10 - i)).map { 42L }
        }
    }
})