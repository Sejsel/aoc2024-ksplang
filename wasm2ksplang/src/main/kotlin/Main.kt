package cz.sejsel

import com.dylibso.chicory.runtime.GlobalInstance
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.wasm.InstantiatedKsplangWasmModule
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To1
import cz.sejsel.ksplang.dsl.core.ProgramFunctionBase
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.dsl.core.pushAddressOf
import cz.sejsel.ksplang.dsl.core.whileNonZero
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.std.dupKthZeroIndexed
import cz.sejsel.ksplang.std.mul
import cz.sejsel.ksplang.std.negate
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.roll
import cz.sejsel.ksplang.std.stacklen
import cz.sejsel.ksplang.std.swap2
import cz.sejsel.ksplang.std.yoink
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromPath
import java.io.File
import kotlin.io.path.Path

fun main() {
    //val parsed = Parser.parse(Path("wasm/ksplang_web_bg.wasm"))
    //val parsed = Parser.parse(Path("wasm/add.wasm"))
    val parsed = Parser.parse(Path("wasm/table.wasm"))
    //println(parsed)

    val tableStore = Store()
    val tableInstance = tableStore.instantiate("table", parsed)
    val times3 = tableInstance.export("times3")
    times3.apply(7).also { println("7 * 3 = ${it[0]}") }

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
    // slow down memory access significantly. Memory is comparatively cheap, we can
    // afford using 8 times more than necessary.

    // Note that we instantiate modules statically, at ksplang generation time.
    // This allows us to statically know what module a function belongs to when generating its code,
    // completely removing the need for a runtime module representation.
    // Additionally, the runtime store can be simplified drastically, the only dynamically accessed
    // part is the function list (through call_indirect), and it does not change during execution.

    // TODO: This is all very manual for now, you need to use the correct incantations
    //  in the correct order or things blow up (silently).

    val wasmStore = Store()
    val translator = KsplangWasmModuleTranslator()
    val addWasmModule = instantiateModuleFromPath(translator, Path("wasm/add.wasm"), name = "add", store)


    val ksplang = program {
        // Add functions from WASM module to the program
        addWasmModule.install(this)

        // Export functions from module for easy access
        val addFunction = addWasmModule.getExportedFunction(this, "add")!! as ProgramFunction2To1

        body {
            // TODO: Ensure we ran start before this!

            initializeSingleMemoryWasmRuntimeData(addWasmModule.toRuntimeData())
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

/**
 * Build a ksplang program with one embedded WASM module, and run one of its functions as the main body.
 */
fun buildSingleModuleProgram(
    module: InstantiatedKsplangWasmModule,
    block: ((WasmBuilder).() -> Unit)
): KsplangProgram {
    return program {
        // Add functions from WASM module to the program
        module.install(this)

        body {
            // TODO: Ensure we ran start before this!

            val runtimeData = module.toRuntimeData()
            val builder: WasmBuilder = if (runtimeData.memory.size == 0) {
                val indices = initializeNoMemoryWasmRuntimeData(runtimeData)
                NoMemoryWasmBuilder(
                    builder = this@program,
                    body = this,
                    indices = indices,
                    module = module
                )
            } else if (runtimeData.memory.size == 1) {
                val indices = initializeSingleMemoryWasmRuntimeData(runtimeData)

                // The stack now starts with
                // 0 input_len [globals] [fun_table] [mem_size mem_max_size [mem_pages]] [input]

                SingleModuleWasmBuilder(
                    builder = this@program,
                    body = this,
                    indices = indices,
                    module = module
                )
            } else {
                error("Multiple memories not supported")
            }

            builder.block()
        }
    }
}

interface WasmBuilder {
    fun getExportedFunction(name: String): ProgramFunctionBase?

    fun body(block: ComplexFunction.() -> Unit)

    fun ComplexFunction.getInputSize(): ComplexFunction
    fun ComplexFunction.yoinkInput(): ComplexFunction
    fun ComplexFunction.yoinkInput(k: Int): ComplexFunction

    fun build(): KsplangProgram
}

class NoMemoryWasmBuilder(
    private val module: InstantiatedKsplangWasmModule,
    private val builder: KsplangProgramBuilder,
    private val body: ComplexFunction,
    val indices: NoMemoryRuntimeIndexes,
) : WasmBuilder {
    private var bodyCalled = false

    override fun getExportedFunction(name: String): ProgramFunctionBase? = module.getExportedFunction(builder, name)

    override fun body(block: ComplexFunction.() -> Unit) {
        check(!bodyCalled) { "Body can only be set once" }
        bodyCalled = true

        block(body)
    }

    override fun ComplexFunction.getInputSize() = complexFunction("inputSize") {
        push(indices.inputLenIndex)
        yoink()
    }

    /**
     * Signature: ```i -> input[i]```
     */
    override fun ComplexFunction.yoinkInput() = complexFunction("yoinkInput") {
        push(indices.inputStartIndex)
        add()
        yoink()
    }

    /**
     * Signature: ``` -> input[k]```
     */
    override fun ComplexFunction.yoinkInput(k: Int) = complexFunction("yoinkInput($k)") {
        push(indices.inputStartIndex + k)
        yoink()
    }

    override fun build(): KsplangProgram = builder.build()
}

class SingleModuleWasmBuilder(
    private val module: InstantiatedKsplangWasmModule,
    private val builder: KsplangProgramBuilder,
    private val body: ComplexFunction,
    val indices: SingleMemoryRuntimeIndexes,
) : WasmBuilder {
    private var bodyCalled = false

    override fun getExportedFunction(name: String): ProgramFunctionBase? = module.getExportedFunction(builder, name)

    override fun body(block: ComplexFunction.() -> Unit) {
        check(!bodyCalled) { "Body can only be set once" }
        bodyCalled = true

        block(body)
    }

    override fun ComplexFunction.getInputSize() = complexFunction("inputSize") {
        push(indices.inputLenIndex)
        yoink()
    }

    /**
     * Signature: ```i -> input[i]```
     */
    override fun ComplexFunction.yoinkInput() = complexFunction("yoinkInput") {
        // input starts at memStartIndex + memSize * 65536

        // i
        push(indices.memSizeIndex)
        yoink()
        // i mem_size
        push(65536)
        mul()
        // i mem_size*65536

        push(indices.memStartIndex)
        // i mem_size*65536 mem_start
        add()
        // i mem_start+mem_size*65536
        add()
        // i+mem_start+mem_size*65536
        yoink()
    }

    /**
     * Signature: ``` -> input[k]```
     */
    override fun ComplexFunction.yoinkInput(k: Int) = complexFunction("yoinkInput($k)") {
        // input starts at memStartIndex + memSize * 65536

        push(indices.memSizeIndex)
        yoink()
        // mem_size
        push(65536)
        mul()
        // mem_size*65536

        push(indices.memStartIndex + k)
        // mem_size*65536 mem_start+k
        add()
        // k+mem_start+mem_size*65536
        yoink()
    }

    override fun build(): KsplangProgram = builder.build()
}

fun InstantiatedKsplangWasmModule.toRuntimeData(): RuntimeData {
    // Chicory Store only has exported values from each Instance,
    // to get everything, we have to go through Instances
    val globals = instance.getGlobals().map {
        // TODO: When adding F32/F64, just check it works as expected, it is most likely fine
        check(it.type in listOf(ValType.I32, ValType.I64)) { "Unsupported type" }
        check(it.valueHigh == 0L) { "Unsupported high part" }
        it
    }
    val memories = instance.memory()?.let { listOf(instance.memory()) } ?: emptyList<Memory>()
    val tables = instance.getTables()
    val funTable: List<ProgramFunctionBase?> = if (tables.size == 0) {
        emptyList()
    } else {
        check(tables.size == 1) { "Multiple tables not supported" }
        val table = tables[0]
        check(table.elementType() == ValType.FuncRef) { "Only funcref tables supported" }
        (0..<table.size()).map { i ->
            val refId = table.ref(i)
            if (refId == -1) null else {
                getFunction(i)
            }
        }
    }
    return RuntimeData(globals, funTable, memories)
}

data class RuntimeData(
    val globals: List<GlobalInstance>,
    val funTable: List<ProgramFunctionBase?>,
    val memory: List<Memory>,
)

data class SingleMemoryRuntimeIndexes(
    val inputLenIndex: Int,
    val globalsStartIndex: Int,
    val globalsCount: Int,
    val funTableStartIndex: Int,
    val funTableCount: Int,
    val memSizeIndex: Int,
    val memMaxSizeIndex: Int,
    val memStartIndex: Int,
)

data class NoMemoryRuntimeIndexes(
    val inputLenIndex: Int,
    val globalsStartIndex: Int,
    val globalsCount: Int,
    val funTableStartIndex: Int,
    val funTableCount: Int,
    val inputStartIndex: Int
)


// input -> 0 input_len [globals] [fun_table] [input]
//          ^ leaving that available for L-swap or other optimizations
// input is statically adressable
private fun ComplexBlock.initializeNoMemoryWasmRuntimeData(runtimeData: RuntimeData): NoMemoryRuntimeIndexes {
    check(runtimeData.memory.isEmpty())

    // [input]
    stacklen()
    push(0)
    swap2()
    // [input] 0 inputlen
    runtimeData.globals.forEach {
        push(it.value)
    }
    // [input] 0 inputlen [globals]
    runtimeData.funTable.forEach {
        if (it != null) pushAddressOf(it) else push(-1)
    }
    // [input] 0 inputlen [globals] [fun_table]

    val staticSize = 2 + runtimeData.globals.size + runtimeData.funTable.size
    dupKthZeroIndexed(staticSize - 2)

    // [input] 0 inputlen [globals] [fun_table] inputlen
    dup()
    push(staticSize)
    add()
    // [input] 0 inputlen [globals] [fun_table] inputlen inputlen+static_size
    swap2()
    negate()
    swap2()
    // [input] 0 inputlen [globals] [fun_table] -inputlen inputlen+static_size
    lroll()
    // 0 inputlen [globals] [fun_table] [input]
    return NoMemoryRuntimeIndexes(
        inputLenIndex = 1,
        globalsStartIndex = 2,
        globalsCount = runtimeData.globals.size,
        funTableStartIndex = 2 + runtimeData.globals.size,
        funTableCount = runtimeData.funTable.size,
        inputStartIndex = 2 + runtimeData.globals.size + runtimeData.funTable.size,
    )
}

// input -> 0 input_len [globals] [fun_table] [mem_size mem_max_size [mem_pages]] [input]
//          ^ leaving that available for L-swap or other optimizations
// everything up to mem_pages is static size, so we can have access to memory without indirect addressing
// input moves with memory growth, it starts at static_size + mem_size * 65536
// all instantiations must be finished at this point
private fun ComplexBlock.initializeSingleMemoryWasmRuntimeData(runtimeData: RuntimeData): SingleMemoryRuntimeIndexes {
    check(runtimeData.memory.size == 1)
    val memory = runtimeData.memory.single()

    // [input]
    stacklen()
    push(0)
    swap2()
    // [input] 0 inputlen
    runtimeData.globals.forEach {
        push(it.value)
    }
    // [input] 0 inputlen [globals]
    runtimeData.funTable.forEach {
        if (it != null) {
            pushAddressOf(it)
        } else {
            push(-1)
        }
    }
    // [input] 0 inputlen [globals] [fun_table]
    push(memory.pages())
    // [input] 0 inputlen [globals] [fun_table] mem_size
    push(memory.maximumPages())
    // [input] 0 inputlen [globals] [fun_table] mem_size mem_max_size

    val chunks = MemoryChunker.chunkMemory(memory, minZeroesToChunk = 200)
    chunks.forEach {
        when (it) {
            is MemoryChunk.Element -> {
                push(it.long.toUByte().toLong())
            }

            is MemoryChunk.Zeroes -> pushManyZeroes(it.count.toLong())
        }
    }

    val staticSize = 2 + runtimeData.globals.size + runtimeData.funTable.size + 2
    val memorySize = memory.pages() * 65536L
    dupKthZeroIndexed(staticSize + memorySize - 2)
    // [input] 0 inputlen [globals] [fun_table] mem_size mem_max_size [mem] inputlen
    dup()
    push(staticSize + memorySize)
    add()
    // [input] 0 inputlen [globals] [fun_table] mem_size mem_max_size [mem] inputlen inputlen+static_size+mem_size
    swap2()
    negate()
    swap2()
    // [input] 0 inputlen [globals] [fun_table] mem_size mem_max_size [mem] -inputlen inputlen+static_size+mem_size
    lroll()
    // 0 inputlen [globals] [fun_table] mem_size mem_max_size [mem] [input]

    return SingleMemoryRuntimeIndexes(
        inputLenIndex = 1,
        globalsStartIndex = 2,
        globalsCount = runtimeData.globals.size,
        funTableStartIndex = 2 + runtimeData.globals.size,
        funTableCount = runtimeData.funTable.size,
        memSizeIndex = 2 + runtimeData.globals.size + runtimeData.funTable.size,
        memMaxSizeIndex = 3 + runtimeData.globals.size + runtimeData.funTable.size,
        memStartIndex = 4 + runtimeData.globals.size + runtimeData.funTable.size,
    )
}

private const val ZEROES_PER_ITERATION = 100

/**
 * Allocates a block of zeroes into the stack, starting from position `from` and with length `len`.
 *
 * Signature:  -> [0 0 0 0... 0 0 0 0]
 */
fun ComplexBlock.pushManyZeroes(count: Long) = complexFunction("pushManyZeroes($count)") {
    require(count >= 0L) { "Count must be non-negative, got $count" }
    if (count == 0L) {
        return@complexFunction
    }

    if (count < ZEROES_PER_ITERATION) {
        push(0)
        repeat((count - 1).toInt()) {
            CS()
        }
        // [count * 0]
    } else {
        val iterations = count / ZEROES_PER_ITERATION
        val remainder = count % ZEROES_PER_ITERATION
        push(iterations)
        // i
        whileNonZero {
            // [000] i
            push(0)
            repeat(ZEROES_PER_ITERATION - 1) {
                CS()
            }
            // [000] i [ZEROES_PER_ITERATION * 0]
            roll((ZEROES_PER_ITERATION + 1).toLong(), -1)
            // [000] i
            dec()
            // [000] i-1
        }
        // [000]
        repeat(remainder.toInt()) {
            CS()
        }
    }
    // [count * 0]
}

object MemoryChunker {
    fun chunkMemory(memory: Memory, minZeroesToChunk: Int): List<MemoryChunk> {
        val size = (memory.pages() * 65536)

        val chunks = mutableListOf<MemoryChunk>()
        var zeroes = 0
        for (i in 0 until size) {
            val value = memory.read(i)
            if (value == 0.toByte()) {
                zeroes++
            } else {
                if (zeroes > 0) {
                    if (zeroes >= minZeroesToChunk) {
                        chunks.add(MemoryChunk.Zeroes(zeroes))
                    } else {
                        repeat(zeroes) { chunks.add(MemoryChunk.Element(0)) }
                    }
                    zeroes = 0
                }
                chunks.add(MemoryChunk.Element(value))
            }
        }
        if (zeroes > 0) {
            if (zeroes >= minZeroesToChunk) {
                chunks.add(MemoryChunk.Zeroes(zeroes))
            } else {
                repeat(zeroes) { chunks.add(MemoryChunk.Element(0)) }
            }
        }
        return chunks
    }
}

sealed interface MemoryChunk {
    data class Element(val long: Byte) : MemoryChunk
    data class Zeroes(val count: Int) : MemoryChunk
}

