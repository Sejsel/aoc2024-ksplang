package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import com.dylibso.chicory.wasm.types.ExternalType
import com.dylibso.chicory.wasm.types.OpCode
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction3To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction4To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction5To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction6To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction7To8
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To3
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To4
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To5
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To6
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To7
import cz.sejsel.ksplang.dsl.core.ProgramFunction8To8
import cz.sejsel.ksplang.dsl.core.ProgramFunctionBase
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import java.nio.file.Path
import cz.sejsel.ksplang.wasm.WasmFunctionScope.Companion.initialize as initializeScope

class TranslatedWasmModule(
    val programFunctions: List<ProgramFunctionBase>,
    val chicoryModule: WasmModule,
    private val exportedFunctions: Map<String, ProgramFunctionBase>,
    /** Forward declaration, needs to be implemented by embedder */
    val getMemoryFunction: ProgramFunction1To1?,
    /** Forward declaration, needs to be implemented by embedder */
    val getGlobalFunctions: Map<Int, ProgramFunction0To1>,
    /** Forward declaration, needs to be implemented by embedder */
    val setGlobalFunctions: Map<Int, ProgramFunction1To0>,
) {
    fun KsplangProgramBuilder.installFunctions() {
        programFunctions.forEach { installFunction(it) }
        getGlobalFunctions.values.forEach { installFunction(it) }
        setGlobalFunctions.values.forEach { installFunction(it) }
        getMemoryFunction?.let { installFunction(it) }
    }

    fun getFunction(index: Int): ProgramFunctionBase? {
        return programFunctions.getOrNull(index)
    }

    fun KsplangProgramBuilder.getExportedFunction(name: String): ProgramFunctionBase? {
        return exportedFunctions[name]?.also {
            check(hasFunction(it.name)) {
                "Function '${it.name}' is not installed in the program builder. Did you forget to call installFunctions()?"
            }
        }
    }
}

class ModuleTranslatorState {
    // All of these are forward declarations:
    val getGlobalFunctions = mutableMapOf<Int, ProgramFunction0To1>()
    val setGlobalFunctions = mutableMapOf<Int, ProgramFunction1To0>()
    var getMemoryFunction: ProgramFunction1To1? = null

    fun getMemoryFunction(): ProgramFunction1To1 {
        // Forward declaration.
        return getMemoryFunction ?: ProgramFunction1To1(
            name = "wasm_getMemory",
            body = null,
        ).also { getMemoryFunction = it }
    }

    fun getGlobalFunction(globalIndex: Int): ProgramFunction0To1 {
        // Forward declaration.
        return getGlobalFunctions.getOrPut(globalIndex) {
            ProgramFunction0To1(
                name = "wasm_getGlobal($globalIndex)",
                body = null,
            )
        }
    }

    fun setGlobalFunction(globalIndex: Int): ProgramFunction1To0 {
        // Forward declaration.
        return setGlobalFunctions.getOrPut(globalIndex) {
            ProgramFunction1To0(
                name = "wasm_setGlobal($globalIndex)",
                body = null,
            )
        }
    }
}

class KsplangWasmModuleTranslator() {
    // TODO: Forward function declaration
    // TODO: Memory
    // TODO: Start function
    fun translate(moduleName: String, module: WasmModule): TranslatedWasmModule {
        val functions = mutableListOf<ProgramFunctionBase>()
        val state = ModuleTranslatorState()
        for (functionIndex in 0..<module.functionSection().functionCount()) {
            val function = functionToKsplang(module, functionIndex, moduleName, state)
            functions.add(function)
        }

        return TranslatedWasmModule(
            programFunctions = functions,
            chicoryModule = module,
            exportedFunctions = associateExportedFunctions(module, functions),
            getGlobalFunctions = state.getGlobalFunctions,
            setGlobalFunctions = state.setGlobalFunctions,
            getMemoryFunction = state.getMemoryFunction,
        )
    }

    fun translate(moduleName: String, path: Path): TranslatedWasmModule {
        val module = Parser.parse(path)
        return translate(moduleName, module)
    }

    private fun associateExportedFunctions(
        module: WasmModule,
        functions: List<ProgramFunctionBase>
    ): Map<String, ProgramFunctionBase> {
        val exportSection = module.exportSection()
        return (0..<exportSection.exportCount()).map { exportSection.getExport(it) }
            .filter { it.exportType() == ExternalType.FUNCTION }
            .associate { it.name() to functions[it.index()] }
    }

