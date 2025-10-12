package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Store
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.bitsToLong
import cz.sejsel.ksplang.wasm.chicorycomparison.createWasmModuleFromWat
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropertyContext
import io.kotest.property.arbitrary.bind
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
        val ksplangModule = translator.translate("test", chicoryModule)

        val store = Store()
        val func = store.instantiate("mod", chicoryModule).export(exportedFunctionName)!!

        val program = program {
            with(ksplangModule) { installFunctions() }
            val function = with(ksplangModule) { getExportedFunction(exportedFunctionName)!! }

            body {
                call(function)
            }
        }

        val ksplang = builder.buildAnnotated(program)
        return Pair(func, ksplang.toRunnableProgram())
    }

    fun checkIntResultUnary(
        func: ExportFunction,
        ksplang: String
    ): suspend PropertyContext.(Int) -> Unit = { a ->
        val input = listOf(a.bitsToLong())
        val expected = func.apply(*input.toLongArray()).single()
        val result = runner.run(ksplang, input)
        // Note that the upper bits do not match between ksplang and chicory.
        // Chicory may have sign extension (1111... in all upper 32 bits),
        // while ksplang always maintains zeros in the bits.
        result.last().toInt() shouldBe expected.toInt()
    }

    fun checkUIntResultUnary(
        func: ExportFunction,
        ksplang: String
    ): suspend PropertyContext.(UInt) -> Unit = { a ->
        val input = listOf(a.bitsToLong())
        val expected = func.apply(*input.toLongArray()).single()
        val result = runner.run(ksplang, input)
        // Note that the upper bits do not match between ksplang and chicory.
        // Chicory may have sign extension (1111... in all upper 32 bits),
        // while ksplang always maintains zeros in the bits.
        result.last().toInt() shouldBe expected.toInt()
    }

    fun checkIntResultBinary(
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

    fun checkUIntResultBinary(
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
            checkAll<Int>(checkIntResultUnary(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllU32(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - uint") {
            checkAll<UInt>(checkUIntResultUnary(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllI32Pairs(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - int, int") {
            checkAll<Int, Int>(checkIntResultBinary(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllU32Pairs(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - uint, uint") {
            checkAll<UInt, UInt> (checkUIntResultBinary(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllI32PairsWithFilter(
        func: ExportFunction,
        ksplang: String,
        filter: (Int, Int) -> Boolean
    ) {
        val arb = Arb.bind(Arb.int(), Arb.int()) { a, b -> Pair(a, b) }.filter { filter(it.first, it.second) }
        this.test("chicory result should equal ksplang result - int, int with filter") {
            checkAll(arb) {
                checkIntResultBinary(func, ksplang)(it.first, it.second)
            }
        }
    }

    suspend fun FunSpecContainerScope.checkAllU32PairsWithFilter(
        func: ExportFunction,
        ksplang: String,
        filter: (UInt, UInt) -> Boolean
    ) {
        val arb = Arb.bind(Arb.uInt(), Arb.uInt()) { a, b -> Pair(a, b) }.filter { filter(it.first, it.second) }
        this.test("chicory result should equal ksplang result - uint, uint with filter") {
            checkAll(arb) {
                checkUIntResultBinary(func, ksplang)(it.first, it.second)
            }
        }
    }

    fun prepareI32UnaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $fun (export "fun") (param $a i32) (result i32)
                    local.get $a
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    fun prepareI32BinaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $fun (export "fun") (param $a i32) (param $b i32) (result i32)
                    local.get $a
                    local.get $b
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    context("i32.add") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.add")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.sub") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.sub")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.mul") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.mul")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.div_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.div_s")
        // Partial: division by zero and overflow case are undefined
        checkAllI32PairsWithFilter(func, ksplang) { a, b -> b != 0 && !(b == -1 && a == Int.MIN_VALUE) }
        checkAllU32PairsWithFilter(func, ksplang) { a, b -> b != 0U && !(b.toInt() == -1 && a.toInt() == Int.MIN_VALUE) }
    }

    context("i32.div_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.div_u")
        // Partial: division by zero is undefined
        checkAllI32PairsWithFilter(func, ksplang) { _, b -> b != 0 }
        checkAllU32PairsWithFilter(func, ksplang) { _, b -> b != 0U }
    }

    context("i32.rem_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.rem_u")
        // Partial: division by zero is undefined
        checkAllI32PairsWithFilter(func, ksplang) { _, b -> b != 0 }
        checkAllU32PairsWithFilter(func, ksplang) { _, b -> b != 0U }
    }

    context("i32.rem_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.rem_s")
        // Partial: division by zero is undefined
        checkAllI32PairsWithFilter(func, ksplang) { _, b -> b != 0 }
        checkAllU32PairsWithFilter(func, ksplang) { _, b -> b != 0U }
    }

    context("i32.and") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.and")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.or") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.or")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.xor") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.xor")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.shl") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.shl")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.shr_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.shr_s")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.shr_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.shr_u")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.rotl") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.rotl")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.rotr") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.rotr")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.clz") {
        val (func, ksplang) = prepareI32UnaryFunModule("i32.clz")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.ctz") {
        val (func, ksplang) = prepareI32UnaryFunModule("i32.ctz")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.popcnt") {
        val (func, ksplang) = prepareI32UnaryFunModule("i32.popcnt")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.eqz") {
        val (func, ksplang) = prepareI32UnaryFunModule("i32.eqz")
        checkAllI32(func, ksplang)
        checkAllU32(func, ksplang)
    }

    context("i32.eq") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.eq")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.ne") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.ne")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.lt_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.lt_s")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.lt_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.lt_u")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.gt_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.gt_s")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.gt_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.gt_u")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.le_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.le_s")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.le_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.le_u")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.ge_s") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.ge_s")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }

    context("i32.ge_u") {
        val (func, ksplang) = prepareI32BinaryFunModule("i32.ge_u")
        checkAllI32Pairs(func, ksplang)
        checkAllU32Pairs(func, ksplang)
    }
})