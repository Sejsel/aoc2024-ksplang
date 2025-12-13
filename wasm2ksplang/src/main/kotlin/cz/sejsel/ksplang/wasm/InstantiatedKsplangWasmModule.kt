package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.tools.wasm.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To0
import cz.sejsel.ksplang.dsl.core.ProgramFunctionBase
import java.nio.file.Path

class InstantiatedKsplangWasmModule(val moduleName: String, val module: TranslatedWasmModule, store: Store) {
    // TODO: Check in the store that module name is unique?
    init {
        // This is a bit of an ugly hack, but it's the only way we can reuse chicory's instantiate functionality.
        // TODO: Do this for all imports, we have module here, so we can do it automatically
        store.addFunction(HostFunction("env", "input_size", FunctionType.of(listOf(), listOf(ValType.I32))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("env", "read_input", FunctionType.of(listOf(ValType.I32), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("env", "save_raw_i64", FunctionType.of(listOf(ValType.I64, ValType.I32), listOf())) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("env", "set_input", FunctionType.of(listOf(ValType.I64, ValType.I32), listOf())) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "max", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "u_add", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "u_subabs", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "u_mul", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "u_curseddiv", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "u_factorial", FunctionType.of(listOf(ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "u_sgn", FunctionType.of(listOf(ValType.I64), listOf(ValType.I32))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "rem", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "mod", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "tetr", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "cs", FunctionType.of(listOf(ValType.I64), listOf(ValType.I32))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "lensum", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I32))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "bitshift", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "and", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "gcd", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "funkcia", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I32))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "spanek", FunctionType.of(listOf(), listOf())) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "negate", FunctionType.of(listOf(ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
        store.addFunction(HostFunction("ksplang", "div", FunctionType.of(listOf(ValType.I64, ValType.I64), listOf(ValType.I64))) { _, _ -> error("Dummy function") })
    }
    val instance: Instance = store.instantiate(moduleName, module.chicoryModule)

    val isMemoryUsed: Boolean = module.isMemoryUsed

    fun getFunction(index: Int): ProgramFunctionBase? = module.getFunction(index)

    fun getExportedFunction(builder: KsplangProgramBuilder, name: String): ProgramFunctionBase? {
        return with(builder) { with(module) { getExportedFunction(name) } }
    }

    /** Functions for getGlobal, must have body set by the embedder. */
    fun getGetGlobalFunctions(): Map<Int, ProgramFunction0To1> = module.getGlobalFunctions

    /** Functions for setGlobal, must have body set by the embedder. */
    fun getSetGlobalFunctions(): Map<Int, ProgramFunction1To0> = module.setGlobalFunctions

    /** Function for getMemory, must have body set by the embedder. */
    fun getGetMemoryFunction(): ProgramFunction1To1? = module.getMemoryFunction

    /** Function for setMemory, must have body set by the embedder. */
    fun getSetMemoryFunction(): ProgramFunction2To0? = module.setMemoryFunction

    /** Function for getMemorySize, must have body set by the embedder. */
    fun getGetMemorySizeFunction(): ProgramFunction0To1? = module.getMemorySizeFunction

    /** Function for growMemory, must have body set by the embedder. */
    fun getGrowMemoryFunction(): ProgramFunction1To1? = module.growMemoryFunction

    /** Function for getInputSize, must have body set by the embedder. */
    fun getGetInputSizeFunction(): ProgramFunction0To1? = module.getInputSizeFunction

    /** Function for readInput, must have body set by the embedder. */
    fun getReadInputFunction(): ProgramFunction1To1? = module.readInputFunction

    /** Function for getFunctionAddress, must have body set by the embedder. */
    fun getGetFunctionAddressFunction(): ProgramFunction1To1? = module.getFunctionAddressFunction

    fun getSaveRawFunction(): ProgramFunction2To0? = module.saveRawFunction

    fun getSetInputFunction(): ProgramFunction2To0? = module.setInputFunction

    fun install(builder: KsplangProgramBuilder) {
        with(builder) {
            with(module) { installFunctions() }
        }
    }
}

fun instantiateModuleFromPath(translator: KsplangWasmModuleTranslator, path: Path, name: String, store: Store): InstantiatedKsplangWasmModule {
    val translated = translator.translate(name, path)
    return InstantiatedKsplangWasmModule(name, translated, store)
}


fun instantiateModuleFromWat(translator: KsplangWasmModuleTranslator, wat: String, name: String, store: Store): InstantiatedKsplangWasmModule {
    val chicoryModule = Parser.parse(Wat2Wasm.parse(wat))
    val translated = translator.translate(name, chicoryModule)
    return InstantiatedKsplangWasmModule(name, translated, store)
}
