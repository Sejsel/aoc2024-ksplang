package cz.sejsel.cz.sejsel.ksplang.wasm

import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import java.nio.file.Path

abstract class KsplangWasmModule(path: Path) {
    protected val module = KsplangWasmModuleTranslator(path).translate()

    fun install(builder: KsplangProgramBuilder) {
        with(builder) {
            with(module) { installFunctions() }
        }
    }
}

