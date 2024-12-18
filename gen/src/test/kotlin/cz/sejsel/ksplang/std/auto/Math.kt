package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class MathTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("add") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                val b = variable("b", 2)
                val c = variable("c")
                add(a, b) { setTo(c) }
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
                subabs(a, b) { setTo(c) }
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
                mul(a, b) { setTo(c) }
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
                cursedDiv(a, b) { setTo(c) }
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
                div(a, b) { setTo(c) }
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
                min2(a, b) { setTo(c) }
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
                max2(a, b) { setTo(c) }
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(2)
    }

    test("negate") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                negate(a) { setTo(a) }
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-1)
    }

    test("negate var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                a.negate()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(-1)
    }

    test("add var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 1)
                a.add(18)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(19)
    }

    test("mul var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 2)
                a.mul(18)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(36)
    }

    test("div var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 18)
                a.div(2)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(9)
    }

    test("sgn") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 18)
                val b = variable("b")
                sgn(a) { setTo(b) }
                keepOnly(b)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("sgn var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 18)
                a.sgn()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("abs var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", -18)
                a.abs()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(18)
    }

    test("zeroNot var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 0)
                a.zeroNot()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }

    test("zeroNot var 2") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 15)
                a.zeroNot()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0)
    }

    test("inc var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 15)
                a.inc()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(16)
    }

    test("dec var") {
        val program = builder.build(buildComplexFunction {
            auto {
                val a = variable("a", 15)
                a.dec()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(14)
    }
})

