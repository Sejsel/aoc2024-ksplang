package cz.sejsel.ksplang.benchmarks

import com.dylibso.chicory.runtime.Store
import cz.sejsel.buildSingleModuleProgram
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.interpreter.VMOptions
import cz.sejsel.ksplang.interpreter.parseProgram
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.leaveTop
import cz.sejsel.ksplang.std.permute
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.sort
import cz.sejsel.ksplang.std.stacklen
import cz.sejsel.ksplang.std.swap2
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromPath
import java.nio.file.Path
import kotlin.io.path.Path

class BenchmarkProgram(val name: String, val program: String, val inputStack: List<Long>) {
    val ops = parseProgram(program)
    val vmOptions = VMOptions(
        initialStack = inputStack,
        maxStackSize = 1_000_000,
        piDigits = listOf(3, 1, 4),
        maxOpCount = Long.MAX_VALUE,
    )
}

object Programs {
    private val builder = KsplangBuilder()

    private val ksplangInterpreterProgram by lazy {
        // not really sum, name kept for historical reasons, it returns a ptr to the stack len,
        // followed by len elements of the result stack
        buildWasmSlicePtrProgram(
            builder,
            Path("benchmarks/wasm/ksplang_wasm.wasm"),
            "sum_ksplang_result"
        )
    }

    val sumloop10000 = BenchmarkProgram(
        name = "sumloop10000",
        program = builder.build(buildComplexFunction { sum() }),
        inputStack = (1..10000L) + 10000L
    )
    val stacklen10000 = BenchmarkProgram(
        name = "stacklen10000",
        program = builder.build(buildComplexFunction { stacklen() }),
        inputStack = (1..10000L).toList()
    )

    val sort100 = BenchmarkProgram(
        name = "sort100",
        program = builder.build(buildComplexFunction { sort() }),
        inputStack = (1..100L).reversed() + 100L
    )

    val wasmaoc24day2 = BenchmarkProgram(
        name = "wasmaoc24day2",
        program = buildWasmI64Program(builder, Path("benchmarks/wasm/aoc24day2.wasm"), "day2part1"),
        inputStack = aoc24day2SampleInput.map { it.code.toLong() }
    )

    val wasmksplangpush1 = BenchmarkProgram(
        name = "wasmksplangpush1",
        program = ksplangInterpreterProgram,
        inputStack = "CS CS lensum CS funkcia ++;20 30".map { it.code.toLong() }
    )

    val wasmi32factorial200 = BenchmarkProgram(
        name = "wasmi32factorial200",
        program = buildWasmI64Program(builder, Path("benchmarks/wasm/i32_factorial.wasm"), "factorial"),
        inputStack = listOf(200L)
    )
    val wasmi64factorial200 = BenchmarkProgram(
        name = "wasmi64factorial200",
        program = buildWasmI64Program(builder, Path("benchmarks/wasm/i64_factorial.wasm"), "factorial"),
        inputStack = listOf(200L)
    )
}

// TODO: Replace will real input or subset
private val aoc24day2SampleInput = """7 6 4 2 1
1 2 7 8 9
9 7 6 2 1
1 3 2 4 5
8 6 4 4 1
1 3 6 7 9""".trimIndent()

private fun buildWasmI64Program(builder: KsplangBuilder, wasmPath: Path, functionName: String): String {
    val translator = KsplangWasmModuleTranslator()

    val store = Store()
    val module = instantiateModuleFromPath(translator, wasmPath, "module", store)
    val program = buildSingleModuleProgram(module) {
        val mainFunction = getExportedFunction(functionName) as ProgramFunction0To1

        body {
            call(mainFunction)
            // i64
            leaveTop() // destroys runtime layout
        }
    }

    val annotated = builder.buildAnnotated(program)
    return annotated.toRunnableProgram()
}

private fun buildWasmSlicePtrProgram(
    builder: KsplangBuilder,
    wasmPath: Path,
    functionName: String
): String {
    val translator = KsplangWasmModuleTranslator()

    val store = Store()
    val module = instantiateModuleFromPath(translator, wasmPath, "module", store)
    val program = buildSingleModuleProgram(module) {
        val mainFunction = getExportedFunction(functionName) as ProgramFunction0To1
        body {
            call(mainFunction)
            // pointer
            keepOnlyMemoryPtr() // destroys runtime layout
        }
    }

    val annotated = builder.buildAnnotated(program)
    return annotated.toRunnableProgram()
}

private fun ComplexBlock.sum() {
    // n
    push(0); swap2()
    // 0 n
    doWhileNonZero {
        // [...] x sum i
        permute("x sum i", "i x sum")
        add()
        // i x+sum
        swap2()
        // x+sum i
        dec()
        // sum i-1
        CS()
    }
    // sum 0
    pop()
}