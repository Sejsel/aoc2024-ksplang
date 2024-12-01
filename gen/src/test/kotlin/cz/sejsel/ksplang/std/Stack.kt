package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.function
import cz.sejsel.ksplang.KsplangRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly

class RollTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("roll(0, 0) does nothing") {
        val program = builder.build(function { roll(0, 0) })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2)
    }

    test("roll(1, 1) does nothing") {
        val program = builder.build(function { roll(1, 1) })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2)
    }

    context("roll(i, 0) does nothing") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(function { roll(it, 0) })
            runner.run(program, stack) shouldContainExactly stack
        }
    }

    context("roll(i, 1) rotates one right") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(function { roll(it, 1) })
            runner.run(program, stack) shouldContainExactly listOf(stack[stack.size - 1]) + stack.dropLast(1)
        }
    }

    context("roll(i, -1) rotates one left") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(function { roll(it, -1) })
            runner.run(program, stack) shouldContainExactly stack.drop(1) + stack[0]
        }
    }

    context("roll(i, i) does nothing") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(function { roll(it, it) })
            runner.run(program, stack) shouldContainExactly stack
        }
    }
})

