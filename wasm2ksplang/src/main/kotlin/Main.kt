package cz.sejsel

import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasm.Parser;
import cz.sejsel.ksplang.wasm.KsplangWasmModule
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To1
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.std.auto.*
import java.io.File
import kotlin.io.path.Path

class WasmAdd(store: WasmStore) : KsplangWasmModule(Path("wasm/add.wasm"), store) {
    fun add(builder: KsplangProgramBuilder): ProgramFunction2To1 {
        return with(builder) { with(module) { getExportedFunction("add")!! as ProgramFunction2To1 } }
    }
}

fun main() {
    //val parsed = Parser.parse(Path("wasm/ksplang_web_bg.wasm"))
    val parsed = Parser.parse(Path("wasm/add.wasm"))
    //println(parsed)

    //val store = Store()
    //val instance = store.instantiate("add", parsed)
    //val add = instance.export("add")
    //add.apply(1, 2).also { println(it[0]) }

    val store = Store()
    val module = Parser.parse(Path("wasm/memory-basics.wasm"))
    val instance = store.instantiate("memory-basics", module)
    val size = instance.export("wasm_size")
    val grow = instance.export("wasm_grow")
    size.apply().also { println("Size: ${it[0]}") }
    grow.apply(1)
    size.apply().also { println("Size after grow: ${it[0]}") }

    val builder = KsplangBuilder()

    /*
     * There are two fundamental approaches to using WASM in ksplang:
     * - "static" runtime with WASM runtime data on a known (at ksplang gen time) static location on the stack.
     *   - easier to implement
     *   - better performance
     *   - harder to compose, does not allow for multiple runtimes, or dynamic module loading
     *   - all programs using WASM will be forced to use the same layouts, which can be less efficient
     * - "dynamic" runtime with runtime data on the top of the stack, with its location being passed to WASM functions.
     *   - requires one more parameter for each function call, so slower
     *       - we could omit the extra parameter when it is known not to be needed, which can be decided statically
     *   - and a bit more annoying to implement
     *   - it would be possible to implement functions in WASM, and then expose them as to
     *     a non-wasm aware ksplang program, which could even be reasonably performant if memory/tables/globals
     *     are not used.
     *
     * We go with the static approach for now, hopefully extending it to dynamic would not be too hard,
     * we could maybe make some wrapper over the static version.
     */

    // WASM has *memories* with page size of 65536 bytes.
    // Our values are 8 bytes, but we won't be packing the values for now, as it would
    // slow down memory access significantly. Memory is comparatively cheap, so we can
    // afford using 8 times more than necessary.


    // Note that we instantiate modules statically, at ksplang generation time.
    // This allows us to statically know what module a function belongs to when generating its code,
    // completely removing the need for a runtime module representation.
    // Additionally, the runtime store can be simplified drastically, the only dynamically accessed
    // part is the function list (through call_indirect), and it does not change during execution.

    val wasmStore = WasmStore()
    val addWasmModule = WasmAdd(wasmStore)

    val ksplang = program {
        addWasmModule.install(this)

        val addFunction = addWasmModule.add(this)

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
