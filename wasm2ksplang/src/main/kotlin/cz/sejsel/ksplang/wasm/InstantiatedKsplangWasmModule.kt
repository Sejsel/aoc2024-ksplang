package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.tools.wasm.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To0
import cz.sejsel.ksplang.dsl.core.ProgramFunctionBase
import java.nio.file.Path

class InstantiatedKsplangWasmModule(val moduleName: String, val module: TranslatedWasmModule, store: Store) {
    // TODO: Check in the store that module name is unique?
    val instance: Instance = store.instantiate(moduleName, module.chicoryModule)

    fun getFunction(index: Int): ProgramFunctionBase? = module.getFunction(index)

    fun getExportedFunction(builder: KsplangProgramBuilder, name: String): ProgramFunctionBase? {
        return with(builder) { with(module) { getExportedFunction(name) } }
    }

    /* Functions for getGlobal, must have body set by the embedder. */
    fun getGetGlobalFunctions(): Map<Int, ProgramFunction0To1> = module.getGlobalFunctions

    /* Functions for setGlobal, must have body set by the embedder. */
    fun getSetGlobalFunctions(): Map<Int, ProgramFunction1To0> = module.setGlobalFunctions

    /** Function for getMemory, must have body set by the embedder. */
    fun getGetMemoryFunction(): ProgramFunction1To1? = module.getMemoryFunction

    /** Function for setMemory, must have body set by the embedder. */
    fun getSetMemoryFunction(): ProgramFunction2To0? = module.setMemoryFunction

    /** Function for getMemorySize, must have body set by the embedder. */
    fun getGetMemorySizeFunction(): ProgramFunction0To1? = module.getMemorySizeFunction

    /** Function for growMemory, must have body set by the embedder. */
    fun getGrowMemoryFunction(): ProgramFunction1To1? = module.growMemoryFunction

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
