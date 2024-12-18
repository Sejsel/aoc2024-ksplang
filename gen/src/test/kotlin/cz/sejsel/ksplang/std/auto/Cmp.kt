package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
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
                cmp(a, b) { setTo(c) }
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
                cmp(a, b) { setTo(c) }
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
                isInRange(a, b, variable("d", 3)) { setTo(c) }
                keepOnly(c)
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1)
    }
})

