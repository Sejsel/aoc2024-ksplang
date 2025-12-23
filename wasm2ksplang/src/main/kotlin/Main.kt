package cz.sejsel

import com.dylibso.chicory.runtime.GlobalInstance
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.wasm.InstantiatedKsplangWasmModule
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import cz.sejsel.ksplang.dsl.core.KsplangProgramBuilder
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.ProgramFunctionBase
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.dsl.core.pushAddressOf
import cz.sejsel.ksplang.dsl.core.whileNonZero
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.std.dupAb
import cz.sejsel.ksplang.std.dupFourth
import cz.sejsel.ksplang.std.dupKthZeroIndexed
import cz.sejsel.ksplang.std.dupThird
import cz.sejsel.ksplang.std.mul
import cz.sejsel.ksplang.std.negate
import cz.sejsel.ksplang.std.popMany
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.pushOn
import cz.sejsel.ksplang.std.roll
import cz.sejsel.ksplang.std.sgn
import cz.sejsel.ksplang.std.stacklen
import cz.sejsel.ksplang.std.stacklenWithMin
import cz.sejsel.ksplang.std.sub
import cz.sejsel.ksplang.std.swap2
import cz.sejsel.ksplang.std.yeet
import cz.sejsel.ksplang.std.yoink
import cz.sejsel.ksplang.wasm.KsplangWasmModuleTranslator
import cz.sejsel.ksplang.wasm.instantiateModuleFromPath
import getGlobals
import getTables
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import kotlin.io.path.Path

private val logger = KotlinLogging.logger {}

fun main() {
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

    /*
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
     */
    val translator = KsplangWasmModuleTranslator()

    val store = Store()
    val module = instantiateModuleFromPath(translator, Path("wasm/ksplang_wasm.wasm"), "interpreter", store)
    val program = buildSingleModuleProgram(module) {
        val sum = getExportedFunction("sum_ksplang_result") as ProgramFunction0To1

        body {
            call(sum)
            // pointer
            keepOnlyMemoryPtr() // destroys runtime layout
        }
    }

    val referenceStore = Store()
    val input = "CS CS lensum CS funkcia ++;20 30".map { it.code.toLong() }
    referenceStore.addFunction(
        TestChicoryHostFunctions.readInput(input),
        TestChicoryHostFunctions.inputSize(input),
        TestChicoryHostFunctions.saveRaw()
    )
    val func = referenceStore.instantiate("mod", module.module.chicoryModule).export("sum_ksplang_result")!!
    val expectedResult = func.apply().single()
    println(expectedResult)

    println("Translated program tree")

    val annotated = builder.buildAnnotated(program)
    val ksplang = annotated.toRunnableProgram()
    File("interpreter.ksplang").writeText(ksplang)
    println("Saved program with ${ksplang.length} chars")
    val instructionCount = ksplang.trim().split("\\s+".toRegex()).count()
    println("Saved program with $instructionCount instructions")
    File("interpreter.ksplang.json").writeText(annotated.toAnnotatedTreeJson())
    println("Saved annotated json")
}

object TestChicoryHostFunctions {
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

