package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComplexFunctionsTests : FunSpec({
    test("if zero else dsl") {
        val a = complexFunction {
            ifZero {
                CS()
                inc()
            } orIfNonZero {
                CS()
                pop()
            }
        }

        a shouldBe ComplexFunction(null,
            IfZero(
                mutableListOf(CS, inc),
                ComplexFunction(null, CS, pop),
            )
        )
    }
})