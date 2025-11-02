package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To0
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.std.dup
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

class MemoryTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    context("memory.grow") {
        val wat = $$"""
        (module 
            (memory (export "mem") 1 4)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $grow (export "grow") (param $by i32) (result i32)
                local.get $by
                memory.grow
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val grow = getExportedFunction("grow") as ProgramFunction1To1

            body {
                dup()
                call(grow)
            }
        }
        val annotated = builder.buildAnnotated(program)
        val ksplang = annotated.toRunnableProgram()

        withData(listOf(-1, 0, 1, 2, 3, 4, 5, 22443)) { value ->
            test("chicory gets the same result") {
                val input = listOf(value.bitsToLong())

                val referenceStore = Store()
                val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("grow")!!
                val expectedResult = func.apply(*input.toLongArray()).single()
                val chicoryMemory = referenceStore.toImportValues().memories().single()
                val expectedPages = chicoryMemory.memory().pages()
                val expectedBytes = chicoryMemory.memory().readBytes(0, 65536 * expectedPages)

                val result = runner.run(ksplang, input)
                // We expect layout to be as follows (no fun table, no globals):
                // 0 input_len mem_size mem_max_size [memory] [input]

                result[2] shouldBe expectedPages
                result.last() shouldBe expectedResult
                val memory = result.subList(4, 4 + expectedPages * 65536)
                memory.forEachIndexed { index, value ->
                    val expected = expectedBytes[index].toUByte().toLong()
                    value shouldBe expected
                    value shouldBeLessThan 256L // just a sanity check really
                }
                val runtimeInput = result.subList(4 + expectedPages * 65536, 4 + expectedPages * 65536 + input.size)
                runtimeInput.map { it.toInt() } shouldBe input.map { it.toInt() }
            }
        }
    }

    context("memory.size") {
        val wat = $$"""
        (module 
            (memory (export "mem") 2 4)
            ;; 8 bytes: 
            (data (i32.const 0x0000) "\F0\0D\BE\EF\F0\CA\CC\1A")
            (func $size (export "size") (result i32)
                memory.size
            )
        )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val size = getExportedFunction("size") as ProgramFunction0To1

            body {
                call(size)
            }
        }
        val annotated = builder.buildAnnotated(program)
        val ksplang = annotated.toRunnableProgram()

        test("chicory gets the same result") {
            val input = listOf<Long>(1, 2, 3)

            val referenceStore = Store()
            val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("size")!!
            val expectedResult = func.apply(*input.toLongArray()).single()
            val chicoryMemory = referenceStore.toImportValues().memories().single()
            val expectedPages = chicoryMemory.memory().pages()
            val expectedBytes = chicoryMemory.memory().readBytes(0, 65536 * expectedPages)

            val result = runner.run(ksplang, input)
            // We expect layout to be as follows (no fun table, no globals):
            // 0 input_len mem_size mem_max_size [memory] [input]

            result[2] shouldBe expectedPages
            result.last() shouldBe expectedResult
            val memory = result.subList(4, 4 + expectedPages * 65536)
            memory.forEachIndexed { index, value ->
                val expected = expectedBytes[index].toUByte().toLong()
                value shouldBe expected
                value shouldBeLessThan 256L // just a sanity check really
            }
            val runtimeInput = result.subList(4 + expectedPages * 65536, 4 + expectedPages * 65536 + input.size)
            runtimeInput.map { it.toInt() } shouldBe input.map { it.toInt() }
        }
    }
})