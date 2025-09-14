package cz.sejsel.ksplang.wasm.chicorycomparison

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Store
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropertyContext
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class I64ChicoryTests : FunSpec({
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

        val ksplang = builder.buildAnnotated(program)
        return Pair(func, ksplang.toRunnableProgram())
    }

    fun checkLongResultUnary(
        func: ExportFunction,
        ksplang: String
    ): suspend PropertyContext.(Long) -> Unit = { a ->
        val input = listOf(a)
        val expected = func.apply(*input.toLongArray()).single()
        val result = runner.run(ksplang, input)
        result.last() shouldBe expected
    }

    fun checkLongResultBinary(
        func: ExportFunction,
        ksplang: String
    ): suspend PropertyContext.(Long, Long) -> Unit = { a, b ->
        val input = listOf(a, b)
        val expected = func.apply(*input.toLongArray()).single()
        val result = runner.run(ksplang, input)
        result.last() shouldBe expected
    }

    suspend fun FunSpecContainerScope.checkAllI64(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - long") {
            checkAll<Long>(checkLongResultUnary(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllI64Pairs(func: ExportFunction, ksplang: String) {
        this.test("chicory result should equal ksplang result - long, long") {
            checkAll<Long, Long>(checkLongResultBinary(func, ksplang))
        }
    }

    suspend fun FunSpecContainerScope.checkAllI64PairsWithFilter(
        func: ExportFunction,
        ksplang: String,
        filter: (Long, Long) -> Boolean
    ) {
        val arb = Arb.bind(Arb.long(), Arb.long()) { a, b -> Pair(a, b) }.filter { filter(it.first, it.second) }
        this.test("chicory result should equal ksplang result - int, int with filter") {
            checkAll(arb) {
                checkLongResultBinary(func, ksplang)(it.first, it.second)
            }
        }
    }

    fun prepareI64toI64UnaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $fun (export "fun") (param $a i64) (result i64)
                    local.get $a
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    fun prepareI64toI32UnaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $fun (export "fun") (param $a i64) (result i32)
                    local.get $a
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    fun prepareI64toI64BinaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.get $b
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    fun prepareI64toI32BinaryFunModule(instructionName: String) = prepareModule(
        wat = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i32)
                    local.get $a
                    local.get $b
                    $$instructionName
                ))""".trimIndent(),
        exportedFunctionName = "fun"
    )

    context("i64.add") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.add")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.sub") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.sub")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.mul") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.mul")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.div_s") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.div_s")
        // Partial: division by zero and overflow case are undefined
        checkAllI64PairsWithFilter(func, ksplang) { a, b -> b != 0L && !(b == -1L && a == Long.MIN_VALUE) }
    }

    context("i64.div_u") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.div_u")
        // Partial: division by zero is undefined
        checkAllI64PairsWithFilter(func, ksplang) { _, b -> b != 0L }
    }

    context("i64.rem_u") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.rem_u")
        // Partial: division by zero is undefined
        checkAllI64PairsWithFilter(func, ksplang) { _, b -> b != 0L }
    }

    context("i64.rem_s") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.rem_s")
        // Partial: division by zero is undefined
        checkAllI64PairsWithFilter(func, ksplang) { _, b -> b != 0L }
    }

    context("i64.and") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.and")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.or") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.or")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.xor") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.xor")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.shl") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.shl")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.shr_s") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.shr_s")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.shr_u") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.shr_u")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.rotl") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.rotl")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.rotr") {
        val (func, ksplang) = prepareI64toI64BinaryFunModule("i64.rotr")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.clz") {
        val (func, ksplang) = prepareI64toI64UnaryFunModule("i64.clz")
        checkAllI64(func, ksplang)
    }

    context("i64.ctz") {
        val (func, ksplang) = prepareI64toI64UnaryFunModule("i64.ctz")
        checkAllI64(func, ksplang)
    }

    context("i64.popcnt") {
        val (func, ksplang) = prepareI64toI64UnaryFunModule("i64.popcnt")
        checkAllI64(func, ksplang)
    }

    context("i64.eqz") {
        val (func, ksplang) = prepareI64toI32UnaryFunModule("i64.eqz")
        checkAllI64(func, ksplang)
    }

    context("i64.eq") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.eq")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.ne") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.ne")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.lt_s") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.lt_s")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.lt_u") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.lt_u")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.gt_s") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.gt_s")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.gt_u") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.gt_u")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.le_s") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.le_s")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.le_u") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.le_u")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.ge_s") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.ge_s")
        checkAllI64Pairs(func, ksplang)
    }

    context("i64.ge_u") {
        val (func, ksplang) = prepareI64toI32BinaryFunModule("i64.ge_u")
        checkAllI64Pairs(func, ksplang)
    }
})