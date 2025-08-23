package cz.sejsel.ksplang.wasm.chicorycomparison

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.tools.wasm.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.bitsToLong
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropertyContext
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.uInt
import io.kotest.property.checkAll

class I32ChicoryTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    fun prepareModule(wat: String, exportedFunctionName: String): Pair<ExportFunction, String> {
        val chicoryModule = createWasmModuleFromWat(wat)
        val ksplangModule = translator.translate("test", chicoryModule, Store())

        val store = Store()
        val func = store.instantiate("mod", chicoryModule).export(exportedFunctionName)!!

        val program = program {
            with(ksplangModule) { installFunctions() }
            val function = with(ksplangModule) { getExportedFunction(exportedFunctionName)!! }

            body {
                call(function)
            }
        }

        val ksplang = builder.build(program)
        return Pair(func, ksplang)
    }

    fun checkIntResult(
        func: ExportFunction,
        ksplang: String
    ): suspend PropertyContext.(Int, Int) -> Unit = { a, b ->
        val input = listOf(a.bitsToLong(), b.bitsToLong())
        val expected = func.apply(*input.toLongArray()).single()
        val result = runner.run(ksplang, input)
        // Note that the upper bits do not match between ksplang and chicory.
        // Chicory may have sign extension (1111... in all upper 32 bits),
        // while ksplang always maintains zeros in the bits.
        result.last().toInt() shouldBe expected.toInt()
    }

    fun checkUIntResult(
        func: ExportFunction,
        ksplang: String
    ): suspend PropertyContext.(UInt, UInt) -> Unit = { a, b ->
        val input = listOf(a.bitsToLong(), b.bitsToLong())
        val expected = func.apply(*input.toLongArray()).single()
        val result = runner.run(ksplang, input)
        // Note that the upper bits do not match between ksplang and chicory.
        // Chicory may have sign extension (1111... in all upper 32 bits),
        // while ksplang always maintains zeros in the bits.
        result.last().toInt() shouldBe expected.toInt()
    }

    suspend fun FunSpecContainerScope.checkAllI32(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - int") {
            checkAll<Int, Int>(checkIntResult(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllU32(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - uint") {
            checkAll<UInt, UInt> (checkUIntResult(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllI32WithSecondNonZero(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - int, nonzero int") {
            checkAll(Arb.int(), Arb.int().filter { it != 0 }, checkIntResult(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllU32WithSecondNonZero(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - uint, nonzero uint") {
            checkAll(Arb.uInt(), Arb.uInt().filter { it != 0U }, checkUIntResult(func, ksplang))
        }
    }

    fun prepareI32BinaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $add (export "fun") (param $a i32) (param $b i32) (result i32)
                    local.get $a
                    local.get $b
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    context("i32.add") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.add")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.sub") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.sub")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.mul") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.mul")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.div_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.div_s")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
    }

    context("i32.div_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.div_u")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
    }

    context("i32.rem_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.rem_u")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
    }

    context("i32.rem_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.rem_s")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
    }

    context("i32.and") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.and")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
    }

    context("i32.or") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.or")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
    }

    context("i32.xor") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.xor")
        checkAllI32WithSecondNonZero(func, ksplang)
        checkAllU32WithSecondNonZero(func, ksplang)
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