package cz.sejsel.ksplang.wasm.runtime.singlemodule

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

class GlobalTests : FunSpec({
    val runner = DefaultKsplangRunner(defaultOpLimit = 100_000_000)
    val builder = KsplangBuilder()
    val translator = KsplangWasmModuleTranslator()

    val wat = $$"""
        (module 
            ;; globals
            (global $g_mut (mut i32) i32.const 7)
            (global $g_imm i32 i32.const 42)
            (func $add (export "add") (param $a i32) (result i32)
                global.get $g_mut
                local.get $a
                i32.add
            )
        )""".trimIndent()

    val store = Store()
    val module = instantiateModuleFromWat(translator, wat, "test", store)

    val emptyProgram = buildSingleModuleProgram(module) {
        val add = getExportedFunction("add") as ProgramFunction1To1

        body {
            call(add)
        }
    }
    val ksplang = builder.buildAnnotated(emptyProgram).toRunnableProgram()

    test("global.get") {
        val result = runner.run(ksplang, listOf(10L))
        result.last() shouldBe 17L
    }
})

