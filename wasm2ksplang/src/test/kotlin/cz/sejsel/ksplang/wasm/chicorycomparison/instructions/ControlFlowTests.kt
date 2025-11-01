package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromWat
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlin.collections.toLongArray

class ControlFlowTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    // Note: we do not do multi value WASM extension yet, so blocks cannot have params
    // (but they can have one or zero results)

    // Unfortunately, we need to also validate the runtime data to make sure we don't get extra values,
    // malformed runtime data, etc.

    // For this reason, we use the same general approach in all tests: define one func (flow),
    // which takes one number as input, returns one number as output. The block/loop somehow affects the returned value,
    // which is based on the input (to ensure everything works when put together)

    fun List<Long>.hasExpectedPrefixForInput(input: List<Long>) {
        this[1] shouldBe input.size
        this.subList(2, 2 + input.size) shouldBe input
    }

    fun List<Long>.removePrefix(input: List<Long>): List<Long> =
        this.drop(2 + input.size)

    val testCases = listOf(
        "simple block with result with no br" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block (result i32)
                        local.get $input
                        i32.const 42
                        i32.add
                    end
                )
            )""".trimIndent(),

        "simple block with result with unconditional br" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block (result i32)
                        local.get $input
                        br 0
                        i32.const 42
                        i32.add
                    end
                )
            )""".trimIndent(),

        "simple block without result with unconditional br" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block
                        br 0
                        local.get $input
                        i32.const 42
                        i32.add
                        local.set $input
                    end
                    local.get $input
                )
            )""".trimIndent(),

        "simple block with result with br_if" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block (result i32)
                        local.get $input
                        local.get $input
                        br_if 0
                        i32.const 42
                        i32.add
                    end
                )
            )""".trimIndent(),

        "nested blocks with result - break inner" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block
                        block
                            br 0
                            local.get $input
                            i32.const 1
                            i32.add
                            local.set $input
                        end
                        local.get $input
                        i32.const 2
                        i32.add
                        local.set $input
                    end
                    local.get $input
                )
            )""".trimIndent(),

        "nested blocks with result - break outer" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block
                        block
                            br 1
                            local.get $input
                            i32.const 1
                            i32.add
                            local.set $input
                        end
                        local.get $input
                        i32.const 2
                        i32.add
                        local.set $input
                    end
                    local.get $input
                )
            )""".trimIndent(),

        "nested blocks with result - br removes intermediate values" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    block
                        local.get $input ;; this is the intermediate
                        block
                            br 1
                            local.get $input
                            i32.const 1
                            i32.add
                            local.set $input
                        end
                        i32.const 2
                        i32.add
                        local.set $input
                    end
                    local.get $input
                )
            )""".trimIndent(),

        "simple loop with result with no br" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    loop (result i32)
                        local.get $input
                        i32.const 42
                        i32.add
                    end
                )
            )""".trimIndent(),

        "simple loop with 3 iterations" to $$"""
            (module 
                (func $flow (export "flow") (param $input i32) (result i32)
                    (local $index i32)  
                    (local.set $index (i32.const 3))
                    loop
                        local.get $input
                        i32.const 1
                        i32.add
                        local.set $input
                        local.get $index
                        i32.const 1
                        i32.sub
                        local.tee $index
                        br_if 0
                    end
                  local.get $input
                )
            )""".trimIndent()
    )

    withData(nameFn = { (name, _) -> name }, testCases) { (_, wat) ->
        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val flow = getExportedFunction("flow") as ProgramFunction1To1

            body {
                dup() // dup last input number
                call(flow)
            }
        }
        val ksplang = builder.buildAnnotated(program).toRunnableProgram()

        test("chicory gets the same result") {
            checkAll<Int> { i ->
                val input = listOf(i.toLong())

                val referenceStore = Store()
                val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("flow")!!
                val expected = func.apply(*input.toLongArray()).single()

                val result = runner.run(ksplang, input)
                result.hasExpectedPrefixForInput(input)
                result.removePrefix(input).map { it.toInt() } shouldBe listOf(expected).map { it.toInt() }
            }
        }
    }
})