package cz.sejsel.ksplang.wasm.chicorycomparison

import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.tools.wasm.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.wasm.bitsToLong
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import cz.sejsel.ksplang.wasm.WasmFunctionScope.Companion.initialize as initializeScope

class I32ChicoryTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

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

        val chicoryModule = createWasmModuleFromWat($$"""
            (module (func $add (export "fun") (param $a i32) (param $b i32) (result i32)
                local.get $a
                local.get $b
                i32.add
            ))""".trimIndent()
        )

        val store = Store()
        val func = store.instantiate("fun", chicoryModule).export("fun")

        test("chicory result should equal ksplang result") {
            checkAll<Int, Int> { a, b ->
                val input = listOf(a.bitsToLong(), b.bitsToLong())
                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.dropLast(1) shouldBe input
                // Note that the upper bits do not match between ksplang and chicory.
                // Chicory may have sign extension (1111... in all upper 32 bits),
                // while ksplang always maintains zeros in the bits.
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i32Sub") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList())
            with(scope) {
                getLocal(0)
                getLocal(1)
                i32Sub()
            }
        }
        val ksplang = builder.build(function)

        val chicoryModule = createWasmModuleFromWat($$"""
            (module (func $add (export "fun") (param $a i32) (param $b i32) (result i32)
                local.get $a
                local.get $b
                i32.sub
            ))""".trimIndent()
        )

        val store = Store()
        val func = store.instantiate("fun", chicoryModule).export("fun")

        test("chicory result should equal ksplang result") {
            checkAll<Int, Int> { a, b ->
                val input = listOf(a.bitsToLong(), b.bitsToLong())
                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.dropLast(1) shouldBe input
                // Note that the upper bits do not match between ksplang and chicory.
                // Chicory may have sign extension (1111... in all upper 32 bits),
                // while ksplang always maintains zeros in the bits.
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i32Mul") {
        val function = buildComplexFunction {
            val scope = initializeScope(listOf(ValType.I32, ValType.I32), listOf(), emptyList())
            with(scope) {
                getLocal(0)
                getLocal(1)
                i32Mul()
            }
        }
        val ksplang = builder.build(function)

        val chicoryModule = createWasmModuleFromWat($$"""
            (module (func $add (export "fun") (param $a i32) (param $b i32) (result i32)
                local.get $a
                local.get $b
                i32.mul
            ))""".trimIndent()
        )

        val store = Store()
        val func = store.instantiate("fun", chicoryModule).export("fun")

        test("chicory result should equal ksplang result") {
            checkAll<Int, Int> { a, b ->
                val input = listOf(a.bitsToLong(), b.bitsToLong())
                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.dropLast(1) shouldBe input
                // Note that the upper bits do not match between ksplang and chicory.
                // Chicory may have sign extension (1111... in all upper 32 bits),
                // while ksplang always maintains zeros in the bits.
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }
})

/*
fun createWasmModule(): WasmModule {
    // Module with one function
    val functionType = FunctionType.of(listOf(ValType.I32, ValType.I32), listOf(ValType.I32))
    val exportedFunctionName = "fun"
    val functionBody = FunctionBody(emptyList(), AnnotatedInstruction.builder().from(Instruction()))

    return WasmModule.builder()
        .setTypeSection(TypeSection.builder().addFunctionType(functionType).build())
        .setExportSection(ExportSection.builder().addExport(Export(exportedFunctionName, 0, ExternalType.FUNCTION)).build())
        .setCodeSection(CodeSection.builder().addFunctionBody())
        .build()
}
 */

fun createWasmModuleFromWat(wat: String): WasmModule {
    val bytes = Wat2Wasm.parse(wat)
    return Parser.parse(bytes)
}