    fun saveRaw(): HostFunction {
        return HostFunction(
            "env",
            "save_raw_i64",
            FunctionType.of(listOf(ValType.I64, ValType.I32), listOf()),
        ) { _, args ->
            val value = args[0]
            val index = args[1].toInt()
            println("Saving raw: [$index] = $value")
            longArrayOf()
        }
    }
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
            val builder: WasmBuilder = if (runtimeData.memory.isEmpty()) {
                val indices = initializeNoMemoryWasmRuntimeData(runtimeData)

                // The stack now starts with
                // 0 input_len [globals] [fun_table] [input]

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

            module.getGetGlobalFunctions().forEach { (index, function) ->
                function.setBody {
                    // TODO: For immutable globals, we can just push, they would not even have to be in runtime data
                    with(builder) { yoinkGlobal(index) }
                }
            }

            module.getSetGlobalFunctions().forEach { (index, function) ->
                function.setBody {
                    with(builder) { yeetGlobal(index) }
                }
            }

            module.getGetMemoryFunction()?.let {
                it.setBody {
                    with(builder) { yoinkMemory() }
                }
            }

            module.getSetMemoryFunction()?.let {
                it.setBody {
                    with(builder) { yeetMemory() }
                }
            }

            module.getGetMemorySizeFunction()?.let {
                it.setBody {
                    with(builder) { getMemorySize() }
                }
            }

            module.getGrowMemoryFunction()?.let {
                it.setBody {
                    with(builder) { growMemory() }
                }
            }

            module.getReadInputFunction()?.let {
                it.setBody {
                    with(builder) { yoinkInput() }
                }
            }

            module.getGetInputSizeFunction()?.let {
                it.setBody {
                    with(builder) { getInputSize() }
                }
            }

            module.getGetFunctionAddressFunction()?.let {
                it.setBody {
                    with(builder) { getFunctionAddress() }
                }
            }

            module.getSaveRawFunction()?.let {
                it.setBody {
                    with(builder) { yeetMemory() }
                }
            }

            module.getReadRawFunction()?.let {
                it.setBody {
                    with(builder) { yoinkMemory() }
                }
            }

            module.getSetInputFunction()?.let {
                it.setBody {
                    with(builder) { yeetInput() }
                }
            }

            builder.block()
        }
    }
}

interface WasmBuilder {
    fun getExportedFunction(name: String): ProgramFunctionBase?

    fun body(block: ComplexBlock.() -> Unit)

    fun ComplexBlock.getInputSize(): ComplexFunction
    fun ComplexBlock.getFunctionAddress(): ComplexFunction
    fun ComplexBlock.yoinkInput(): ComplexFunction
    /** Signature: ```index value -> input[index] = value``` */
    fun ComplexBlock.yeetInput(): ComplexFunction
    fun ComplexBlock.yoinkInput(index: Int): ComplexFunction
    fun ComplexBlock.yoinkGlobal(index: Int): ComplexFunction
    fun ComplexBlock.yeetGlobal(index: Int): ComplexFunction
    fun ComplexBlock.yoinkMemory(): ComplexFunction
    fun ComplexBlock.yoinkMemory(index: Int): ComplexFunction
    /** Signature: ```v i -> memory[i] = v``` */
    fun ComplexBlock.yeetMemory(): ComplexFunction
    fun ComplexBlock.getMemorySize(): ComplexFunction
    fun ComplexBlock.growMemory(): ComplexFunction
    /**
     * Given a memory pointer to a slice (len as first element, then len elements), remove everything
     * else on the ksplang stack. **Destroys the runtime layout**, useful at the end of programs.
     *
     * Signature: ```ptr -> mem[ptr+1] ... mem[ptr+1+mem[ptr]]```
     */
    fun ComplexBlock.keepOnlyMemoryPtr(): ComplexFunction

    fun build(): KsplangProgram
}

