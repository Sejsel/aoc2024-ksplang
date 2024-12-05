package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class SimpleFunctionTests : FunSpec({
    test("simple function flattening") {
        val f = SimpleFunction(
            children = mutableListOf(
                SimpleFunction(
                    children = mutableListOf(
                        CS, inc,
                        SimpleFunction(children = mutableListOf(CS, CS, inc, CS)),
                        CS, inc
                    )
                ),
            )
        )
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }

    test("simple function dsl flattening") {
        val f = buildFunction {
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
        val inner = buildFunction {
            CS()
            CS()
            inc()
            CS()
        }
        val f = buildFunction {
            CS()
            inc()
            +inner
            CS()
            inc()
        }
        f.getInstructions() shouldContainExactly listOf(CS, inc, CS, CS, inc, CS, CS, inc)
    }
})