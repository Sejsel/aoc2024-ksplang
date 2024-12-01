package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.complex
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.inc
import cz.sejsel.ksplang.dsl.core.orIfNonZero
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IfZeroBuilderTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val a = complex {
        ifZero {
            inc()
        } orIfNonZero {
            inc()
            inc()
        }
    }

    val program = builder.build(a)
    test("if zero - 0") {
        runner.run(program, listOf(0)) shouldBe listOf(1)
    }
    test("if zero - 1") {
        runner.run(program, listOf(1)) shouldBe listOf(3)
    }
})
