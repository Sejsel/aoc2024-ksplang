package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.program
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class CallTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("call2To1") {
        val program = builder.build(program {
            val gcdFunction = function2To1("gcd") {
                gcd()
            }
            body {
                auto {
                    val c = call(gcdFunction, 9.const, 21.const)
                    keepOnly(c)
                }
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(3)
    }
})

