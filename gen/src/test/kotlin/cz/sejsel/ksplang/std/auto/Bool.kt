package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly


class BoolTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("and(0, 1) = 0") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 0)
                val b = variable("b", 1)
                val c = and(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0)
    }

    test("and(1, 0) = 0") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                val b = variable("b", 0)
                val c = and(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0)
    }

    test("and(1, 1) = 1") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 2)
                val b = variable("b", 1)
                val c = and(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("and(0, 0) = 0") {
        val program = builder.build(buildComplexFunction {
            auto {
                val c = and(const(0), const(0))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0)
    }

    test("or(0, 0) = 0") {
        val program = builder.build(buildComplexFunction {
            auto {
                val c = or(const(0), const(0))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0)
    }

    test("or(0, 1) = 1") {
        val program = builder.build(buildComplexFunction {
            auto {
                val c = or(const(0), const(1))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("or(1, 0) = 1") {
        val program = builder.build(buildComplexFunction {
            auto {
                val c = or(const(1), const(0))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("or(1, 1) = 1") {
        val program = builder.build(buildComplexFunction {
            auto {
                val c = or(const(1), const(1))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }
})

