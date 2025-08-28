package cz.sejsel.ksplang.wasm.chicorycomparison

import com.dylibso.chicory.tools.wasm.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule

/*
fun createWasmModule(): WasmModule {
    // Module with one function
    val functionType = FunctionType.of(listOf(ValType.I32, ValType.I32), listOf(ValType.I32))
    val exportedFunctionName = "fun"
    val functionBody = FunctionBody(emptyList(), AnnotatedInstruction.builder().from(Instruction()))

    return WasmModule.builder()
        .setTypeSection(TypeSection.builder().addFunctionType(functionType).build())
        .setExportSection(ExportSection.builder().addExport(Export(exportedFunctionName, 0, ExternalType.FUNCTION)).build())
        .setCodeSection(CodeSection.builder().addFunctionBody())
        .build()
}
 */

fun createWasmModuleFromWat(wat: String): WasmModule {
    val bytes = Wat2Wasm.parse(wat)
    return Parser.parse(bytes)
}
