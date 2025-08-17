package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.runtime.Store
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import java.nio.file.Path

/**
 * WASM module wrapper which can be used to integrate WASM modules in a type-safe way.
 *
 * ## Example
 * ```kotlin
 * class WasmAdd(store: WasmStore) : KsplangWasmModule(Path("wasm/add.wasm", store)) {
 *     fun add(builder: KsplangProgramBuilder): ProgramFunction2To1 {
 *         return with(builder) { with(module) { getExportedFunction("add")!! as ProgramFunction2To1 } }
 *     }
 * }
 * ```
 */
abstract class KsplangWasmModule(path: Path, store: Store) {
    // TODO: Check in the store that module name is unique?
    val moduleName = path.fileName.toString()
    protected val module = KsplangWasmModuleTranslator().translate(moduleName, path, store)

    fun install(builder: KsplangProgramBuilder) {
        with(builder) {
            with(module) { installFunctions() }
        }
    }
}

