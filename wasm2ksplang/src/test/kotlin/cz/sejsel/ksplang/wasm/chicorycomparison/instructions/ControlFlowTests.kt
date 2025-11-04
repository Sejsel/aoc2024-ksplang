package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.buildSingleModuleProgram
import getTables
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.bitsToLong
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

    fun List<Long>.hasExpectedPrefixForInput(input: List<Long>, functionCount: Int) {
        this[1] shouldBe input.size
        this.subList(2 + functionCount, 2 + functionCount + input.size) shouldBe input
    }

    fun List<Long>.removePrefix(input: List<Long>, functionCount: Int): List<Long> =
        this.drop(2 + functionCount + input.size)

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
            )""".trimIndent(),

        "if else with result" to $$"""
            (module
                (func $flow (export "flow") (param $input i32) (result i32)
                    local.get $input
                    i32.const 0
                    i32.gt_s
                    if (result i32)
                        local.get $input
                        i32.const 1
                        i32.add
                    else
                        local.get $input
                        i32.const 2
                        i32.add
                    end
                )
            )""".trimIndent(),

        "if else without result" to $$"""
            (module
                (func $flow (export "flow") (param $input i32) (result i32)
                    local.get $input
                    i32.const 0
                    i32.gt_s
                    if
                        local.get $input
                        i32.const 1
                        i32.add
                        local.set $input
                    else
                        local.get $input
                        i32.const 2
                        i32.add
                        local.set $input
                    end
                    local.get $input
                )
            )""".trimIndent(),

        "if else with result - br in if" to $$"""
            (module
                (func $flow (export "flow") (param $input i32) (result i32)
                    local.get $input
                    i32.const 0
                    i32.gt_s
                    if (result i32)
                        local.get $input
                        br 0
                        i32.const 1
                        i32.add
                    else
                        local.get $input
                        i32.const 2
                        i32.add
                    end
                )
            )""".trimIndent(),

        "if else with result - br in else" to $$"""
            (module
                (func $flow (export "flow") (param $input i32) (result i32)
                    local.get $input
                    i32.const 0
                    i32.gt_s
                    if (result i32)
                        local.get $input
                        i32.const 1
                        i32.add
                    else
                        local.get $input
                        br 0
                        i32.const 2
                        i32.add
                    end
                )
            )""".trimIndent(),

        "br_table" to $$"""
            (module
              (func $flow (export "flow") (param $input i32) (result i32)
                (block $outer_block
                  (block $middle_block
                    (block $inner_block
                      local.get $input
                      i32.const 4
                      i32.rem_s
                      ;; we should get -3,-2,-1,0,1,2,3
                      ;; only 0,1,2 map to entries, others out of bounds should map to last label (middle_block)
                      (br_table $inner_block $outer_block $middle_block)
                      unreachable
                    )
                    local.get $input
                    i32.const 1
                    i32.add
                    local.set $input
                  )
                  local.get $input
                  i32.const 2
                  i32.add
                  local.set $input
                )
                local.get $input
              )
            )""".trimMargin(),

        "return drops values from bottom of stack" to $$"""
            (module
              (func $flow (export "flow") (param $input i32) (result i32)
                i32.const 10 ;; should be dropped
                local.get $input
                return
              )
            )
        """.trimMargin(),

        "return in a block" to $$"""
            (module
              (func $flow (export "flow") (param $input i32) (result i32)
                block
                  local.get $input
                  i32.const 5
                  i32.add
                  return
                end
                ;; unreachable
                local.get $input
                i32.const 10
                i32.add
              )
            )
        """.trimMargin(),

        "conditional return in a block" to $$"""
            (module
              (func (export "flow") (param $input i32) (result i32)
                block
                    i32.const 10
                    i32.const 90
                    local.get $input
                    br_if 0
                    return
                end
                i32.const 42
              )
            )
        """.trimMargin(),

        "simple call" to $$"""
            (module
              (func $helper (param $x i32) (result i32)
                local.get $x
                i32.const 100
                i32.add
              )
              (func $flow (export "flow") (param $input i32) (result i32)
                local.get $input
                call $helper
              )
            )
        """.trimMargin(),

        "indirect call with two functions" to $$"""
            (module
              (type $func_type (func (param i32) (result i32)))
              (func $helper1 (type $func_type) (param $x i32) (result i32)
                local.get $x
                i32.const 200
                i32.add
              )
              (func $helper2 (type $func_type) (param $x i32) (result i32)
                local.get $x
                i32.const 300
                i32.add
              )
              (table 2 funcref)
              (elem (i32.const 0) $helper1 $helper2)
              (func $flow (export "flow") (param $input i32) (result i32)
                local.get $input
                local.get $input
                i32.const 2
                i32.rem_u ;; 0 or 1
                call_indirect (type $func_type)
              )
            )
        """.trimMargin(),
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
                val input = listOf(i.bitsToLong())

                val referenceStore = Store()
                val instance = referenceStore.instantiate("mod", module.module.chicoryModule)
                val func = instance.export("flow")!!
                val expected = func.apply(*input.toLongArray()).single()
                val functionsInTable = instance.getTables().singleOrNull { it.elementType() == ValType.FuncRef }?.size() ?: 0

                val result = runner.run(ksplang, input)
                result.hasExpectedPrefixForInput(input, functionsInTable)
                result.removePrefix(input, functionsInTable).map { it.toInt() } shouldBe listOf(expected).map { it.toInt() }
            }
        }
    }
})