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
    val index: Int,
    val setter: (PreparedPush, Long, Int) -> Unit,
    val padding: Int,
    var invalidated: Boolean = false,
) {
    val placeholder: String = "[PREPARED-PUSH-$index]"

    fun set(n: Int) = set(n.toLong())

    fun set(n: Long) {
        if (invalidated) {
            throw IllegalStateException("This push was already set")
        }
        setter(this, n, padding)
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
    var program = mutableListOf<String>()
    var lastDepth = 0
    var lastSimpleFunction: SimpleFunction? = null
    var earlyExitPushes = mutableListOf<PreparedPush>()
    var preparedPushes = mutableListOf<PreparedPush>()
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
    }

    fun build(programTree: Block): String = when (programTree) {
        is ComplexBlock -> build(programTree)
        is SimpleFunction -> build(programTree)
        is Instruction -> build(programTree)
    }

    private fun build(programTree: ComplexBlock): String {
        // For simplification, we use a global address padding (all addresses are padded to the same length)
        for (addressPad in 6..Int.MAX_VALUE) {
            try {
                val state = BuilderState()
                val pushNameRegex = """^push\((-?\d+)\)$""".toRegex()

                fun backupState(): BuilderState {
                    return BuilderState().apply {
                        index = state.index
                        program = state.program.toMutableList()
                        lastDepth = state.lastDepth
                        lastSimpleFunction = state.lastSimpleFunction
                        earlyExitPushes = state.earlyExitPushes.toMutableList()
                        preparedPushes = state.preparedPushes.toMutableList()
                    }
                }

                fun restoreState(backup: BuilderState) {
                    state.index = backup.index
                    state.program = backup.program.toMutableList()
                    state.lastDepth = backup.lastDepth
                    state.lastSimpleFunction = backup.lastSimpleFunction
                    state.earlyExitPushes = backup.earlyExitPushes.toMutableList()
                    state.preparedPushes = backup.preparedPushes.toMutableList()
                }

                fun applyPreparedPush(push: PreparedPush, n: Long, padding: Int) {
                    val index = state.program.indexOf(push.placeholder)
                    val paddedPush = extract { pushPaddedTo(n, padding) }
                    state.program[index] = build(paddedPush.getInstructions()) + "\n"
                }

                fun preparePaddedPush(padding: Int? = null): PreparedPush {
                    val padding = padding ?: addressPad
                    val push = PreparedPush(state.index, ::applyPreparedPush, padding)
                    state.program.add(push.placeholder)
                    state.index += padding
                    state.preparedPushes.add(push)
                    return push
                }

                fun expand(
                    block: Block,
                    isLast: Boolean = false,
                    depth: Int = 0,
                    useCalls: Boolean = true
                ) {
                    // A shorthand to expand recursively with correct params
                    fun e(block: Block) {
                        expand(block, isLast, depth + 1, useCalls)
                    }

                    if (depth > state.lastDepth) {
                        while (state.program.isNotEmpty() && state.program.last().isBlank()) {
                            state.program.removeLast()
                        }
                        if (state.program.isNotEmpty() && state.program.last() != "\n") {
                            state.program.add("\n")
                        }
                        state.program.add(" ".repeat(depth))
                    }
                    state.lastDepth = depth

                    when (block) {
                        is Instruction -> {
                            state.lastSimpleFunction = null
                            state.index++
                            state.program.add(block.text)
                            state.program.add(" ")
                        }

                        is SimpleFunction -> {
                            var optimized = false
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
                                    val registered = registeredFunctions.find { it.name == block.name && it.index != null }
                                    if (registered != null) {
                                        e(registered.toCall())
                                    } else {
                                        block.children.forEach { e(it) }
                                    }
                                } else {
                                    block.children.forEach { e(it) }
                                }
                            }
                            state.lastSimpleFunction = block
                        }

                        is ComplexFunction -> {
                            // It may be called complex, but it is so simple, oh so simple:
                            for (child in block.children) {
                                e(child)
                            }
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
                    }
                }

                val callables = registeredFunctions.map { it to it.toCallable() }

                // 16 is a really nice number to align to, as it can be created in very few instructions
                // call cost of num is len(short_pushes[num].split())
                // call cost 11: 16 24 27
                // call cost 12: 17 25 28 32 48 64 77 256 512 2048 65536 16777216 7625597484988
                // call cost 13: 15 18 20 26 29 33 49 65 71 78 96 112 128 160 225 257 384 513 1536 2049 3125 4096 4608
                // call cost 14: 14 19 21 30 34 39 42 50 66 72 79 97 109 113 129 154 161 168 175 192 218 226 258 320 385 450 514 768 896 1024 1537 2050 3072 3126 3200 4097 4352 4609
                if (callables.isNotEmpty()) {
                    for (firstFunStart in 16 until Int.MAX_VALUE) {
                        val backup = backupState()
                        try {
                            // Initial jump past the callable functions
                            val afterPush = preparePaddedPush(firstFunStart - 1)
                            expand(j)
                            // TODO: We may be able to save a few instructions per call by padding to a good value
                            //       should be possible to math it out, we know how many calls we have as well
                            // TODO: We may also be able to save a bit by using goto instead of j there
                            // Callable functions
                            for ((f, c) in callables) {
                                // We use f.index to check if a function is callable within expansion,
                                // so we need to only set it after we are done with expanding this function
                                // or it will expand itself as a call to itself.
                                val fIndex = state.index
                                expand(c)
                                f.index = fIndex
                            }
                            // Landing pop for the initial jump
                            afterPush.setForJump(state.index)
                            expand(pop)
                            break
                        } catch (_: PaddingFailureException) {
                            for ((f, _) in callables) {
                                f.index = null
                            }
                            restoreState(backup)
                        }
                    }
                }


                programTree.children.forEachIndexed { i, block ->
                    val isLast = i == programTree.children.size - 1
                    expand(block, isLast, depth = 0)
                }

                if (state.earlyExitPushes.isNotEmpty()) {
                    TODO()
                }

                return state.program.joinToString("")
            } catch (e: PaddingFailureException) {
                // Try again with a different address padding
                registeredFunctions.forEach {
                    it.index = null
                }
            }
        }

        throw IllegalStateException("Could not find a suitable address padding")
    }

    private fun build(instructions: SimpleFunction): String {
        return build(instructions.getInstructions())
    }

    fun build(instructions: List<Instruction>): String {
        return instructions.joinToString(" ") { it.text }
    }
}