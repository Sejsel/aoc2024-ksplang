package cz.sejsel.ksplang.dsl.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.auto.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class AutoStackTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("add two numbers and keep only result") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(16)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                add(first, third) { setTo(second) }

                keepOnly(second)
            }
        }

        var program = builder.build(f)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 50)
    }

    test("add constant") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(16)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                add(const(32), third) { setTo(second) }

                keepOnly(second)
            }
        }

        var program = builder.build(f)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 32 + 42)
    }

    test("do while loop") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(5)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                var minusOne = variable("minusOne", -1)

                doWhileNonZero(third) {
                    add(third, minusOne) { setTo(third) }
                    add(first, second) { setTo(first) }
                }

                keepOnly(first)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 8 + 5*42)
    }
})