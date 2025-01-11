package cz.sejsel.ksplang.dsl.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.auto.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class AutoStackTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("add two numbers and keep only result") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(16)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                val result = add(first, third)

                keepOnly(result)
            }
        }

        var program = builder.build(f)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 50)
    }

    test("set var to const") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(16)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                set(third) to const(50)

                keepOnly(third)
            }
        }

        var program = builder.build(f)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 50)
    }

    test("set var to var") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(16)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                var fourth = variable("fourth", 14)
                set(third) to fourth

                keepOnly(third)
            }
        }

        var program = builder.build(f)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 14)
    }

    test("add constant") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(16)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                set(second) to add(const(32), third)

                keepOnly(second)
            }
        }

        var program = builder.build(f)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 32 + 42)
    }

    test("do while loop") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(5)
            auto("first", "second") { first, second ->
                var third = variable("third", 42)
                var minusOne = variable("minusOne", -1)

                doWhileNonZero({ third }) {
                    set(third) to add(third, minusOne)
                    set(first) to add(first, second)
                }

                keepOnly(first)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 8 + 5*42)
    }

    test("ifBool otherwise true") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(5)
            auto("first", "second") { first, second ->
                var one = variable(1)

                val result = variable()

                ifBool({ one }) {
                    set(result) to const(42)
                } otherwise {
                    set(result) to const(43)
                }

                keepOnly(result)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 42)
    }

    test("ifBool otherwise false") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(5)
            auto("first", "second") { first, second ->
                var zero = variable(0)

                val result = variable()

                ifBool({ zero }) {
                    set(result) to const(42)
                } otherwise {
                    set(result) to const(43)
                }

                keepOnly(result)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 43)
    }

    test("ifBool true") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(5)
            auto("first", "second") { first, second ->
                var one = variable(1)

                val result = variable(43)

                ifBool({ one }) {
                    set(result) to const(42)
                }

                keepOnly(result)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 42)
    }

    test("ifBool false") {
        val f = buildComplexFunction {
            push(4)
            push(8)
            push(5)
            auto("first", "second") { first, second ->
                var zero = variable(0)

                val result = variable(43)

                ifBool({ zero }) {
                    set(result) to const(42)
                }

                keepOnly(result)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 43)
    }

    test("whileNonZero lambda counter") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val i = variable(42)
                val iterations = variable(0)

                whileNonZero({ i }) {
                    set(i) to dec(i)
                    set(iterations) to add(iterations, 2)
                }

                keepOnly(iterations)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 42*2)
    }

    test("whileNonZero lambda no run") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val i = variable(false)
                val didRun = variable(false)

                whileNonZero({ i }) {
                    set(didRun) to true
                }

                keepOnly(didRun)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 0)
    }

    test("doNTimes counter") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val i = variable(42)
                val iterations = variable(0)

                doNTimes(i) {
                    set(iterations) to add(iterations, 2)
                }

                // i is kept, not changed
                keepOnly(i, iterations)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 42, 42*2)
    }

    test("doNTimes(1) i is 0") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val lastIValue = variable(0)

                doNTimes(variable(1)) { i ->
                    set(lastIValue) to i
                }

                keepOnly(lastIValue)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 0)
    }

    test("doNTimes(2) last i is 1") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val lastIValue = variable(0)

                doNTimes(variable(2)) { i ->
                    set(lastIValue) to i
                }

                keepOnly(lastIValue)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 1)
    }

    test("doNTimes(8) last i is 7") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val lastIValue = variable(0)

                doNTimes(variable(8)) { i ->
                    set(lastIValue) to i
                }

                keepOnly(lastIValue)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 7)
    }

    test("doNTimes(0) does not run") {
        val f = buildComplexFunction {
            push(4)
            auto {
                val i = variable(0)
                val didRun = variable(false)

                doNTimes(i) {
                    set(didRun) to true
                }

                keepOnly(didRun)
            }
        }

        println(f)

        var program = builder.build(f)
        println(program)
        runner.run(program, listOf(-1)) shouldContainExactly listOf<Long>(-1, 4, 0)
    }
})