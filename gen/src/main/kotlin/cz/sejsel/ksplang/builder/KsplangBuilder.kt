package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.Instruction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.IfZero
import cz.sejsel.ksplang.dsl.core.*
import cz.sejsel.ksplang.std.PaddingFailureException
import cz.sejsel.ksplang.std.roll
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.pushOn
import cz.sejsel.ksplang.std.pushPaddedTo

data class RegisteredFunction(
    val function: SimpleFunction,
    val nParams: Int,
    val nOut: Int,
    var index: Int? = null
) {
    val hash = function.getInstructions().joinToString(" ") { it.toString() }.hashCode()
    val name = function.name ?: "anonymous_$hash"

    fun toCallable(): SimpleFunction = buildFunction("callable_$name") {
        pop2()
        roll(1L + nParams, 1)
        +function
        roll(1L + nOut, nOut.toLong())
        goto()
    }

    fun toCall(): SimpleFunction = buildFunction("call_$name") {
        push(index!!)
        call()
        pop()
    }
}


data class PreparedPush(
    /** Index of first instruction in the push. */
    val index: Int,
    /** Index of the replaced segment in the program. */
    val programIndex: Int,
    val setter: (PreparedPush, Long) -> Unit,
    val padding: Int,
    var invalidated: Boolean = false,
) {
    fun set(n: Int) = set(n.toLong())

    fun set(n: Long) {
        if (invalidated) {
            throw IllegalStateException("This push was already set")
        }
        setter(this, n)
        invalidated = true
    }

    fun setForJump(n: Int) {
        set((n - indexAfter() - 1))
    }

    fun indexEnd(): Int {
        return index + padding - 1
    }

    fun indexAfter(): Int {
        return index + padding
    }
}

private class BuilderState {
    var index = 0
    // This is a list of lists to support replacing subparts of different lengths (prepared pushes...)
    var program = mutableListOf<List<AnnotatedKsplangSegment>>()
    var funCounter = 0
    var lastSimpleFunction: SimpleFunction? = null
    var earlyExitPushes = mutableListOf<PreparedPush>()
    var preparedPushes = mutableListOf<PreparedPush>()
    var functionStates = mutableMapOf<String, FunctionState>()

    fun getFunctionState(name: String): FunctionState {
        return functionStates.getOrPut(name) { FunctionState() }
    }

    fun getEmittedFunctionId() = funCounter++

    fun deepCopy(): BuilderState {
        val copy = BuilderState()
        copy.index = index
        copy.program = program.toMutableList()
        copy.lastSimpleFunction = lastSimpleFunction
        copy.earlyExitPushes = earlyExitPushes.toMutableList()
        copy.preparedPushes = preparedPushes.toMutableList()
        copy.functionStates = functionStates.mapValues { it.value.clone() }.toMutableMap()
        return copy
    }
}

private class FunctionState {
    var callIndex: Int? = null
        set(value) {
            require(field == null) { "Cannot set index twice" }
            field = value
        }

    val pendingCalls = mutableListOf<PreparedPush>()

    fun clone(): FunctionState {
        val clone = FunctionState()
        callIndex?.let { clone.callIndex = it }
        clone.pendingCalls.addAll(pendingCalls.map { it.copy() })
        return clone
    }
}

sealed interface AnnotatedKsplangSegment {
    /** A ksplang operation. */
    data class Op(val instruction: Instruction) : AnnotatedKsplangSegment
    /** A marker showing the start of a simple/complex function, match with corresponding [FuncEnd] using the [id]. */
    data class FuncStart(val name: String?, val id: Int) : AnnotatedKsplangSegment
    /** A marker showing the end of a simple/complex function, match with corresponding [FuncStart] using the [id]. */
    data class FuncEnd(val id: Int) : AnnotatedKsplangSegment
}

