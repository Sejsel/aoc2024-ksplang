package cz.sejsel.ksplang.wasm.runtime.singlemodule

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To1
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromWat
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class LayoutTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    context("add with no memory or table or globals") {
        val wat = $$"""
                (module (func $fun (export "add") (param $a i32) (param $b i32) (result i32)
                    local.get $a
                    local.get $b
                    i32.add
                ))""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)

        // expected layout:
        // 0 input_len [globals] [fun_table] [input]

        test("invoking returns add") {
            val program = buildSingleModuleProgram(module) {
                val addFunction = getExportedFunction("add")!! as ProgramFunction2To1

                body {
                    // The input is at the end of the stack, so we can just call right away:
                    call(addFunction)
                }
            }
            val ksplang = builder.build(program)
            val result = runner.run(ksplang, listOf(40, 2).map { it.toLong() })
            result.last() shouldBe 42L
        }

        val emptyProgram = buildSingleModuleProgram(module) {}
        val ksplang = builder.buildAnnotated(emptyProgram).toRunnableProgram()

        test("first value is zero") {
            val result = runner.run(ksplang, listOf(40, 2).map { it.toLong() })
            result.first() shouldBe 0L
        }

        context("second value is input size") {
            withData(listOf(1, 2, 3, 42, 100)) { it ->
                val stack = List(it) { index -> index.toLong() }
                val result = runner.run(ksplang, stack)
                result[1] shouldBe it.toLong()
            }
        }

        test("input starts right after input len") {
            val result = runner.run(ksplang, listOf(1, 2, 3, 4, 5))
            result.subList(2, result.size) shouldBe listOf(1L, 2L, 3L, 4L, 5L)
            result.takeLast(5) shouldBe listOf(1L, 2L, 3L, 4L, 5L)
        }

        context("getInputSize returns input size") {
            withData(listOf(1, 2, 3, 42, 100)) { it ->
                val input = List(it) { index -> index.toLong() }
                val program = buildSingleModuleProgram(module) {
                    body {
                        getInputSize()
                    }
                }
                val ksplang = builder.build(program)
                val result = runner.run(ksplang, input)
                result.last() shouldBe input.size
            }
        }

        context("yoinkInput - dynamic") {
            withData(listOf(0, 1, 2)) { index ->
                val program = buildSingleModuleProgram(module) {
                    body {
                        push(index)
                        yoinkInput()
                    }
                }
                val input = listOf(40L, 41L, 42L)
                val ksplang = builder.build(program)
                val result = runner.run(ksplang, input)
                result.last() shouldBe input[index]
            }
        }

        context("yoinkInput - static") {
            withData(listOf(0, 1, 2)) { index ->
                val program = buildSingleModuleProgram(module) {
                    body {
                        yoinkInput(index)
                    }
                }
                val input = listOf(40L, 41L, 42L)
                val ksplang = builder.build(program)
                val result = runner.run(ksplang, input)
                result.last() shouldBe input[index]
            }
        }
    }

    context("one memory") {
        val memSize = 1
        val maxMemSize = 2
        val wat = $$"""
                (module 
                    (func $fun (export "add") (param $a i32) (param $b i32) (result i32)
                        local.get $a
                        local.get $b
                        i32.add
                    )
                    ;; initial 1 page, max 2 pages
                    (memory (export "mem") $$memSize $$maxMemSize)
                    
                    ;; 8 bytes: 
                    (data (i32.const 0x0000)
                        "\F0\0D\BE\EF\F0\CA\CC\1A"
                    )
                    
                )""".trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)

        // expected layout:
        // 0 input_len [globals] [fun_table] [mem_size mem_max_size [mem_pages]] [input]
        // 0 input_len [1 2 [240 13 190 239 240 202 204 26 0 ... 0]] [input]

        // The input is on the top, so we should be able to call right away
        test("invoking returns add") {
            val program = buildSingleModuleProgram(module) {
                val addFunction = getExportedFunction("add")!! as ProgramFunction2To1

                body {
                    // The input is at the end of the stack, so we can just call right away:
                    call(addFunction)
                }
            }

            val ksplang = builder.build(program)
            val result = runner.run(ksplang, listOf(40, 2).map { it.toLong() })
            result.last() shouldBe 42L
        }

        val emptyProgram = buildSingleModuleProgram(module) {}
        val ksplang = builder.buildAnnotated(emptyProgram).toRunnableProgram()

        test("first value is zero") {
            val result = runner.run(ksplang, listOf(40, 2).map { it.toLong() })
            result.first() shouldBe 0L
        }

        context("second value is input size") {
            withData(listOf(1, 2, 3, 42, 100)) { it ->
                val stack = List(it) { index -> index.toLong() }
                val result = runner.run(ksplang, stack)
                result[1] shouldBe it.toLong()
            }
        }

        test("third value is mem size") {
            val result = runner.run(ksplang, listOf(1, 2))
            result[2] shouldBe memSize.toLong()
        }

        test("fourth value is max mem size") {
            val result = runner.run(ksplang, listOf(1, 2))
            result[3] shouldBe maxMemSize.toLong()
        }

        test("memory starts with data sequence") {
            val result = runner.run(ksplang, listOf(1, 2))
            val expectedData = listOf(0xF0, 0x0D, 0xBE, 0xEF, 0xF0, 0xCA, 0xCC, 0x1A).map { it.toLong() }
            result.subList(4, 12) shouldBe expectedData
        }

        test("memory continues with many zeroes") {
            val expectedZeroes = List(65536 - 8) { 0L }
            val result = runner.run(ksplang, listOf(1, 2))
            result.subList(12, 12 + 65536 - 8) shouldBe expectedZeroes
        }

        test("input starts after memory") {
            val result = runner.run(ksplang, listOf(1, 2, 3, 4, 5))
            result.subList(12 + 65536 - 8, result.size) shouldBe listOf(1L, 2L, 3L, 4L, 5L)
            result.takeLast(5) shouldBe listOf(1L, 2L, 3L, 4L, 5L)
        }

        context("getInputSize returns input size") {
            withData(listOf(1, 2, 3, 42, 100)) { it ->
                val input = List(it) { index -> index.toLong() }
                val program = buildSingleModuleProgram(module) {
                    body {
                        getInputSize()
                    }
                }
                val ksplang = builder.build(program)
                val result = runner.run(ksplang, input)
                result.last() shouldBe input.size
            }
        }

        context("yoinkInput - dynamic") {
            withData(listOf(0, 1, 2)) { index ->
                val program = buildSingleModuleProgram(module) {
                    body {
                        push(index)
                        yoinkInput()
                    }
                }
                val input = listOf(40L, 41L, 42L)
                val ksplang = builder.build(program)
                val result = runner.run(ksplang, input)
                result.last() shouldBe input[index]
            }
        }

        context("yoinkInput - static") {
            withData(listOf(0, 1, 2)) { index ->
                val program = buildSingleModuleProgram(module) {
                    body {
                        yoinkInput(index)
                    }
                }
                val input = listOf(40L, 41L, 42L)
                val ksplang = builder.build(program)
                val result = runner.run(ksplang, input)
                result.last() shouldBe input[index]
            }
        }
    }
})

