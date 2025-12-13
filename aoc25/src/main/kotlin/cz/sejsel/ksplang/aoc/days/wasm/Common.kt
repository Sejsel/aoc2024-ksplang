package cz.sejsel.ksplang.aoc.days.wasm

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.std.leaveTop
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromPath
import java.io.File
import java.nio.file.Path

/**
 * A ksplang program which calls the main function - it should have no params and it should return one i32/i64 value.
 * It should interact with the input through imported functions: `env.read_input(index)` and `env.input_size`.
 */
fun buildWasmSingleValueProgram(wasmPath: Path, functionName: String): KsplangProgram {
    val translator = KsplangWasmModuleTranslator()

    val store = Store()
    val module = instantiateModuleFromPath(translator, wasmPath, "module", store)
    return buildSingleModuleProgram(module) {
        val mainFunction = getExportedFunction(functionName) as ProgramFunction0To1

        body {
            call(mainFunction)
            // i32/i64
            leaveTop() // destroys runtime layout
        }
    }
}

fun rebuildAllWasm() {
    val process = ProcessBuilder("./build-all.sh")
        .directory(File("aoc25/rust"))
        .inheritIO()
        .start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        error("build-all.sh failed with exit code $exitCode")
    }
}