class NoMemoryWasmBuilder(
    private val module: InstantiatedKsplangWasmModule,
    private val builder: KsplangProgramBuilder,
    private val body: ComplexBlock,
    val indices: NoMemoryRuntimeIndexes,
) : WasmBuilder {
    private var bodyCalled = false

    override fun getExportedFunction(name: String): ProgramFunctionBase? = module.getExportedFunction(builder, name)

    override fun body(block: ComplexBlock.() -> Unit) {
        check(!bodyCalled) { "Body can only be set once" }
        bodyCalled = true

        block(body)
    }

    override fun ComplexBlock.getInputSize() = complexFunction("inputSize") {
        push(indices.inputLenIndex)
        yoink()
    }

    override fun ComplexBlock.getFunctionAddress(): ComplexFunction = complexFunction("getFunctionAddress") {
        // i
        push(indices.funTableStartIndex)
        add()
        yoink()
    }

    /**
     * Signature: ```i -> input[i]```
     */
    override fun ComplexBlock.yoinkInput() = complexFunction("yoinkInput") {
        push(indices.inputStartIndex)
        add()
        yoink()
    }

    override fun ComplexBlock.yeetInput(): ComplexFunction = complexFunction("yeetInput") {
        // value index
        push(indices.inputStartIndex)
        add()
        yeet()
    }

    /**
     * Signature: ``` -> input[k]```
     */
    override fun ComplexBlock.yoinkInput(index: Int) = complexFunction("yoinkInput($index)") {
        push(indices.inputStartIndex + index)
        yoink()
    }

    /**
     * Signature: ```-> globals[index]```
     */
    override fun ComplexBlock.yoinkGlobal(index: Int): ComplexFunction = complexFunction("yoinkGlobal($index)") {
        push(indices.globalsStartIndex + index)
        yoink()
    }

    /**
     * Signature : ```v -> globals[index] = v```
     */
    override fun ComplexBlock.yeetGlobal(index: Int): ComplexFunction = complexFunction("yeetGlobal($index)") {
        // v
        push(indices.globalsStartIndex + index)
        // v i
        yeet()
    }

    override fun ComplexBlock.yoinkMemory(): ComplexFunction {
        error("Not supported")
    }

    override fun ComplexBlock.yoinkMemory(index: Int): ComplexFunction {
        error("Not supported")
    }

    override fun ComplexBlock.yeetMemory(): ComplexFunction {
        error("Not supported")
    }

    override fun ComplexBlock.getMemorySize(): ComplexFunction {
        error("Not supported")
    }

    override fun ComplexBlock.growMemory(): ComplexFunction {
        error("Not supported")
    }

    override fun ComplexBlock.keepOnlyMemoryPtr(): ComplexFunction {
        error("Not supported")
    }

    override fun build(): KsplangProgram = builder.build()
}

