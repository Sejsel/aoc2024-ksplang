package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import cz.sejsel.ksplang.wasm.WasmFunctionScope.Companion.initialize as initializeScope

class WasmFunctionScopeTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("initializing non-parameter local should default to 0") {
        val function = buildComplexFunction {
            initializeScope(listOf(ValType.I32, ValType.I32), listOf(ValType.I32), emptyList())
        }

        val ksplang = builder.build(function)
        runner.run(ksplang, listOf(42, 11)) shouldBe listOf(42, 11, 0)
    }

    test("pop locals removes locals but keeps return value") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(ValType.I32), listOf(ValType.I32))
            with(scope) {
                getLocal(1)
                popLocals()
            }
        }

        val ksplang = builder.build(function)
        runner.run(ksplang, listOf(42, 11)) shouldBe listOf(11)
    }


    test("getLocal 0") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList())
            with(scope) {
                getLocal(0)
            }
        }

        val ksplang = builder.build(function)
        runner.run(ksplang, listOf(42, 11)) shouldBe listOf(42, 11, 42)
    }

    test("getLocal 1") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList())
            with(scope) {
                getLocal(1)
            }
        }

        val ksplang = builder.build(function)
        runner.run(ksplang, listOf(42, 11)) shouldBe listOf(42, 11, 11)
    }

    test("getLocal of non-parameter local") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(ValType.I32), emptyList())
            with(scope) {
                getLocal(2)
            }
        }

        val ksplang = builder.build(function)
        runner.run(ksplang, listOf(42, 11)) shouldBe listOf(42, 11, 0, 0)
    }

    test("getLocal twice") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList())
            with(scope) {
                getLocal(1)
                getLocal(0)
            }
        }

        val ksplang = builder.build(function)
        runner.run(ksplang, listOf(42, 11)) shouldBe listOf(42, 11, 11, 42)
    }

    context("i32Add") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList())
            with(scope) {
                getLocal(0)
                getLocal(1)
                i32Add()
            }
        }
        val ksplang = builder.build(function)

        test("simple test") {
            runner.run(ksplang, listOf(42, 11)) shouldBe listOf(42, 11, 42 + 11)
        }

        withData(
            nameFn = { (a: UInt, b: UInt) -> "$a + $b" },
            listOf(
                0u to 0u,
                1u to 1u,
                UInt.MAX_VALUE to 0u,
                0u to UInt.MAX_VALUE,
                UInt.MAX_VALUE to UInt.MAX_VALUE,
                UInt.MAX_VALUE / 2u to UInt.MAX_VALUE / 2u + 1u
            )
        ) { (a, b) ->
            val expected = (a + b).toLong()
            val input = listOf(a.toLong(), b.toLong())
            runner.run(ksplang, input) shouldBe input + expected
        }
    }
})
