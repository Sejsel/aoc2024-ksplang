package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.gt
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly


class CmpTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("cmp") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to cmp(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-1)
    }

    test("cmp 2") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 2)
                val b = variable("b", 1)
                val c = variable("c")
                set(c) to cmp(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("isInRange") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 2)
                val b = variable("b", 1)
                val c = variable("c")
                set(c) to isInRange(a, b, variable("d", 3))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }
})


class ComparisonTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val pairs = listOf(
        0L to 0L,
        1L to -1L,
        -1L to 0L,
        0L to 1L,
        42L to 15L,
        Long.MAX_VALUE to Long.MAX_VALUE,
        Long.MIN_VALUE to Long.MIN_VALUE,
        Long.MIN_VALUE to Long.MAX_VALUE,
        Long.MAX_VALUE to Long.MIN_VALUE,
    )

    context("gt") {
        withData(pairs) { (a, b) ->
            val expectedResult = if (a > b) 1L else 0L
            val program = builder.build(buildComplexFunction {
                auto("a", "b") {  a, b ->
                    keepOnly(gt(a, b))
                }
            })

            runner.run(program, listOf(a, b)) shouldContainExactly listOf(expectedResult)
        }
    }

    context("geq") {
        withData(pairs) { (a, b) ->
            val expectedResult = if (a >= b) 1L else 0L
            val program = builder.build(buildComplexFunction {
                auto("a", "b") {  a, b ->
                    keepOnly(geq(a, b))
                }
            })

            runner.run(program, listOf(a, b)) shouldContainExactly listOf(expectedResult)
        }
    }

    context("lt") {
        withData(pairs) { (a, b) ->
            val expectedResult = if (a < b) 1L else 0L
            val program = builder.build(buildComplexFunction {
                auto("a", "b") {  a, b ->
                    keepOnly(gt(b, a))
                }
            })

            runner.run(program, listOf(a, b)) shouldContainExactly listOf(expectedResult)
        }
    }

    context("leq") {
        withData(pairs) { (a, b) ->
            val expectedResult = if (a <= b) 1L else 0L
            val program = builder.build(buildComplexFunction {
                auto("a", "b") {  a, b ->
                    keepOnly(geq(b, a))
                }
            })

            runner.run(program, listOf(a, b)) shouldContainExactly listOf(expectedResult)
        }
    }
})
