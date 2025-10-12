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
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class MiscTests : FunSpec({
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

    context("i32.const") {
        test("chicory result should equal ksplang result - int") {
            checkAll<Int> { a ->
                val program = $$"""
                (module (func $fun (export "fun") (result i32)
                    i32.const $$a
                ))""".trimIndent()
                val (func, ksplang) = prepareModule(program, "fun")

                val input = listOf(a.bitsToLong())

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }

        test("chicory result should equal ksplang result - uint") {
            checkAll<UInt> { a ->
                val program = $$"""
                (module (func $fun (export "fun") (result i32)
                    i32.const $$a
                ))""".trimIndent()
                val (func, ksplang) = prepareModule(program, "fun")

                val input = listOf(a.bitsToLong())

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i64.const") {
        test("chicory result should equal ksplang result - long") {
            checkAll<Long> { a ->
                val program = $$"""
                (module (func $fun (export "fun") (result i64)
                    i64.const $$a
                ))""".trimIndent()
                val (func, ksplang) = prepareModule(program, "fun")

                val input = listOf(a)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }
    }

    context("drop") {
        test("chicory result should equal ksplang result - long") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.get $b
                    drop
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long> { a, b ->
                val input = listOf(a, b)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }
    }

    context("select") {
        test("chicory result should equal ksplang result - long") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (param $c i32) (result i64)
                    local.get $a
                    local.get $b
                    local.get $c
                    select
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long, Int> { a, b, c ->
                val input = listOf(a, b, c.bitsToLong())

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }
    }

    context("local.set") {
        test("chicory result should equal ksplang result - changes local") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.set $b
                    local.get $b
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long> { a, b, ->
                val input = listOf(a, b)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }

        test("chicory result should equal ksplang result - other local not changed") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.set $b
                    local.get $a
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long> { a, b, ->
                val input = listOf(a, b)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }
    }

    context("local.tee") {
        test("chicory result should equal ksplang result - keeps value on stack") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.tee $b
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long> { a, b, ->
                val input = listOf(a, b)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }

        test("chicory result should equal ksplang result - other local not changed") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.tee $b
                    drop
                    local.get $a
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long> { a, b, ->
                val input = listOf(a, b)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }

        test("chicory result should equal ksplang result - local changed") {
            val program = $$"""
                (module (func $fun (export "fun") (param $a i64) (param $b i64) (result i64)
                    local.get $a
                    local.tee $b
                    drop
                    local.get $b
                ))""".trimIndent()
            val (func, ksplang) = prepareModule(program, "fun")
            checkAll<Long, Long> { a, b, ->
                val input = listOf(a, b)

                val expected = func.apply(*input.toLongArray()).single()
                val result = runner.run(ksplang, input)
                result.last() shouldBe expected
            }
        }
    }
})