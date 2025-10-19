package cz.sejsel.ksplang.wasm.property

import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.wasm.ModuleTranslatorState
import cz.sejsel.ksplang.wasm.bitsToLong
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.mockk.mockk
import cz.sejsel.ksplang.wasm.WasmFunctionScope.Companion.initialize as initializeScope

class I32Tests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val globalState: ModuleTranslatorState = mockk()

    context("i32Add") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList(), globalState)
            with(scope) {
                getLocal(0)
                getLocal(1)
                i32Add()
            }
        }
        val ksplang = builder.build(function)

        test("i32add(a, b) should equal to (a + b) - uint") {
            checkAll<UInt, UInt> { a, b ->
                val input = listOf(a.toLong(), b.toLong())
                runner.run(ksplang, input) shouldBe input + (a + b).bitsToLong()
            }
        }

        test("i32add(a, b) should equal to (a + b) - int") {
            checkAll<Int, Int> { a, b ->
                val input = listOf(a.toLong(), b.toLong())
                // toLong does sign extension, we do not want that
                val expected = (a + b).bitsToLong()
                runner.run(ksplang, input) shouldBe input + expected
            }
        }
    }

    context("i32Sub") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList(), globalState)
            with(scope) {
                getLocal(0)
                getLocal(1)
                i32Sub()
            }
        }
        val ksplang = builder.build(function)

        test("i32sub(a, b) should equal to (a - b) - uint") {
            checkAll<UInt, UInt> { a, b ->
                val input = listOf(a.toLong(), b.toLong())
                runner.run(ksplang, input) shouldBe input + (a - b).bitsToLong()
            }
        }

        test("i32sub(a, b) should equal to (a - b) - int") {
            checkAll<Int, Int> { a, b ->
                val input = listOf(a.toLong(), b.toLong())
                // toLong does sign extension, we do not want that
                val expected = (a - b).bitsToLong()
                runner.run(ksplang, input) shouldBe input + expected
            }
        }
    }

    context("i32Mul") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList(), globalState)
            with(scope) {
                getLocal(0)
                getLocal(1)
                i32Mul()
            }
        }
        val ksplang = builder.build(function)

        test("i32mul(a, b) should equal to (a * b) - uint") {
            checkAll<UInt, UInt> { a, b ->
                val input = listOf(a.toLong(), b.toLong())
                runner.run(ksplang, input) shouldBe input + (a * b).bitsToLong()
            }
        }

        test("i32mul(a, b) should equal to (a * b) - int") {
            checkAll<Int, Int> { a, b ->
                val input = listOf(a.toLong(), b.toLong())
                // toLong does sign extension, we do not want that
                val expected = (a * b).bitsToLong()
                runner.run(ksplang, input) shouldBe input + expected
            }
        }
    }
})