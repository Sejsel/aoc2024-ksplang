package cz.sejsel.ksplang.wasm.chicorycomparison

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.tools.wasm.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType

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

object ChicoryHostFunctions {
    fun readInput(input: List<Long>): HostFunction {
        return HostFunction(
            "env",
            "read_input",
            FunctionType.of(listOf(ValType.I32), listOf(ValType.I64)),
        ) { _, args ->
            val index = args[0].toInt()
            val value = input[index]
            longArrayOf(value)
        }
    }

    fun inputSize(input: List<Long>): HostFunction {
        return HostFunction(
            "env",
            "input_size",
            FunctionType.of(listOf(), listOf(ValType.I32)),
        ) { _, args ->
            longArrayOf(input.size.toLong())
        }
    }
}
