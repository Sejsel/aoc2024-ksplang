package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class SimpleFunctionTests : FunSpec({
    test("simple function flattening") {
        val f = SimpleFunction(
            children = listOf(
                SimpleFunction(
                    children = listOf(
                        CS, inc,
                        SimpleFunction(children = listOf(CS, CS, inc, CS)),
                        CS, inc
                    )
                ),
            )
        )
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }

    test("simple function dsl flattening") {
        val f = function {
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
            CS()
            inc()
            +inner
            CS()
            inc()
        }
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }
})