package cz.sejsel.ksplang.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class KsplangProgramTests : FunSpec({
    test("body name should be ksplang_program_body") {
        val program = program {
            body {
                inc()
            }
        }

        val body = program.body.shouldNotBeNull()
        body.name shouldBe "ksplang_program_body"
    }

    test("program one function and call") {
        val program = program {
            val max = function("max", args = 2, outputs = 1) {
                max2()
            }

            body {
                call(max)
            }
        }

        program.definedFunctions shouldHaveSize 1
        val maxFun = program.definedFunctions.find { it.name == "max" }.shouldNotBeNull()
        maxFun.name shouldBe "max"
        maxFun.body shouldBe ComplexFunction(
            name = "ksplang_program_function_max", max2
        )
        maxFun.args shouldBe 2
        maxFun.outputs shouldBe 1

        val body = program.body.shouldNotBeNull()
        body.name shouldBe "ksplang_program_body"
        body.children shouldHaveSize 1
        val functionCall = body.children[0].shouldBeTypeOf<FunctionCall>()
        functionCall.calledFunction shouldBe maxFun
    }

    test("program two functions called in if else") {
        val program = program {
            val max = function("max", args = 2, outputs = 1) {
                max2()
            }

            val gcd = function("gcd", args = 2, outputs = 1) {
                gcd()
            }

            body {
                ifZero {
                    call(gcd)
                } otherwise {
                    call(max)
                }
            }
        }

        program.definedFunctions shouldHaveSize 2
        val maxFun = program.definedFunctions.find { it.name == "max" }.shouldNotBeNull()
        val gcdFun = program.definedFunctions.find { it.name == "gcd" }.shouldNotBeNull()
        maxFun.name shouldBe "max"
        maxFun.body shouldBe ComplexFunction(
            name = "ksplang_program_function_max", max2
        )
        gcdFun.name shouldBe "gcd"
        gcdFun.body shouldBe ComplexFunction(
            name = "ksplang_program_function_gcd", gcd
        )

        val body = program.body.shouldNotBeNull()
        body.children shouldHaveSize 1

        val ifZero = body.children[0].shouldBeTypeOf<IfZero>()
        ifZero.children shouldHaveSize 1
        val gcdCall = ifZero.children[0].shouldBeTypeOf<FunctionCall>()
        gcdCall.calledFunction shouldBe gcdFun

        val orElse = ifZero.orElse.shouldNotBeNull()
        orElse.children shouldHaveSize 1
        val maxCall = orElse.children[0].shouldBeTypeOf<FunctionCall>()
        maxCall.calledFunction shouldBe maxFun
    }

    test("program with one function calling another") {
        val program = program {
            val a = function("a", args = 1, outputs = 1) {
                inc()
            }

            val b = function("b", args = 1, outputs = 1) {
                call(a)
            }

            body {
                call(b)
            }
        }

        program.definedFunctions shouldHaveSize 2
        val a = program.definedFunctions.find { it.name == "a" }.shouldNotBeNull()
        val b = program.definedFunctions.find { it.name == "b" }.shouldNotBeNull()

        val body = program.body.shouldNotBeNull()
        body.children shouldHaveSize 1
        val functionCall = body.children[0].shouldBeTypeOf<FunctionCall>()
        functionCall.calledFunction shouldBe b

        a.name shouldBe "a"
        a.body shouldBe ComplexFunction(
            name = "ksplang_program_function_a", inc
        )

        b.name shouldBe "b"
        val bFunctionCall = b.body!!.children.shouldHaveSize(1).single().shouldBeTypeOf<FunctionCall>()
        bFunctionCall.calledFunction shouldBe a
    }

    test("program with forward declaration") {
        val program = program {
            val a = function("a", args = 1, outputs = 1)

            val b = function("b", args = 1, outputs = 1) {
                call(a)
            }

            a.setBody {
                call(b)
            }

            body {
                call(a)
            }
        }

        program.definedFunctions shouldHaveSize 2
        val a = program.definedFunctions.find { it.name == "a" }.shouldNotBeNull()
        val b = program.definedFunctions.find { it.name == "b" }.shouldNotBeNull()

        a.name shouldBe "a"
        val aFunctionCall = a.body!!.children.shouldHaveSize(1).single().shouldBeTypeOf<FunctionCall>()
        aFunctionCall.calledFunction shouldBe b

        b.name shouldBe "b"
        val bFunctionCall = b.body!!.children.shouldHaveSize(1).single().shouldBeTypeOf<FunctionCall>()
        bFunctionCall.calledFunction shouldBe a
    }

    test("program with self recursion") {
        val program = program {
            val a = function("a", args = 1, outputs = 1)

            a.setBody {
                call(a)
            }

            body {
                call(a)
            }
        }

        program.definedFunctions shouldHaveSize 1
        val a = program.definedFunctions.find { it.name == "a" }.shouldNotBeNull()

        a.name shouldBe "a"
        val aFunctionCall = a.body!!.children.shouldHaveSize(1).single().shouldBeTypeOf<FunctionCall>()
        aFunctionCall.calledFunction shouldBe a
    }
})
