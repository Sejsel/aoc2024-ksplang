package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly


class BoolTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("and 1 true 1 false") {
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

    test("and 2 true") {
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

    test("and 2 false") {
        val program = builder.build(buildComplexFunction {
            auto {
                val c = and(const(0), const(0))
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0)
    }
})