class Ksplang(val segments: List<AnnotatedKsplangSegment>) {
    fun toRunnableProgram(): String {
        val sb = StringBuilder()
        var depth = 0
        var isLineStarted = false
        for (segment in segments) {
            when (segment) {
                is AnnotatedKsplangSegment.Op -> {
                    if (!isLineStarted) {
                        sb.append("\n", " ".repeat(depth))
                        isLineStarted = true
                    } else {
                        sb.append(" ")
                    }
                    sb.append(segment.instruction.text)
                }
                is AnnotatedKsplangSegment.FuncStart -> {
                    depth += 1
                    isLineStarted = false
                }
                is AnnotatedKsplangSegment.FuncEnd -> {
                    depth -= 1
                    isLineStarted = false
                }
            }
        }

        return sb.toString().trimIndent()
    }
}


/** Transforms the ksplang DSL tree consisting of [Instruction], [SimpleFunction], and [ComplexFunction]
 * into real ksplang code. */
class KsplangBuilder(
    val enablePushOptimizations: Boolean = true
) {
    private val registeredFunctions = mutableListOf<RegisteredFunction>()

    fun registerFunction(function: SimpleFunction, nParams: Int, nOut: Int) {
        require(registeredFunctions.none { it.name == function.name }) {
            "Function ${function.name} is already registered"
        }
        registeredFunctions.add(RegisteredFunction(function, nParams, nOut))
        TODO("Currently not wired up")
    }

    fun build(program: KsplangProgram) = buildAnnotated(program).toRunnableProgram()
    fun build(programTree: Block) = buildAnnotated(programTree).toRunnableProgram()

    fun buildAnnotated(program: KsplangProgram): Ksplang {
        return buildAnnotated(program.body, program.definedFunctions)
    }

    fun buildAnnotated(programTree: Block): Ksplang = when (programTree) {
        is ComplexBlock -> buildAnnotated(programTree, functions = emptyList())
        is SimpleFunction -> buildAnnotated(programTree)
        is Instruction -> buildAnnotated(programTree)
    }

    private fun buildAnnotated(programTree: ComplexBlock, functions: List<ProgramFunctionBase>): Ksplang {
        require(functions.map { it.name }.distinct().size == functions.size) { "All functions must have unique names." }

        // For simplification, we use a global address padding (all addresses are padded to the same length)
        for (addressPad in 6..Int.MAX_VALUE) {
            try {
                val state = BuilderState()
                val pushNameRegex = """^push\((-?\d+)\)$""".toRegex()

                fun restoreState(backup: BuilderState) {
                    state.index = backup.index
                    state.program = backup.program.toMutableList()
                    state.lastSimpleFunction = backup.lastSimpleFunction
                    state.earlyExitPushes = backup.earlyExitPushes.toMutableList()
                    state.preparedPushes = backup.preparedPushes.toMutableList()
                    state.functionStates = backup.functionStates.mapValues { it.value.clone() }.toMutableMap()
                }

                fun applyPreparedPush(push: PreparedPush, n: Long) {
                    val paddedPush = extract { pushPaddedTo(n, push.padding) }
                    check(state.program[push.programIndex].isEmpty()) { "Prepared push applied twice or program index is broken" }
                    state.program[push.programIndex] = buildList {
                        val funcEmitId = state.getEmittedFunctionId()
                        add(AnnotatedKsplangSegment.FuncStart(paddedPush.name, funcEmitId))
                        addAll(paddedPush.getInstructions().map { AnnotatedKsplangSegment.Op(it) })
                        add(AnnotatedKsplangSegment.FuncEnd(funcEmitId))
                    }
                }

                fun preparePaddedPush(padding: Int? = null): PreparedPush {
                    val padding = padding ?: addressPad
                    val programIndex = state.program.size
                    val push = PreparedPush(state.index, programIndex, ::applyPreparedPush, padding)
                    state.program.add(emptyList())
                    state.index += padding
                    state.preparedPushes.add(push)
                    return push
                }

                fun expand(
                    block: Block,
                    isLast: Boolean = false,
                    useCalls: Boolean = true
                ) {
                    // A shorthand to expand recursively with correct params
                    fun e(block: Block) {
                        expand(block, isLast, useCalls)
                    }

                    when (block) {
                        is Instruction -> {
                            state.lastSimpleFunction = null
                            state.index++
                            state.program.add(listOf(AnnotatedKsplangSegment.Op(block)))
                        }

                        is SimpleFunction -> {
                            var optimized = false
                            val funcEmitId = state.getEmittedFunctionId()
                            state.program.add(listOf(AnnotatedKsplangSegment.FuncStart(block.name, funcEmitId)))

                            if (enablePushOptimizations && state.lastSimpleFunction != null) {
                                val lastMatch = pushNameRegex.find(state.lastSimpleFunction!!.name ?: "")
                                val thisMatch = pushNameRegex.find(block.name ?: "")
                                if (lastMatch != null && thisMatch != null) {
                                    val lastN = lastMatch.groupValues[1].toLong()
                                    val thisN = thisMatch.groupValues[1].toLong()
                                    // We must not optimize again in case push_on falls back onto push(n)
                                    // That would result in an infinite loop.
                                    state.lastSimpleFunction = null
                                    e(extract { pushOn(lastN, thisN) })
                                    optimized = true
                                }
                                // else if (thisMatch != null && state.lastSimpleFunction == sgn) {
                                //     val thisN = thisMatch.groupValues[1].toLong()
                                //     e(extract { pushOnSgn(thisN, isLast) })
                                //     state.lastSimpleFunction = null
                                //     optimized = true
                                // }
                            }

                            if (!optimized) {
                                if (useCalls) {
                                    val registered =
                                        registeredFunctions.find { it.name == block.name && it.index != null }
                                    if (registered != null) {
                                        TODO()
                                        e(registered.toCall())
                                    } else {
                                        block.children.forEach { e(it) }
                                    }
                                } else {
                                    block.children.forEach { e(it) }
                                }
                            }
                            state.lastSimpleFunction = block
                            state.program.add(listOf(AnnotatedKsplangSegment.FuncEnd(funcEmitId)))
                        }

                        is ComplexFunction -> {
                            val funcEmitId = state.getEmittedFunctionId()
                            state.program.add(listOf(AnnotatedKsplangSegment.FuncStart(block.name, funcEmitId)))
                            // It may be called complex, but it is so simple, oh so simple:
                            for (child in block.children) {
                                e(child)
                            }
                            state.program.add(listOf(AnnotatedKsplangSegment.FuncEnd(funcEmitId)))
                        }

                        is IfZero -> {
                            if (block.orElse == null) {
                                // We can specialize
                                throw NotImplementedError()
                            }

                            val thenPush = preparePaddedPush()
                            e(extract { roll(2, 1) })
                            e(brz)
                            e(pop2)
                            val otherwiseJPush = preparePaddedPush()
                            val otherwiseJIndex = state.index
                            e(j)
                            thenPush.set(state.index)
                            e(pop2)
                            if (block.popChecked) {
                                e(pop)
                            }
                            for (b in block.children) {
                                e(b)
                            }
                            val endJPush = preparePaddedPush()
                            val endJIndex = state.index
                            e(j)
                            e(pop)
                            otherwiseJPush.set(state.index - otherwiseJIndex - 2)
                            if (block.popChecked) {
                                e(pop)
                            }
                            for (b in block.orElse!!.children) {
                                e(b)
                            }
                            e(CS)
                            e(pop)
                            endJPush.set(state.index - endJIndex - 2)
                        }

                        is DoWhileZero -> {
                            e(CS)
                            e(CS)
                            val redoIndex = state.index
                            e(pop)
                            e(pop)
                            for (b in block.children) {
                                e(b)
                            }
                            e(extract { push(redoIndex) })
                            e(extract { roll(2, 1) })
                            e(brz)
                            e(pop)
                            e(pop)
                        }

                        is WhileNonZero -> {
                            // x
                            e(CS)
                            val redoIndex = state.index
                            e(pop)
                            val endPush = preparePaddedPush()
                            e(extract { roll(2, 1) })
                            e(brz)
                            e(pop2)
                            for (b in block.children) {
                                e(b)
                            }
                            e(extract { push(redoIndex) })
                            e(goto)
                            endPush.set(state.index)
                            e(pop)
                            e(pop)
                        }

                        is FunctionCall -> {
                            val functionState = state.getFunctionState(block.calledFunction.name)
                            // Because of function calls in functions (recursion, or calling functions not defined yet),
                            // we need to prepare this push instead of eagerly expanding it.
                            val callPush = preparePaddedPush()
                            functionState.pendingCalls.add(callPush)
                            e(call)
                            e(pop)
                        }
                    }
                }

                // 16 is a really nice number to align to, as it can be created in very few instructions
                // call cost of num is len(short_pushes[num].split())
                // call cost 11: 16 24 27
                // call cost 12: 17 25 28 32 48 64 77 256 512 2048 65536 16777216 7625597484988
                // call cost 13: 15 18 20 26 29 33 49 65 71 78 96 112 128 160 225 257 384 513 1536 2049 3125 4096 4608
                // call cost 14: 14 19 21 30 34 39 42 50 66 72 79 97 109 113 129 154 161 168 175 192 218 226 258 320 385 450 514 768 896 1024 1537 2050 3072 3126 3200 4097 4352 4609
                if (functions.isNotEmpty()) {
                    // We have a separate "padding" here, just for the first jump. If the expansion of the inner
                    // function fails, it must not be caught here, we need to increase the global padding in that case.
                    for (firstFunStart in 16 until Int.MAX_VALUE) {
                        val backup = state.deepCopy()
                        // Initial jump past the callable functions
                        val afterPush = preparePaddedPush(firstFunStart - 1)
                        expand(j)
                        // Callable functions
                        for (function in functions) {
                            val functionState = state.getFunctionState(function.name)
                            val fIndex = state.index
                            val wrapper = buildComplexFunction("fun_wrapper_${function.name}") {
                                // Remove address used for the jump (it is not consumed by call)
                                pop2()
                                // Move the return address below the arguments
                                roll(1L + function.args, 1)
                                +function.body!!
                                // Move the return address back to the top
                                roll(1L + function.outputs, function.outputs.toLong())
                                goto()
                            }
                            expand(wrapper)
                            functionState.callIndex = fIndex
                        }
                        try {
                            afterPush.setForJump(state.index)
                        } catch (_: PaddingFailureException) {
                            restoreState(backup)
                            continue
                        }
                        expand(pop)
                        break
                    }
                }

                check(state.functionStates.size == functions.size) { "Not all functions were expanded, expected ${functions.size}, got ${state.functionStates.size}" }
                check(state.functionStates.all { it.value.callIndex != null }) { "Not all functions have a call index set, some functions may not have been expanded properly." }

                programTree.children.forEachIndexed { i, block ->
                    val isLast = i == programTree.children.size - 1
                    expand(block, isLast)
                }

                // We can now expand all prepared function calls
                for (function in state.functionStates) {
                    val functionState = function.value
                    functionState.pendingCalls.forEach {
                        it.set(functionState.callIndex!!)
                    }
                }

                if (state.earlyExitPushes.isNotEmpty()) {
                    TODO()
                }

                return Ksplang(state.program.flatten())
            } catch (e: PaddingFailureException) {
                // Try again with a different address padding
                registeredFunctions.forEach {
                    it.index = null
                }
            }
        }

        throw IllegalStateException("Could not find a suitable address padding")
    }

    private fun buildAnnotated(instructions: SimpleFunction): Ksplang {
        return buildAnnotated(instructions.getInstructions())
    }

    fun build(instructions: List<Instruction>): String = buildAnnotated(instructions).toRunnableProgram()

    fun buildAnnotated(instructions: List<Instruction>): Ksplang {
        return Ksplang(instructions.map { AnnotatedKsplangSegment.Op(it) })
    }
}