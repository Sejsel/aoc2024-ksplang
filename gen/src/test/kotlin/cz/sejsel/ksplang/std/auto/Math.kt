package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class MathTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("add") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to add(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(3)
    }

    test("subabs") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to subabs(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("mul") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", -8)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to mul(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-16)
    }

    test("cursedDiv") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", -8)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to cursedDiv(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-4)
    }

    test("div") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", -8)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to div(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-4)
    }

    test("min2") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", -8)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to min2(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-8)
    }

    test("max2") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", -8)
                val b = variable("b", 2)
                val c = variable("c")
                set(c) to max2(a, b)
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(2)
    }

    test("negate") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                set(a) to negate(a)
                keepOnly(a)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-1)
    }

    test("sgn") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 18)
                val b = variable("b")
                set(b) to sgn(a)
                keepOnly(b)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("bitshift") {
        val program = builder.build(buildComplexFunction {
            auto {
                val result = bitshift(const(1), const(8))
                keepOnly(result)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(256)
    }

    test("bitnot") {
        val number = 10L
        val program = builder.build(buildComplexFunction {
            auto {
                val result = bitnot(number.const)
                keepOnly(result)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(number.inv())
    }
})

