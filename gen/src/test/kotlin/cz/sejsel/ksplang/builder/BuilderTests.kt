package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.doWhileNonNegative
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.doWhileZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.dsl.core.pushAddressOf
import cz.sejsel.ksplang.dsl.core.whileNonZero
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.std.mul
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.swap2
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class IfZeroBuilderTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        ifZero {
            inc()
        } otherwise {
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

class DoWhileZeroBuilderTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        doWhileZero {
            pop()
            CS()
        }
    }

    val program = builder.build(a)
    context("do while zero pop will pop all zeros at the end") {
        withData(1..20) {
            val stack = listOf(42L) + (0..<it).map { 0L }
            runner.run(program, stack) shouldBe listOf(42)
        }
    }
})

class DoWhileNonZeroBuilderTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        doWhileNonZero {
            pop()
            CS()
        }
    }

    val program = builder.build(a)
    context("do while nonzero zero pop will pop all nonzeros at the end") {
        withData(1..20) {
            val stack = listOf(0L) + (1L..it)
            runner.run(program, stack) shouldBe listOf(0)
        }
    }
})

class DoWhileNonNegativeBuilderTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val a = buildComplexFunction {
        doWhileNonNegative {
            pop()
            dup()
        }
    }

    val program = builder.build(a)
    context("do while nonnegative pop will pop all non-negatives at the end") {
        withData(1..20) {
            val stack = listOf(-1L) + (0L..it)
            runner.run(program, stack) shouldBe listOf(-1)
        }
    }
})

class WhileNonZeroTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("while nonzero") {
        val tree = buildComplexFunction {
            // c x
            whileNonZero {
                dec()
                swap2()
                inc()
                swap2()
                // c+1 x-1
            }
        }
        val program = builder.build(tree)

        runner.run(program, listOf<Long>(0, 10)) shouldBe listOf<Long>(10)
    }

    context("while nonnegative pop will pop all non-negatives at the end") {
        val tree = buildComplexFunction {
            whileNonZero {
                pop()
            }
        }
        val program = builder.build(tree)
        withData(0..20) {
            val stack = listOf(-1L) + (0L..it)
            runner.run(program, stack) shouldBe listOf(-1)
        }
    }
})


class FunctionTests: FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("2 functions if else") {
        val program = program {
            val max = function("max", 2, 1) {
                max2()
            }

            val gcd = function("gcd", 2, 1) {
                gcd()
            }

            body {
                ifZero(popChecked = true) {
                    call(gcd)
                } otherwise {
                    call(max)
                }
            }
        }

        val ksplang = builder.build(program)

        runner.run(ksplang, listOf(8, 4, 0)) shouldBe listOf(4)
        runner.run(ksplang, listOf(8, 4, 1)) shouldBe listOf(8)
    }

    test("factorial recursion") {
        val program = program {
            val factorial = function("factorial", 1, 1)

            factorial.setBody {
                // x
                ifZero {
                    // 0
                    push(1)
                    pop2()
                    // 1
                } otherwise {
                    // x x
                    dup()
                    dec()
                    // x x-1
                    call(factorial)
                    // x x-1!
                    mul()
                     // x * factorial(x-1)
                }
            }

            body {
                call(factorial)
            }
        }

        val ksplang = builder.build(program)

        runner.run(ksplang, listOf(3)) shouldBe listOf(6)
        runner.run(ksplang, listOf(8)) shouldBe listOf(40320)
    }

    test("push address of - padded") {
        // There is no good way to write a test for this, so we just assume the first function ends up on index 16,
        // which is the most sane default index.
        // If this test fails when adjusting function generation, feel free to update the expected value.
        val program = program {
            val max = function("max", 2, 1) {
                max2()
            }
            body {
                pushAddressOf(max)
            }
        }

        val ksplang = builder.build(program)

        runner.run(ksplang, listOf(8, 4, 0)) shouldBe listOf(8, 4, 0, 16)
    }

    test("push address of - emitted already") {
        val program = program {
            val max = function("max", 2, 1) {
                max2()
            }
            body {
                pushAddressOf(max, guaranteedEmittedAlready = true)
            }
        }

        val ksplang = builder.build(program)

        runner.run(ksplang, listOf(8, 4, 0)) shouldBe listOf(8, 4, 0, 16)
    }

    test("push address of - emitted already - throws if not emitted already") {
        val program = program {
            val max = function("max", 2, 1)
            max.setBody {
                max2()
                pushAddressOf(max, guaranteedEmittedAlready = true)
            }
            body {
                call(max)
            }
        }

        shouldThrow<IllegalStateException> {
            builder.build(program)
        }
    }
})
