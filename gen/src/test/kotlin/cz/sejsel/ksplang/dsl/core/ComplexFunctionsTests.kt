package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComplexFunctionsTests : FunSpec({
    test("if zero else dsl") {
        val a = buildComplexFunction {
            ifZero {
                CS()
                inc()
            } otherwise {
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

    test("if zero nested dsl") {
        val a = buildComplexFunction {
            ifZero {
                CS()
                ifZero {
                    CS()
                } otherwise {
                    pop()
                }
                inc()
            } otherwise {
                CS()
                pop()
            }
        }

        a shouldBe ComplexFunction(null,
            IfZero(
                mutableListOf(
                    CS,
                    IfZero(
                        mutableListOf(CS),
                        ComplexFunction(null, pop),
                    ),
                    inc,
                ),
                ComplexFunction(null, CS, pop),
            )
        )
    }
})