package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromWat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.of

class MemoryTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    context("i32.load") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i32)
                local.get $index
                i32.load
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i64.load") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i64)
                local.get $index
                i64.load
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i32.load8_u") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i32)
                local.get $index
                i32.load8_u
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i64.load8_u") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i64)
                local.get $index
                i64.load8_u
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i32.load16_u") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i32)
                local.get $index
                i32.load16_u
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i64.load16_u") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i64)
                local.get $index
                i64.load16_u
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }

    context("i64.load32_u") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "load") (param $index i32) (result i64)
                local.get $index
                i64.load32_u
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val load = getExportedFunction("load") as ProgramFunction1To1

            body {
                call(load)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8)) { i ->
                val input = listOf(i)
                val func = store.instantiate("mod", module.module.chicoryModule).export("load")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.last().toInt() shouldBe expected.toInt()
            }
        }
    }
})

