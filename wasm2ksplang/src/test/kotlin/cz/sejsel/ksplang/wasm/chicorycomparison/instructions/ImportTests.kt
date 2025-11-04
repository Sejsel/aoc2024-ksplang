package cz.sejsel.ksplang.wasm.chicorycomparison.instructions

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.chicorycomparison.ChicoryHostFunctions
import cz.sejsel.ksplang.wasm.instantiateModuleFromWat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class ImportTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    // This is a module generated from Rust code which just does a for loop over the input and sums the values (as i64).
    context("sum input") {
        val wat = $$"""
            (module $ksplang_wasm.wasm
              (type (;0;) (func (result i32)))
              (type (;1;) (func (param i32) (result i64)))
              (type (;2;) (func (result i64)))
              (import "env" "input_size" (func $_ZN12ksplang_wasm10input_size17h8069a76e395a9d03E (type 0)))
              (import "env" "read_input" (func $_ZN12ksplang_wasm10read_input17h54ec5c3f4bb1e284E (type 1)))
              (func $sum_input (type 2) (result i64)
                (local i32 i64 i32)
                block  ;; label = @1
                  block  ;; label = @2
                    call $_ZN12ksplang_wasm10input_size17h8069a76e395a9d03E
                    local.tee 0
                    i32.const 1
                    i32.ge_s
                    br_if 0 (;@2;)
                    i64.const 0
                    local.set 1
                    br 1 (;@1;)
                  end
                  i32.const 0
                  local.set 2
                  i64.const 0
                  local.set 1
                  loop  ;; label = @2
                    local.get 2
                    call $_ZN12ksplang_wasm10read_input17h54ec5c3f4bb1e284E
                    local.get 1
                    i64.add
                    local.set 1
                    local.get 0
                    local.get 2
                    i32.const 1
                    i32.add
                    local.tee 2
                    i32.ne
                    br_if 0 (;@2;)
                  end
                end
                local.get 1)
              (table (;0;) 1 1 funcref)
              (memory (;0;) 16)
              (global $__stack_pointer (mut i32) (i32.const 1048576))
              (global (;1;) i32 (i32.const 1048576))
              (global (;2;) i32 (i32.const 1048576))
              (export "memory" (memory 0))
              (export "sum_input" (func $sum_input))
              (export "__data_end" (global 1))
              (export "__heap_base" (global 2)))
        """.trimIndent()

        val store = Store()
        val module = instantiateModuleFromWat(translator, wat, "test", store)
        val program = buildSingleModuleProgram(module) {
            val sum = getExportedFunction("sum_input") as ProgramFunction0To1

            body {
                call(sum)
            }
        }
        val annotated = builder.buildAnnotated(program)
        val ksplang = annotated.toRunnableProgram()

        test("chicory gets the same result") {
            checkAll(30, Arb.list(Arb.long(), 1..100)) { input ->
                val referenceStore = Store()
                referenceStore.addFunction(ChicoryHostFunctions.readInput(input), ChicoryHostFunctions.inputSize(input))
                val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("sum_input")!!
                val expectedResult = func.apply().single()

                val result = runner.run(ksplang, input)

                result.last() shouldBe expectedResult
            }
        }
    }
})