    private fun functionToKsplang(module: WasmModule, index: Int, moduleName: String, state: ModuleTranslatorState): ProgramFunctionBase {
        val functionType = module.typeSection().getType(module.functionSection().getFunctionType(index))
        val code = module.codeSection().functionBodies()[index]
        val localTypes = code.localTypes()
        val paramCount = functionType.params().size
        val returnCount = functionType.returns().size

        // There may be a name in the name custom section
        val declaredName = module.nameSection()?.nameOfFunction(index) ?: "anonymous"

        val name = "wasm_${moduleName}_${index}_$declaredName"

        val body = buildComplexFunction {
            val scope = initializeScope(
                params = functionType.params(),
                localTypes = localTypes,
                returns = functionType.returns(),
                state = state
            )

            with(scope) {
                code.instructions().forEach { instruction ->
                    when (instruction.opcode()) {
                        OpCode.UNREACHABLE -> TODO()
                        OpCode.NOP -> TODO()
                        OpCode.BLOCK -> TODO()
                        OpCode.LOOP -> TODO()
                        OpCode.IF -> TODO()
                        OpCode.ELSE -> TODO()
                        OpCode.THROW -> unsupportedExceptionHandling()
                        OpCode.THROW_REF -> unsupportedExceptionHandling()
                        OpCode.END -> {
                            if (instruction.depth() == 0) {
                                // The scope definition from Chicory is a bit weird here, let's double-check
                                // it's always the same just to make sure we are not missing something
                                check(instruction.scope() == instruction)
                                // End of the function
                                popLocals()
                            } else {
                                TODO()
                            }
                        }

                        OpCode.BR -> TODO()
                        OpCode.BR_IF -> TODO()
                        OpCode.BR_TABLE -> TODO()
                        OpCode.RETURN -> TODO()
                        OpCode.CALL -> TODO()
                        OpCode.CALL_INDIRECT -> TODO()
                        OpCode.RETURN_CALL -> unsupportedTailCall()
                        OpCode.RETURN_CALL_INDIRECT -> unsupportedTailCall()
                        OpCode.CALL_REF -> unsupportedTypedFunctionReferenceTypes()
                        OpCode.RETURN_CALL_REF -> unsupportedTypedFunctionReferenceTypes()
                        OpCode.DROP -> drop()
                        OpCode.SELECT -> select()
                        OpCode.SELECT_T -> unsupportedReferenceTypes()
                        OpCode.TRY_TABLE -> unsupportedExceptionHandling()
                        OpCode.LOCAL_GET -> getLocal(instruction.operands()[0].toInt())
                        OpCode.LOCAL_SET -> setLocal(instruction.operands()[0].toInt())
                        OpCode.LOCAL_TEE -> teeLocal(instruction.operands()[0].toInt())
                        OpCode.GLOBAL_GET -> getGlobal(instruction.operands()[0].toInt())
                        OpCode.GLOBAL_SET -> setGlobal(instruction.operands()[0].toInt())
                        OpCode.TABLE_GET -> unsupportedReferenceTypes()
                        OpCode.TABLE_SET -> unsupportedReferenceTypes()
                        OpCode.I32_LOAD -> i32Load()
                        OpCode.I64_LOAD -> i64Load()
                        OpCode.F32_LOAD -> i32Load()
                        OpCode.F64_LOAD -> i64Load()
                        OpCode.I32_LOAD8_S -> i32Load8Signed()
                        OpCode.I32_LOAD8_U -> i8Load()
                        OpCode.I32_LOAD16_S -> i32Load16Signed()
                        OpCode.I32_LOAD16_U -> i16Load()
                        OpCode.I64_LOAD8_S -> i64Load8Signed()
                        OpCode.I64_LOAD8_U -> i8Load()
                        OpCode.I64_LOAD16_S -> i64Load16Signed()
                        OpCode.I64_LOAD16_U -> i16Load()
                        OpCode.I64_LOAD32_S -> i64Load32Signed()
                        OpCode.I64_LOAD32_U -> i32Load()
                        OpCode.I32_STORE -> TODO()
                        OpCode.I64_STORE -> TODO()
                        OpCode.F32_STORE -> TODO()
                        OpCode.F64_STORE -> TODO()
                        OpCode.I32_STORE8 -> TODO()
                        OpCode.I32_STORE16 -> TODO()
                        OpCode.I64_STORE8 -> TODO()
                        OpCode.I64_STORE16 -> TODO()
                        OpCode.I64_STORE32 -> TODO()
                        OpCode.MEMORY_SIZE -> TODO()
                        OpCode.MEMORY_GROW -> TODO()
                        OpCode.I32_CONST -> i32Const(instruction.operands()[0])
                        OpCode.I64_CONST -> i64Const(instruction.operands()[0])
                        OpCode.F32_CONST -> TODO()
                        OpCode.F64_CONST -> TODO()
                        OpCode.I32_EQZ -> i32Eqz()
                        OpCode.I32_EQ -> i32Eq()
                        OpCode.I32_NE -> i32Ne()
                        OpCode.I32_LT_S -> i32LtSigned()
                        OpCode.I32_LT_U -> i32LtUnsigned()
                        OpCode.I32_GT_S -> i32GtSigned()
                        OpCode.I32_GT_U -> i32GtUnsigned()
                        OpCode.I32_LE_S -> i32LeSigned()
                        OpCode.I32_LE_U -> i32LeUnsigned()
                        OpCode.I32_GE_S -> i32GeSigned()
                        OpCode.I32_GE_U -> i32GeUnsigned()
                        OpCode.I64_EQZ -> i64Eqz()
                        OpCode.I64_EQ -> i64Eq()
                        OpCode.I64_NE -> i64Ne()
                        OpCode.I64_LT_S -> i64LtSigned()
                        OpCode.I64_LT_U -> i64LtUnsigned()
                        OpCode.I64_GT_S -> i64GtSigned()
                        OpCode.I64_GT_U -> i64GtUnsigned()
                        OpCode.I64_LE_S -> i64LeSigned()
                        OpCode.I64_LE_U -> i64LeUnsigned()
                        OpCode.I64_GE_S -> i64GeSigned()
                        OpCode.I64_GE_U -> i64GeUnsigned()
                        OpCode.F32_EQ -> TODO()
                        OpCode.F32_NE -> TODO()
                        OpCode.F32_LT -> TODO()
                        OpCode.F32_GT -> TODO()
                        OpCode.F32_LE -> TODO()
                        OpCode.F32_GE -> TODO()
                        OpCode.F64_EQ -> TODO()
                        OpCode.F64_NE -> TODO()
                        OpCode.F64_LT -> TODO()
                        OpCode.F64_GT -> TODO()
                        OpCode.F64_LE -> TODO()
                        OpCode.F64_GE -> TODO()
                        OpCode.I32_CLZ -> i32Clz()
                        OpCode.I32_CTZ -> i32Ctz()
                        OpCode.I32_POPCNT -> i32PopCnt()
                        OpCode.I32_ADD -> i32Add()
                        OpCode.I32_SUB -> i32Sub()
                        OpCode.I32_MUL -> i32Mul()
                        OpCode.I32_DIV_S -> i32DivSigned()
                        OpCode.I32_DIV_U -> i32DivUnsigned()
                        OpCode.I32_REM_S -> i32RemSigned()
                        OpCode.I32_REM_U -> i32RemUnsigned()
                        OpCode.I32_AND -> bitAnd()
                        OpCode.I32_OR -> bitOr()
                        OpCode.I32_XOR -> bitXor()
                        OpCode.I32_SHL -> i32Shl()
                        OpCode.I32_SHR_S -> i32ShrSigned()
                        OpCode.I32_SHR_U -> i32ShrUnsigned()
                        OpCode.I32_ROTL -> i32Rotl()
                        OpCode.I32_ROTR -> i32Rotr()
                        OpCode.I64_CLZ -> i64Clz()
                        OpCode.I64_CTZ -> i64Ctz()
                        OpCode.I64_POPCNT -> i64PopCnt()
                        OpCode.I64_ADD -> i64Add()
                        OpCode.I64_SUB -> i64Sub()
                        OpCode.I64_MUL -> i64Mul()
                        OpCode.I64_DIV_S -> i64DivSigned()
                        OpCode.I64_DIV_U -> i64DivUnsigned()
                        OpCode.I64_REM_S -> i64RemSigned()
                        OpCode.I64_REM_U -> i64RemUnsigned()
                        OpCode.I64_AND -> bitAnd()
                        OpCode.I64_OR -> bitOr()
                        OpCode.I64_XOR -> bitXor()
                        OpCode.I64_SHL -> i64Shl()
                        OpCode.I64_SHR_S -> i64ShrSigned()
                        OpCode.I64_SHR_U -> i64ShrUnsigned()
                        OpCode.I64_ROTL -> i64Rotl()
                        OpCode.I64_ROTR -> i64Rotr()
                        OpCode.F32_ABS -> TODO()
                        OpCode.F32_NEG -> TODO()
                        OpCode.F32_CEIL -> TODO()
                        OpCode.F32_FLOOR -> TODO()
                        OpCode.F32_TRUNC -> TODO()
                        OpCode.F32_NEAREST -> TODO()
                        OpCode.F32_SQRT -> TODO()
                        OpCode.F32_ADD -> TODO()
                        OpCode.F32_SUB -> TODO()
                        OpCode.F32_MUL -> TODO()
                        OpCode.F32_DIV -> TODO()
                        OpCode.F32_MIN -> TODO()
                        OpCode.F32_MAX -> TODO()
                        OpCode.F32_COPYSIGN -> TODO()
                        OpCode.F64_ABS -> TODO()
                        OpCode.F64_NEG -> TODO()
                        OpCode.F64_CEIL -> TODO()
                        OpCode.F64_FLOOR -> TODO()
                        OpCode.F64_TRUNC -> TODO()
                        OpCode.F64_NEAREST -> TODO()
                        OpCode.F64_SQRT -> TODO()
                        OpCode.F64_ADD -> TODO()
                        OpCode.F64_SUB -> TODO()
                        OpCode.F64_MUL -> TODO()
                        OpCode.F64_DIV -> TODO()
                        OpCode.F64_MIN -> TODO()
                        OpCode.F64_MAX -> TODO()
                        OpCode.F64_COPYSIGN -> TODO()
                        OpCode.I32_WRAP_I64 -> TODO()
                        OpCode.I32_TRUNC_F32_S -> TODO()
                        OpCode.I32_TRUNC_F32_U -> TODO()
                        OpCode.I32_TRUNC_F64_S -> TODO()
                        OpCode.I32_TRUNC_F64_U -> TODO()
                        OpCode.I64_EXTEND_I32_S -> TODO()
                        OpCode.I64_EXTEND_I32_U -> TODO()
                        OpCode.I64_TRUNC_F32_S -> TODO()
                        OpCode.I64_TRUNC_F32_U -> TODO()
                        OpCode.I64_TRUNC_F64_S -> TODO()
                        OpCode.I64_TRUNC_F64_U -> TODO()
                        OpCode.F32_CONVERT_I32_S -> TODO()
                        OpCode.F32_CONVERT_I32_U -> TODO()
                        OpCode.F32_CONVERT_I64_S -> TODO()
                        OpCode.F32_CONVERT_I64_U -> TODO()
                        OpCode.F32_DEMOTE_F64 -> TODO()
                        OpCode.F64_CONVERT_I32_S -> TODO()
                        OpCode.F64_CONVERT_I32_U -> TODO()
                        OpCode.F64_CONVERT_I64_S -> TODO()
                        OpCode.F64_CONVERT_I64_U -> TODO()
                        OpCode.F64_PROMOTE_F32 -> TODO()
                        OpCode.I32_REINTERPRET_F32 -> TODO()
                        OpCode.I64_REINTERPRET_F64 -> TODO()
                        OpCode.F32_REINTERPRET_I32 -> TODO()
                        OpCode.F64_REINTERPRET_I64 -> TODO()
                        OpCode.I32_EXTEND_8_S -> TODO()
                        OpCode.I32_EXTEND_16_S -> TODO()
                        OpCode.I64_EXTEND_8_S -> TODO()
                        OpCode.I64_EXTEND_16_S -> TODO()
                        OpCode.I64_EXTEND_32_S -> TODO()
                        OpCode.REF_NULL -> unsupportedReferenceTypes()
                        OpCode.REF_IS_NULL -> unsupportedReferenceTypes()
                        OpCode.REF_FUNC -> unsupportedReferenceTypes()
                        OpCode.REF_AS_NON_NULL -> unsupportedTypedFunctionReferenceTypes()
                        OpCode.BR_ON_NULL -> unsupportedTypedFunctionReferenceTypes()
                        OpCode.BR_ON_NON_NULL -> unsupportedTypedFunctionReferenceTypes()
                        OpCode.I32_TRUNC_SAT_F32_S -> TODO()
                        OpCode.I32_TRUNC_SAT_F32_U -> TODO()
                        OpCode.I32_TRUNC_SAT_F64_S -> TODO()
                        OpCode.I32_TRUNC_SAT_F64_U -> TODO()
                        OpCode.I64_TRUNC_SAT_F32_S -> TODO()
                        OpCode.I64_TRUNC_SAT_F32_U -> TODO()
                        OpCode.I64_TRUNC_SAT_F64_S -> TODO()
                        OpCode.I64_TRUNC_SAT_F64_U -> TODO()
                        OpCode.MEMORY_INIT -> unsupportedBulkMemoryOperations()
                        OpCode.DATA_DROP -> unsupportedBulkMemoryOperations()
                        OpCode.MEMORY_COPY -> unsupportedBulkMemoryOperations()
                        OpCode.MEMORY_FILL -> unsupportedBulkMemoryOperations()
                        OpCode.TABLE_INIT -> unsupportedBulkMemoryOperations()
                        OpCode.ELEM_DROP -> unsupportedBulkMemoryOperations()
                        OpCode.TABLE_COPY -> unsupportedBulkMemoryOperations()
                        OpCode.TABLE_GROW -> unsupportedReferenceTypes()
                        OpCode.TABLE_SIZE -> unsupportedReferenceTypes()
                        OpCode.TABLE_FILL -> unsupportedReferenceTypes()
                        OpCode.V128_LOAD -> unsupportedSimd()
                        OpCode.V128_LOAD8x8_S -> unsupportedSimd()
                        OpCode.V128_LOAD8x8_U -> unsupportedSimd()
                        OpCode.V128_LOAD16x4_S -> unsupportedSimd()
                        OpCode.V128_LOAD16x4_U -> unsupportedSimd()
                        OpCode.V128_LOAD32x2_S -> unsupportedSimd()
                        OpCode.V128_LOAD32x2_U -> unsupportedSimd()
                        OpCode.V128_LOAD8_SPLAT -> unsupportedSimd()
                        OpCode.V128_LOAD16_SPLAT -> unsupportedSimd()
                        OpCode.V128_LOAD32_SPLAT -> unsupportedSimd()
                        OpCode.V128_LOAD64_SPLAT -> unsupportedSimd()
                        OpCode.V128_STORE -> unsupportedSimd()
                        OpCode.V128_CONST -> unsupportedSimd()
                        OpCode.I8x16_SHUFFLE -> unsupportedSimd()
                        OpCode.I8x16_SWIZZLE -> unsupportedSimd()
                        OpCode.I8x16_SPLAT -> unsupportedSimd()
                        OpCode.I16x8_SPLAT -> unsupportedSimd()
                        OpCode.I32x4_SPLAT -> unsupportedSimd()
                        OpCode.I64x2_SPLAT -> unsupportedSimd()
                        OpCode.F32x4_SPLAT -> unsupportedSimd()
                        OpCode.F64x2_SPLAT -> unsupportedSimd()
                        OpCode.I8x16_EXTRACT_LANE_S -> unsupportedSimd()
                        OpCode.I8x16_EXTRACT_LANE_U -> unsupportedSimd()
                        OpCode.I8x16_REPLACE_LANE -> unsupportedSimd()
                        OpCode.I16x8_EXTRACT_LANE_S -> unsupportedSimd()
                        OpCode.I16x8_EXTRACT_LANE_U -> unsupportedSimd()
                        OpCode.I16x8_REPLACE_LANE -> unsupportedSimd()
                        OpCode.I32x4_EXTRACT_LANE -> unsupportedSimd()
                        OpCode.I32x4_REPLACE_LANE -> unsupportedSimd()
                        OpCode.I64x2_EXTRACT_LANE -> unsupportedSimd()
                        OpCode.I64x2_REPLACE_LANE -> unsupportedSimd()
                        OpCode.F32x4_EXTRACT_LANE -> unsupportedSimd()
                        OpCode.F32x4_REPLACE_LANE -> unsupportedSimd()
                        OpCode.F64x2_EXTRACT_LANE -> unsupportedSimd()
                        OpCode.F64x2_REPLACE_LANE -> unsupportedSimd()
                        OpCode.I8x16_EQ -> unsupportedSimd()
                        OpCode.I8x16_NE -> unsupportedSimd()
                        OpCode.I8x16_LT_S -> unsupportedSimd()
                        OpCode.I8x16_LT_U -> unsupportedSimd()
                        OpCode.I8x16_GT_S -> unsupportedSimd()
                        OpCode.I8x16_GT_U -> unsupportedSimd()
                        OpCode.I8x16_LE_S -> unsupportedSimd()
                        OpCode.I8x16_LE_U -> unsupportedSimd()
                        OpCode.I8x16_GE_S -> unsupportedSimd()
                        OpCode.I8x16_GE_U -> unsupportedSimd()
                        OpCode.I16x8_EQ -> unsupportedSimd()
                        OpCode.I16x8_NE -> unsupportedSimd()
                        OpCode.I16x8_LT_S -> unsupportedSimd()
                        OpCode.I16x8_LT_U -> unsupportedSimd()
                        OpCode.I16x8_GT_S -> unsupportedSimd()
                        OpCode.I16x8_GT_U -> unsupportedSimd()
                        OpCode.I16x8_LE_S -> unsupportedSimd()
                        OpCode.I16x8_LE_U -> unsupportedSimd()
                        OpCode.I16x8_GE_S -> unsupportedSimd()
                        OpCode.I16x8_GE_U -> unsupportedSimd()
                        OpCode.I32x4_EQ -> unsupportedSimd()
                        OpCode.I32x4_NE -> unsupportedSimd()
                        OpCode.I32x4_LT_S -> unsupportedSimd()
                        OpCode.I32x4_LT_U -> unsupportedSimd()
                        OpCode.I32x4_GT_S -> unsupportedSimd()
                        OpCode.I32x4_GT_U -> unsupportedSimd()
                        OpCode.I32x4_LE_S -> unsupportedSimd()
                        OpCode.I32x4_LE_U -> unsupportedSimd()
                        OpCode.I32x4_GE_S -> unsupportedSimd()
                        OpCode.I32x4_GE_U -> unsupportedSimd()
                        OpCode.F32x4_EQ -> unsupportedSimd()
                        OpCode.F32x4_NE -> unsupportedSimd()
                        OpCode.F32x4_LT -> unsupportedSimd()
                        OpCode.F32x4_GT -> unsupportedSimd()
                        OpCode.F32x4_LE -> unsupportedSimd()
                        OpCode.F32x4_GE -> unsupportedSimd()
                        OpCode.F64x2_EQ -> unsupportedSimd()
                        OpCode.F64x2_NE -> unsupportedSimd()
                        OpCode.F64x2_LT -> unsupportedSimd()
                        OpCode.F64x2_GT -> unsupportedSimd()
                        OpCode.F64x2_LE -> unsupportedSimd()
                        OpCode.F64x2_GE -> unsupportedSimd()
                        OpCode.V128_NOT -> unsupportedSimd()
                        OpCode.V128_AND -> unsupportedSimd()
                        OpCode.V128_ANDNOT -> unsupportedSimd()
                        OpCode.V128_OR -> unsupportedSimd()
                        OpCode.V128_XOR -> unsupportedSimd()
                        OpCode.V128_BITSELECT -> unsupportedSimd()
                        OpCode.V128_ANY_TRUE -> unsupportedSimd()
                        OpCode.V128_LOAD8_LANE -> unsupportedSimd()
                        OpCode.V128_LOAD16_LANE -> unsupportedSimd()
                        OpCode.V128_LOAD32_LANE -> unsupportedSimd()
                        OpCode.V128_LOAD64_LANE -> unsupportedSimd()
                        OpCode.V128_STORE8_LANE -> unsupportedSimd()
                        OpCode.V128_STORE16_LANE -> unsupportedSimd()
                        OpCode.V128_STORE32_LANE -> unsupportedSimd()
                        OpCode.V128_STORE64_LANE -> unsupportedSimd()
                        OpCode.V128_LOAD32_ZERO -> unsupportedSimd()
                        OpCode.V128_LOAD64_ZERO -> unsupportedSimd()
                        OpCode.F32x4_DEMOTE_LOW_F64x2_ZERO -> unsupportedSimd()
                        OpCode.F64x2_PROMOTE_LOW_F32x4 -> unsupportedSimd()
                        OpCode.I8x16_ABS -> unsupportedSimd()
                        OpCode.I8x16_NEG -> unsupportedSimd()
                        OpCode.I8x16_POPCNT -> unsupportedSimd()
                        OpCode.I8x16_ALL_TRUE -> unsupportedSimd()
                        OpCode.I8x16_BITMASK -> unsupportedSimd()
                        OpCode.I8x16_NARROW_I16x8_S -> unsupportedSimd()
                        OpCode.I8x16_NARROW_I16x8_U -> unsupportedSimd()
                        OpCode.F32x4_CEIL -> unsupportedSimd()
                        OpCode.F32x4_FLOOR -> unsupportedSimd()
                        OpCode.F32x4_TRUNC -> unsupportedSimd()
                        OpCode.F32x4_NEAREST -> unsupportedSimd()
                        OpCode.I8x16_SHL -> unsupportedSimd()
                        OpCode.I8x16_SHR_S -> unsupportedSimd()
                        OpCode.I8x16_SHR_U -> unsupportedSimd()
                        OpCode.I8x16_ADD -> unsupportedSimd()
                        OpCode.I8x16_ADD_SAT_S -> unsupportedSimd()
                        OpCode.I8x16_ADD_SAT_U -> unsupportedSimd()
                        OpCode.I8x16_SUB -> unsupportedSimd()
                        OpCode.I8x16_SUB_SAT_S -> unsupportedSimd()
                        OpCode.I8x16_SUB_SAT_U -> unsupportedSimd()
                        OpCode.F64x2_CEIL -> unsupportedSimd()
                        OpCode.F64x2_FLOOR -> unsupportedSimd()
                        OpCode.I8x16_MIN_S -> unsupportedSimd()
                        OpCode.I8x16_MIN_U -> unsupportedSimd()
                        OpCode.I8x16_MAX_S -> unsupportedSimd()
                        OpCode.I8x16_MAX_U -> unsupportedSimd()
                        OpCode.F64x2_TRUNC -> unsupportedSimd()
                        OpCode.I8x16_AVGR_U -> unsupportedSimd()
                        OpCode.I16x8_EXTADD_PAIRWISE_I8x16_S -> unsupportedSimd()
                        OpCode.I16x8_EXTADD_PAIRWISE_I8x16_U -> unsupportedSimd()
                        OpCode.I32x4_EXTADD_PAIRWISE_I16x8_S -> unsupportedSimd()
                        OpCode.I32x4_EXTADD_PAIRWISE_I16x8_U -> unsupportedSimd()
                        OpCode.I16x8_ABS -> unsupportedSimd()
                        OpCode.I16x8_NEG -> unsupportedSimd()
                        OpCode.I16x8_Q15MULR_SAT_S -> unsupportedSimd()
                        OpCode.I16x8_ALL_TRUE -> unsupportedSimd()
                        OpCode.I16x8_BITMASK -> unsupportedSimd()
                        OpCode.I16x8_NARROW_I32x4_S -> unsupportedSimd()
                        OpCode.I16x8_NARROW_I32x4_U -> unsupportedSimd()
                        OpCode.I16x8_EXTEND_LOW_I8x16_S -> unsupportedSimd()
                        OpCode.I16x8_EXTEND_HIGH_I8x16_S -> unsupportedSimd()
                        OpCode.I16x8_EXTEND_LOW_I8x16_U -> unsupportedSimd()
                        OpCode.I16x8_EXTEND_HIGH_I8x16_U -> unsupportedSimd()
                        OpCode.I16x8_SHL -> unsupportedSimd()
                        OpCode.I16x8_SHR_S -> unsupportedSimd()
                        OpCode.I16x8_SHR_U -> unsupportedSimd()
                        OpCode.I16x8_ADD -> unsupportedSimd()
                        OpCode.I16x8_ADD_SAT_S -> unsupportedSimd()
                        OpCode.I16x8_ADD_SAT_U -> unsupportedSimd()
                        OpCode.I16x8_SUB -> unsupportedSimd()
                        OpCode.I16x8_SUB_SAT_S -> unsupportedSimd()
                        OpCode.I16x8_SUB_SAT_U -> unsupportedSimd()
                        OpCode.F64x2_NEAREST -> unsupportedSimd()
                        OpCode.I16x8_MUL -> unsupportedSimd()
                        OpCode.I16x8_MIN_S -> unsupportedSimd()
                        OpCode.I16x8_MIN_U -> unsupportedSimd()
                        OpCode.I16x8_MAX_S -> unsupportedSimd()
                        OpCode.I16x8_MAX_U -> unsupportedSimd()
                        OpCode.I16x8_AVGR_U -> unsupportedSimd()
                        OpCode.I16x8_EXTMUL_LOW_I8x16_S -> unsupportedSimd()
                        OpCode.I16x8_EXTMUL_HIGH_I8x16_S -> unsupportedSimd()
                        OpCode.I16x8_EXTMUL_LOW_I8x16_U -> unsupportedSimd()
                        OpCode.I16x8_EXTMUL_HIGH_I8x16_U -> unsupportedSimd()
                        OpCode.I32x4_ABS -> unsupportedSimd()
                        OpCode.I32x4_NEG -> unsupportedSimd()
                        OpCode.I32x4_ALL_TRUE -> unsupportedSimd()
                        OpCode.I32x4_BITMASK -> unsupportedSimd()
                        OpCode.I32x4_EXTEND_LOW_I16x8_S -> unsupportedSimd()
                        OpCode.I32x4_EXTEND_HIGH_I16x8_S -> unsupportedSimd()
                        OpCode.I32x4_EXTEND_LOW_I16x8_U -> unsupportedSimd()
                        OpCode.I32x4_EXTEND_HIGH_I16x8_U -> unsupportedSimd()
                        OpCode.I32x4_SHL -> unsupportedSimd()
                        OpCode.I32x4_SHR_S -> unsupportedSimd()
                        OpCode.I32x4_SHR_U -> unsupportedSimd()
                        OpCode.I32x4_ADD -> unsupportedSimd()
                        OpCode.I32x4_SUB -> unsupportedSimd()
                        OpCode.I32x4_MUL -> unsupportedSimd()
                        OpCode.I32x4_MIN_S -> unsupportedSimd()
                        OpCode.I32x4_MIN_U -> unsupportedSimd()
                        OpCode.I32x4_MAX_S -> unsupportedSimd()
                        OpCode.I32x4_MAX_U -> unsupportedSimd()
                        OpCode.I32x4_DOT_I16x8_S -> unsupportedSimd()
                        OpCode.I32x4_EXTMUL_LOW_I16x8_S -> unsupportedSimd()
                        OpCode.I32x4_EXTMUL_HIGH_I16x8_S -> unsupportedSimd()
                        OpCode.I32x4_EXTMUL_LOW_I16x8_U -> unsupportedSimd()
                        OpCode.I32x4_EXTMUL_HIGH_I16x8_U -> unsupportedSimd()
                        OpCode.I64x2_ABS -> unsupportedSimd()
                        OpCode.I64x2_NEG -> unsupportedSimd()
                        OpCode.I64x2_ALL_TRUE -> unsupportedSimd()
                        OpCode.I64x2_BITMASK -> unsupportedSimd()
                        OpCode.I64x2_EXTEND_LOW_I32x4_S -> unsupportedSimd()
                        OpCode.I64x2_EXTEND_HIGH_I32x4_S -> unsupportedSimd()
                        OpCode.I64x2_EXTEND_LOW_I32x4_U -> unsupportedSimd()
                        OpCode.I64x2_EXTEND_HIGH_I32x4_U -> unsupportedSimd()
                        OpCode.I64x2_SHL -> unsupportedSimd()
                        OpCode.I64x2_SHR_S -> unsupportedSimd()
                        OpCode.I64x2_SHR_U -> unsupportedSimd()
                        OpCode.I64x2_ADD -> unsupportedSimd()
                        OpCode.I64x2_SUB -> unsupportedSimd()
                        OpCode.I64x2_MUL -> unsupportedSimd()
                        OpCode.I64x2_EQ -> unsupportedSimd()
                        OpCode.I64x2_NE -> unsupportedSimd()
                        OpCode.I64x2_LT_S -> unsupportedSimd()
                        OpCode.I64x2_GT_S -> unsupportedSimd()
                        OpCode.I64x2_LE_S -> unsupportedSimd()
                        OpCode.I64x2_GE_S -> unsupportedSimd()
                        OpCode.I64x2_EXTMUL_LOW_I32x4_S -> unsupportedSimd()
                        OpCode.I64x2_EXTMUL_HIGH_I32x4_S -> unsupportedSimd()
                        OpCode.I64x2_EXTMUL_LOW_I32x4_U -> unsupportedSimd()
                        OpCode.I64x2_EXTMUL_HIGH_I32x4_U -> unsupportedSimd()
                        OpCode.F32x4_ABS -> unsupportedSimd()
                        OpCode.F32x4_NEG -> unsupportedSimd()
                        OpCode.F32x4_SQRT -> unsupportedSimd()
                        OpCode.F32x4_ADD -> unsupportedSimd()
                        OpCode.F32x4_SUB -> unsupportedSimd()
                        OpCode.F32x4_MUL -> unsupportedSimd()
                        OpCode.F32x4_DIV -> unsupportedSimd()
                        OpCode.F32x4_MIN -> unsupportedSimd()
                        OpCode.F32x4_MAX -> unsupportedSimd()
                        OpCode.F32x4_PMIN -> unsupportedSimd()
                        OpCode.F32x4_PMAX -> unsupportedSimd()
                        OpCode.F64x2_ABS -> unsupportedSimd()
                        OpCode.F64x2_NEG -> unsupportedSimd()
                        OpCode.F64x2_SQRT -> unsupportedSimd()
                        OpCode.F64x2_ADD -> unsupportedSimd()
                        OpCode.F64x2_SUB -> unsupportedSimd()
                        OpCode.F64x2_MUL -> unsupportedSimd()
                        OpCode.F64x2_DIV -> unsupportedSimd()
                        OpCode.F64x2_MIN -> unsupportedSimd()
                        OpCode.F64x2_MAX -> unsupportedSimd()
                        OpCode.F64x2_PMIN -> unsupportedSimd()
                        OpCode.F64x2_PMAX -> unsupportedSimd()
                        OpCode.I32x4_TRUNC_SAT_F32X4_S -> unsupportedSimd()
                        OpCode.I32x4_TRUNC_SAT_F32X4_U -> unsupportedSimd()
                        OpCode.F32x4_CONVERT_I32x4_S -> unsupportedSimd()
                        OpCode.F32x4_CONVERT_I32x4_U -> unsupportedSimd()
                        OpCode.I32x4_TRUNC_SAT_F64x2_S_ZERO -> unsupportedSimd()
                        OpCode.I32x4_TRUNC_SAT_F64x2_U_ZERO -> unsupportedSimd()
                        OpCode.F64x2_CONVERT_LOW_I32x4_S -> unsupportedSimd()
                        OpCode.F64x2_CONVERT_LOW_I32x4_U -> unsupportedSimd()
                    }
                }
            }
        }

        return when (paramCount) {
            0 -> when (returnCount) {
                0 -> ProgramFunction0To0(name, body)
                1 -> ProgramFunction0To1(name, body)
                2 -> ProgramFunction0To2(name, body)
                3 -> ProgramFunction0To3(name, body)
                4 -> ProgramFunction0To4(name, body)
                5 -> ProgramFunction0To5(name, body)
                6 -> ProgramFunction0To6(name, body)
                7 -> ProgramFunction0To7(name, body)
                8 -> ProgramFunction0To8(name, body)
                else -> ProgramFunction(name, args = 0, outputs = returnCount, body)
            }

            1 -> when (returnCount) {
                0 -> ProgramFunction1To0(name, body)
                1 -> ProgramFunction1To1(name, body)
                2 -> ProgramFunction1To2(name, body)
                3 -> ProgramFunction1To3(name, body)
                4 -> ProgramFunction1To4(name, body)
                5 -> ProgramFunction1To5(name, body)
                6 -> ProgramFunction1To6(name, body)
                7 -> ProgramFunction1To7(name, body)
                8 -> ProgramFunction1To8(name, body)
                else -> ProgramFunction(name, args = 1, outputs = returnCount, body)
            }

            2 -> when (returnCount) {
                0 -> ProgramFunction2To0(name, body)
                1 -> ProgramFunction2To1(name, body)
                2 -> ProgramFunction2To2(name, body)
                3 -> ProgramFunction2To3(name, body)
                4 -> ProgramFunction2To4(name, body)
                5 -> ProgramFunction2To5(name, body)
                6 -> ProgramFunction2To6(name, body)
                7 -> ProgramFunction2To7(name, body)
                8 -> ProgramFunction2To8(name, body)
                else -> ProgramFunction(name, args = 2, outputs = returnCount, body)
            }

            3 -> when (returnCount) {
                0 -> ProgramFunction3To0(name, body)
                1 -> ProgramFunction3To1(name, body)
                2 -> ProgramFunction3To2(name, body)
                3 -> ProgramFunction3To3(name, body)
                4 -> ProgramFunction3To4(name, body)
                5 -> ProgramFunction3To5(name, body)
                6 -> ProgramFunction3To6(name, body)
                7 -> ProgramFunction3To7(name, body)
                8 -> ProgramFunction3To8(name, body)
                else -> ProgramFunction(name, args = 3, outputs = returnCount, body)
            }

            4 -> when (returnCount) {
                0 -> ProgramFunction4To0(name, body)
                1 -> ProgramFunction4To1(name, body)
                2 -> ProgramFunction4To2(name, body)
                3 -> ProgramFunction4To3(name, body)
                4 -> ProgramFunction4To4(name, body)
                5 -> ProgramFunction4To5(name, body)
                6 -> ProgramFunction4To6(name, body)
                7 -> ProgramFunction4To7(name, body)
                8 -> ProgramFunction4To8(name, body)
                else -> ProgramFunction(name, args = 4, outputs = returnCount, body)
            }

            5 -> when (returnCount) {
                0 -> ProgramFunction5To0(name, body)
                1 -> ProgramFunction5To1(name, body)
                2 -> ProgramFunction5To2(name, body)
                3 -> ProgramFunction5To3(name, body)
                4 -> ProgramFunction5To4(name, body)
                5 -> ProgramFunction5To5(name, body)
                6 -> ProgramFunction5To6(name, body)
                7 -> ProgramFunction5To7(name, body)
                8 -> ProgramFunction5To8(name, body)
                else -> ProgramFunction(name, args = 5, outputs = returnCount, body)
            }

            6 -> when (returnCount) {
                0 -> ProgramFunction6To0(name, body)
                1 -> ProgramFunction6To1(name, body)
                2 -> ProgramFunction6To2(name, body)
                3 -> ProgramFunction6To3(name, body)
                4 -> ProgramFunction6To4(name, body)
                5 -> ProgramFunction6To5(name, body)
                6 -> ProgramFunction6To6(name, body)
                7 -> ProgramFunction6To7(name, body)
                8 -> ProgramFunction6To8(name, body)
                else -> ProgramFunction(name, args = 6, outputs = returnCount, body)
            }

            7 -> when (returnCount) {
                0 -> ProgramFunction7To0(name, body)
                1 -> ProgramFunction7To1(name, body)
                2 -> ProgramFunction7To2(name, body)
                3 -> ProgramFunction7To3(name, body)
                4 -> ProgramFunction7To4(name, body)
                5 -> ProgramFunction7To5(name, body)
                6 -> ProgramFunction7To6(name, body)
                7 -> ProgramFunction7To7(name, body)
                8 -> ProgramFunction7To8(name, body)
                else -> ProgramFunction(name, args = 7, outputs = returnCount, body)
            }

            8 -> when (returnCount) {
                0 -> ProgramFunction8To0(name, body)
                1 -> ProgramFunction8To1(name, body)
                2 -> ProgramFunction8To2(name, body)
                3 -> ProgramFunction8To3(name, body)
                4 -> ProgramFunction8To4(name, body)
                5 -> ProgramFunction8To5(name, body)
                6 -> ProgramFunction8To6(name, body)
                7 -> ProgramFunction8To7(name, body)
                8 -> ProgramFunction8To8(name, body)
                else -> ProgramFunction(name, args = 8, outputs = returnCount, body)
            }

            else -> ProgramFunction(name, args = paramCount, outputs = returnCount, body)
        }
    }

    companion object {
        fun unsupportedSimd(): Nothing {
            throw UnsupportedOperationException("SIMD extension is not supported")
        }

        fun unsupportedReferenceTypes(): Nothing {
            throw UnsupportedOperationException("Reference Types extension is not supported")
        }

        fun unsupportedTypedFunctionReferenceTypes(): Nothing {
            throw UnsupportedOperationException("Typed Function Reference extension is not supported")
        }

        fun unsupportedBulkMemoryOperations(): Nothing {
            throw UnsupportedOperationException("Bulk Memory Operations extension is not supported")
        }

        fun unsupportedTailCall(): Nothing {
            throw UnsupportedOperationException("Tail Call extension is not supported")
        }

        fun unsupportedExceptionHandling(): Nothing {
            throw UnsupportedOperationException("Exception Handling extension is not supported")
        }
    }
}