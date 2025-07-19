package cz.sejsel

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule
import com.dylibso.chicory.wasm.types.Export
import com.dylibso.chicory.wasm.types.ExternalType
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To1
import cz.sejsel.ksplang.dsl.core.ProgramFunctionBase
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.yoink
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import java.io.File
import kotlin.io.path.Path


class WasmAdd {
    private val module = KsplangWasmModuleTranslator(Path("wasm/add.wasm")).translate()

    fun install(builder: KsplangProgramBuilder) {
        with(builder) {
            with(module) { installFunctions() }
        }
    }

    fun add(builder: KsplangProgramBuilder): ProgramFunction2To1 {
        return with(builder) { with(module) { getExportedFunction("add")!! as ProgramFunction2To1 } }
    }
}

private fun WasmModule.findFunctionExport(name: String): Export? {
    val exportSection = exportSection()
    val export = (0..<exportSection.exportCount()).map { exportSection.getExport(it) }.find {
        it.exportType() == ExternalType.FUNCTION && it.name() == name
    }
    return export
}

fun main() {
    val parsed = Parser.parse(Path("wasm/ksplang_web_bg.wasm"))
    //val parsed = Parser.parse(Path("wasm/add.wasm"))
    println(parsed)

    val builder = KsplangBuilder()

    // WASM has *memories* with page size of 65536 bytes.
    // Our values are 8 bytes, but we won't be packing the values for now, as it would
    // slow down memory access significantly. Memory is comparatively cheap, so we can
    // afford using 8 times more than necessary.

    val PAGE_SIZE = 65536

    // Note that we instantiate modules statically, at ksplang generation time.
    // This allows us to statically know what module a function belongs to when generating its code,
    // completely removing the need for a runtime module representation.
    // Additionally, the runtime store can be simplified drastically, the only dynamically accessed
    // part is the function list (through call_indirect), and it does not change during execution.

    val module = WasmAdd()

    val ksplang = program {
        module.install(this)

        val addFunction = module.add(this)

        body {
            auto {
                val stacklen = stacklen()
                val input = Slice(0.const, copy(stacklen))

                val allocator = Allocator(copy(stacklen))
                //val arena = alloc(allocator, ARENA_ELEMENTS.const)

                val a = yoink(input.from)
                val b = yoink(add(input.from, 1))
                val result = call(addFunction, a, b)
                keepOnly(result)
            }
        }
    }

    val program = builder.build(ksplang)
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("wasmtest.ksplang").writeText(program)
    println("Generated program with $instructionCount instructions")
}

//fun FunctionBody.toKsplang(): ComplexFunction {
//
//}
