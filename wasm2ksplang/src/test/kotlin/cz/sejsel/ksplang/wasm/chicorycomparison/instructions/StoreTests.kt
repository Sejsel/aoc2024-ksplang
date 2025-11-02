package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To0
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.bitsToLong
import cz.sejsel.ksplang.wasm.instantiateModuleFromWat
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.of

class StoreTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    context("i32.store") {
        withData(listOf("i32.store", "i32.store8", "i32.store16")) { storeInstruction ->
            val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "store") (param $index i32) (param $value i32)
                local.get $index
                local.get $value
                $$storeInstruction
            )
        )""".trimIndent()

            val store = Store()
            val module = instantiateModuleFromWat(translator, wat, "test", store)
            val program = buildSingleModuleProgram(module) {
                val store = getExportedFunction("store") as ProgramFunction2To0

                body {
                    call(store)
                }
            }
            val ksplang = builder.buildAnnotated(program).toRunnableProgram()

            test("chicory gets the same result") {
                checkAll<Long, Int>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 5648), Arb.int()) { i, value ->
                    val input = listOf(i, value.bitsToLong())

                    val referenceStore = Store()
                    val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("store")!!
                    func.apply(*input.toLongArray())
                    val chicoryMemory = referenceStore.toImportValues().memories().single()
                    val expectedBytes = chicoryMemory.memory().readBytes(0, 65536)

                    val result = runner.run(ksplang, input)
                    // We expect layout to be as follows (no fun table, no globals):
                    // 0 input_len mem_size mem_max_size [memory] [input]
                    val memory = result.subList(4, 4 + 65536)
                    memory.forEachIndexed { index, value ->
                        val expected = expectedBytes[index].toUByte().toLong()
                        value shouldBe expected
                        value shouldBeLessThan 256L // just a sanity check really
                    }
                }
            }
        }
    }

    context("i64.store") {
        withData(listOf("i64.store", "i64.store8", "i64.store16", "i64.store32")) { storeInstruction ->
            val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "store") (param $index i32) (param $value i64)
                local.get $index
                local.get $value
                $$storeInstruction
            )
        )""".trimIndent()

            val store = Store()
            val module = instantiateModuleFromWat(translator, wat, "test", store)
            val program = buildSingleModuleProgram(module) {
                val store = getExportedFunction("store") as ProgramFunction2To0

                body {
                    call(store)
                }
            }
            val ksplang = builder.buildAnnotated(program).toRunnableProgram()

            test("chicory gets the same result") {
                checkAll<Long, Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 5648), Arb.long()) { i, value ->
                    val input = listOf(i, value)

                    val referenceStore = Store()
                    val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("store")!!
                    func.apply(*input.toLongArray())
                    val chicoryMemory = referenceStore.toImportValues().memories().single()
                    val expectedBytes = chicoryMemory.memory().readBytes(0, 65536)

                    val result = runner.run(ksplang, input)
                    // We expect layout to be as follows (no fun table, no globals):
                    // 0 input_len mem_size mem_max_size [memory] [input]
                    val memory = result.subList(4, 4 + 65536)
                    memory.forEachIndexed { index, value ->
                        val expected = expectedBytes[index].toUByte().toLong()
                        value shouldBe expected
                        value shouldBeLessThan 256L // just a sanity check really
                    }
                }
            }
        }
    }

    context("f32.store") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "store") (param $index i32) (param $value f32)
                local.get $index
                local.get $value
                f32.store
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val store = getExportedFunction("store") as ProgramFunction2To0

            body {
                call(store)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long, Int>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 5648), Arb.int()) { i, value ->
                val input = listOf(i, value.bitsToLong())

                val referenceStore = Store()
                val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("store")!!
                func.apply(*input.toLongArray())
                val chicoryMemory = referenceStore.toImportValues().memories().single()
                val expectedBytes = chicoryMemory.memory().readBytes(0, 65536)

                val result = runner.run(ksplang, input)
                // We expect layout to be as follows (no fun table, no globals):
                // 0 input_len mem_size mem_max_size [memory] [input]
                val memory = result.subList(4, 4 + 65536)
                memory.forEachIndexed { index, value ->
                    val expected = expectedBytes[index].toUByte().toLong()
                    value shouldBe expected
                    value shouldBeLessThan 256L // just a sanity check really
                }
            }
        }
    }

    context("f64.store") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 1)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $load (export "store") (param $index i32) (param $value f64)
                local.get $index
                local.get $value
                f64.store
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val store = getExportedFunction("store") as ProgramFunction2To0

            body {
                call(store)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Long, Long>(Exhaustive.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 5648), Arb.long()) { i, value ->
                val input = listOf(i, value)

                val referenceStore = Store()
                val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("store")!!
                func.apply(*input.toLongArray())
                val chicoryMemory = referenceStore.toImportValues().memories().single()
                val expectedBytes = chicoryMemory.memory().readBytes(0, 65536)

                val result = runner.run(ksplang, input)
                // We expect layout to be as follows (no fun table, no globals):
                // 0 input_len mem_size mem_max_size [memory] [input]
                val memory = result.subList(4, 4 + 65536)
                memory.forEachIndexed { index, value ->
                    val expected = expectedBytes[index].toUByte().toLong()
                    value shouldBe expected
                    value shouldBeLessThan 256L // just a sanity check really
                }
            }
        }
    }
})