class SingleModuleWasmBuilder(
    private val module: InstantiatedKsplangWasmModule,
    private val builder: KsplangProgramBuilder,
    private val body: ComplexBlock,
    val indices: SingleMemoryRuntimeIndexes,
) : WasmBuilder {
    private var bodyCalled = false

    override fun getExportedFunction(name: String): ProgramFunctionBase? = module.getExportedFunction(builder, name)

    override fun body(block: ComplexBlock.() -> Unit) {
        check(!bodyCalled) { "Body can only be set once" }
        bodyCalled = true

        block(body)
    }

    override fun ComplexBlock.getInputSize() = complexFunction("inputSize") {
        push(indices.inputLenIndex)
        yoink()
    }

    override fun ComplexBlock.getFunctionAddress(): ComplexFunction = complexFunction("getFunctionAddress") {
        // i
        push(indices.funTableStartIndex)
        add()
        yoink()
    }

    private fun ComplexBlock.toInputIndex() {
        // input starts at memStartIndex + memSize * 65536
        // i
        push(indices.memSizeIndex)
        yoink()
        // i mem_size
        push(65536)
        mul()
        // i mem_size*65536

        push(indices.memDataStartIndex)
        // i mem_size*65536 mem_start
        add()
        // i mem_start+mem_size*65536
        add()
        // i+mem_start+mem_size*65536
    }

    /**
     * Signature: ```i -> input[i]```
     */
    override fun ComplexBlock.yoinkInput() = complexFunction("yoinkInput") {
        // i
        toInputIndex()
        // i+mem_start+mem_size*65536
        yoink()
    }

    override fun ComplexBlock.yeetInput(): ComplexFunction = complexFunction("yeetInput") {
        // value index
        toInputIndex()
        // value real_index
        yeet()
    }

    /**
     * Signature: ``` -> input[k]```
     */
    override fun ComplexBlock.yoinkInput(index: Int) = complexFunction("yoinkInput($index)") {
        // input starts at memDataStartIndex + memSize * 65536

        push(indices.memSizeIndex)
        yoink()
        // mem_size
        push(65536)
        mul()
        // mem_size*65536

        push(indices.memDataStartIndex + index)
        // mem_size*65536 mem_start+k
        add()
        // k+mem_start+mem_size*65536
        yoink()
    }

    /**
     * Signature: ```-> globals[index]```
     */
    override fun ComplexBlock.yoinkGlobal(index: Int): ComplexFunction = complexFunction("getGlobal($index)") {
        push(indices.globalsStartIndex + index)
        yoink()
    }

    /**
     * Signature : ```v -> globals[index] = v```
     */
    override fun ComplexBlock.yeetGlobal(index: Int): ComplexFunction = complexFunction("yeetGlobal($index)") {
        // v
        push(indices.globalsStartIndex + index)
        // v i
        yeet()
    }

    /**
     * Signature: ```i -> memory[i]```
     */
    override fun ComplexBlock.yoinkMemory(): ComplexFunction = complexFunction("yoinkMemory") {
        // Important note: we do no bounds checking here, that goes against the WASM spec, but it would be very slow.
        // it would also require us to keep track of current data len (page count is not enough) - one extra value

        // i
        push(indices.memDataStartIndex)
        // i mem_start
        add()
        // mem_start+i
        yoink()
    }

    /**
     * Signature: ``` -> memory[index]```
     */
    override fun ComplexBlock.yoinkMemory(index: Int): ComplexFunction = complexFunction("yoinkMemory($index)") {
        push(indices.memDataStartIndex + index)
        yoink()
    }

    /** Signature: ```v i -> memory[i] = v``` */
    override fun ComplexBlock.yeetMemory(): ComplexFunction = complexFunction("yeetMemory") {
        // val i
        push(indices.memDataStartIndex)
        add()
        // val mem_start+i
        yeet()
    }

    override fun ComplexBlock.getMemorySize(): ComplexFunction = complexFunction("getMemorySize"){
        push(indices.memSizeIndex)
        yoink()
    }

    override fun ComplexBlock.growMemory(): ComplexFunction = complexFunction("growMemory") {
        // If we would hit memory max size, we must fail (return -1), otherwise return old size

        // pages
        dup()
        sgn()
        // pages sgn(pages)
        inc()
        ifZero(popChecked = true) {
            // negative page count, we fail (return -1)
            // pages
            push(-1)
            pop2()
        } otherwise {
            // pages
            ifZero {
                // 0
                pushOn(0, indices.memSizeIndex)
                yoink()
                // 0 oldSize
                pop2()
                // oldSize
            } otherwise {
                // pages
                push(indices.memSizeIndex)
                yoink()
                // pages oldSize
                push(indices.memMaxSizeIndex)
                yoink()
                // pages oldSize maxSize
                dupThird(); dupThird()
                // pages oldSize maxSize pages oldSize
                add()
                // pages oldSize maxSize newSize
                sub()
                // pages oldSize maxSize-newSize
                sgn()
                // pages oldSize sgn(maxSize-newSize)
                //               ^ positive if there is leftover space
                //                 0 if we hit the limit perfectly
                //                 negative if we exceed the limit
                inc()
                ifZero(popChecked = true) {
                    // does not fit, return -1
                    // pages oldSize
                    push(-1)
                    pop2()
                    pop2()
                    // -1
                } otherwise {
                    // pages oldSize
                    dupAb(); add()
                    // pages oldSize newSize
                    push(indices.memSizeIndex)
                    yeet()
                    // pages oldSize ; mem_size updated
                    swap2()
                    dup()
                    // oldSize pages pages
                    doWhileNonZero {
                        // oldSize pages remainingPages
                        pushManyZeroes(65536)
                        // oldSize pages remainingPages [65536 * 0]
                        roll(65536 + 3, -3)
                        // [65536 * 0] oldSize pages remainingPages
                        dec()
                        // [65536 * 0] oldSize pages remainingPages-1
                        CS()
                    }
                    // [65536*pages * 0] oldSize pages 0
                    pop()
                    // now we need to move into the correct position, for that we unfortunately need stacklen;
                    // thankfully we can skip most of it because we have a good lower bound:
                    //   mem_data_start + newSize*65536 + input_len
                    // [65536*pages * 0] oldSize pages
                    dupAb()
                    add()
                    // [65536*pages * 0] oldSize pages oldSize+pages
                    // [65536*pages * 0] oldSize pages newSize
                    mul(65536) // cannot overflow due to 32bit mem address limit
                    // [65536*pages * 0] oldSize pages newSize*65536
                    push(indices.inputLenIndex)
                    yoink()
                    // [65536*pages * 0] oldSize pages newSize*65536 inputLen
                    push(indices.memDataStartIndex)
                    // [65536*pages * 0] oldSize pages newSize*65536 inputLen memStart
                    add()
                    add()
                    // [65536*pages * 0] oldSize pages newSize*65536+inputLen+memStart
                    stacklenWithMin()
                    // [65536*pages * 0] oldSize pages stacklen
                    dupThird()
                    // [65536*pages * 0] oldSize pages stacklen oldSize
                    dupThird()
                    // [65536*pages * 0] oldSize pages stacklen oldSize pages
                    mul(65536)
                    // [65536*pages * 0] oldSize pages stacklen oldSize pages*65536
                    add(4)
                    // [65536*pages * 0] oldSize pages stacklen oldSize pages*65536+4
                    push(1)
                    swap2()
                    // [65536*pages * 0] oldSize pages stacklen oldSize 1 pages*65536+4
                    lroll() // increasing stacklen
                    // oldSize [65536*pages * 0] oldSize pages stacklen-1
                    roll(3, 2)
                    // oldSize [65536*pages * 0] pages stacklen-1 oldSize
                    mul(65536)
                    // oldSize [65536*pages * 0] pages stacklen-1 oldSize*65536
                    push(indices.memDataStartIndex)
                    add()
                    // oldSize [65536*pages * 0] pages stacklen-1 oldSize*65536+memStart
                    sub() // reducing stacklen
                    // oldSize [65536*pages * 0] pages stacklen-oldSize*65536+memStart
                    swap2()
                    // oldSize [65536*pages * 0] stacklen-oldSize*65536+memStart pages
                    mul(65536)
                    swap2()
                    // oldSize [65536*pages * 0] pages*65536 stacklen-oldSize*65536+memStart
                    lroll() // newly allocated memory moved to its place
                    // oldSize
                }
            }
        }
    }

    override fun ComplexBlock.keepOnlyMemoryPtr(): ComplexFunction = complexFunction("keepOnlyMemoryPtr") {
        // ptr
        push(indices.inputLenIndex)
        yoink()
        // ptr inputLen
        push(indices.memDataStartIndex)
        // ptr inputLen memStart
        push(indices.memSizeIndex)
        yoink()
        mul(65536)
        // ptr inputLen memStart memSize*65536
        add()
        add()
        // ptr inputLen+memStart+memSize*65536
        stacklenWithMin()
        // [stack] ptr stacklen+1
        dec()
        swap2()
        // [stack] stacklen ptr
        push(indices.memDataStartIndex); add()
        // [stack] stacklen ptr_real
        inc()
        // [stack] stacklen ptr_real+1
        // [stack] stacklen start

        // Prepare data for access after roll. Thankfully, len is already where it needs to be (new top)
        dup()
        // [stack] stacklen start start
        add(-2)
        // [stack] stacklen start start-2
        dupThird()
        // [stack] stacklen start start-2 stacklen
        swap2()
        yeet()
        // [stack] stacklen start
        //   s[start-1] = len
        //   s[start-2] = stacklen
        // [stack] stacklen start
        negate(); swap2()
        // [stack] -start stacklen
        lroll() // roll(stacklen, -start)
        // [output] [stack] old_stacklen len
        // [output] [stack] remove_len+2+len len
        sub()
        // [output] [stack] remove_len+2
        add(-2)
        // [output] [stack] remove_len
        popMany()
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
    val memories = instance.memory()?.let {
        // Only include the memory if it's actually used
        if (isMemoryUsed) {
            logger.debug { "WASM memory found and it is used. "}
            listOf(instance.memory())
        } else {
            logger.debug { "WASM memory is unused, ignoring it. "}
            emptyList<Memory>()
        }
    } ?: emptyList<Memory>()
    val tables = instance.getTables()
    val funTable: List<ProgramFunctionBase?> = if (tables.isEmpty()) {
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
    val memDataStartIndex: Int,
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
        memDataStartIndex = 4 + runtimeData.globals.size + runtimeData.funTable.size,
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

