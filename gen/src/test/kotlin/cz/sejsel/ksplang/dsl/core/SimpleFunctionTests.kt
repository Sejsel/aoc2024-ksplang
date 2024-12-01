package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class SimpleFunctionTests : FunSpec({
    test("simple function flattening") {
        val f = SimpleFunction(
            listOf(
                SimpleFunction(
                    listOf(
                        CS, inc,
                        SimpleFunction(
                            listOf(
                                CS, CS, inc, CS
                            )
                        ),
                        CS, inc
                    )
                ),
            )
        )
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }

    test("simple function dsl flattening") {
        val f = function {
            function {
                CS()
                inc()
                function {
                    CS()
                    CS()
                    inc()
                    CS()
                }
                CS()
                inc()
            }
        }
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }

    test("simple function insertion with unaryPlus") {
        val inner = function {
            CS()
            CS()
            inc()
            CS()
        }
        val f = function {
            function {
                CS()
                inc()
                +inner
                CS()
                inc()
            }
        }
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }
})