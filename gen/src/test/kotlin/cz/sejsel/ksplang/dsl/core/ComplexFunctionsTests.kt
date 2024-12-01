package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComplexFunctionsTests : FunSpec({
    test("if zero else dsl") {
        val a = complex {
            ifZero {
                CS()
                inc()
            } orIfNonZero {
                CS()
                pop()
            }
        }

        a shouldBe ComplexFunction(
            IfZero(
                mutableListOf(CS, inc),
                ComplexFunction(CS, pop),
            )
        )
    }
})