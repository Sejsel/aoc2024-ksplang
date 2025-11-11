package cz.sejsel.ksplang.annotools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.defaultStdin
import com.github.ajalt.clikt.parameters.types.inputStream
import com.github.ajalt.clikt.parameters.types.path
import cz.sejsel.ksplang.builder.AnnotatedKsplangSegment
import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import cz.sejsel.ksplang.builder.BlockType
import cz.sejsel.ksplang.dsl.core.Instruction
import cz.sejsel.ksplang.interpreter.Op
import cz.sejsel.ksplang.interpreter.PiDigits
import cz.sejsel.ksplang.interpreter.State
import cz.sejsel.ksplang.interpreter.VMOptions
import cz.sejsel.ksplang.interpreter.parseProgram
import cz.sejsel.ksplang.interpreter.parseWord
import kotlinx.serialization.json.Json
import kotlin.collections.forEach
import kotlin.io.path.readText

class CallstackCommand : CliktCommand(name = "callstack") {/*
    private val index by argument(
        name = "index",
        help = "Index of the instruction to get call stack for"
    ).convert { it.toInt() }
     */

    private val instructionCount by option(
        "--count", help = "Instruction count to run the program up to"
    ).convert { it.toLong() }.default(Long.MAX_VALUE)

    private val inputStack by option(
        "--stack", help = "Initial stack values"
    ).required()

    private val textMode by option(
        "--text", help = "Whether to interpret the input stack as text (UTF-8 encoded string)"
    ).flag(default = false)

    private val printStack by option(
        "--print-stack", help = "Whether to print the stack at the end"
    ).flag(default = false)

    private val inputFile by option(
        "--ksplang", help = "Input JSON file containing an AnnotatedKsplangTree"
    ).path(mustExist = true, canBeDir = false, mustBeReadable = true).required()

    private val maxStackSize by option(
        "-m", "--max-stack-size", help = "Maximum stack size (amount of int64 elements on the program stack)"
    ).convert { it.toInt() }.default(2147483647)

    override fun run() {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val jsonContent = inputFile.readText()
        val tree = json.decodeFromString<AnnotatedKsplangTree>(jsonContent)

        val ops = extractOps(tree).map { parseWord(it) }

        val inputStack = if (textMode) {
            val str = inputStack
            str.map { it.code.toLong() }
        } else {
            inputStack.split("\\s+".toRegex()).map { it.trim().toLong() }
        }

        echo("Starting run, op count = ${ops.size}")

        val callstack = mutableListOf<Long>()

        val state = State(
            ops = ops,
            initialStack = inputStack,
            maxStackSize = maxStackSize.toLong(),
            piDigits = PiDigits.digits,
            maxOpCount = Long.MAX_VALUE
        )

        var prevOp: Op? = null
        var prevIp: Int? = null

        val parentCache = mutableMapOf<Int, AnnotatedKsplangTree?>()

        while (true) {
            val counter = state.operationsRun()
            if (counter == instructionCount) {
                break
            }

            if (prevOp == Op.Call) {
                val parent = parentCache.getOrPut(prevIp!!) { getParent(tree = tree, targetIndex = prevIp) }
                //echo("Call detected at IP=${state.getCurrentIp()}, parent = $parent")
                if (parent is AnnotatedKsplangTree.Block && (parent.type == BlockType.FunctionCall || parent.type == BlockType.InlinedFunctionCall)) {
                    // Function call
                    echo("$counter Entering call to IP=${state.getCurrentIp()} (name = ${parent.name})")
                    callstack.add(state.getCurrentIp().toLong())
                }
            } else if (prevOp == Op.Goto) {
                //echo("Goto detected at IP=${state.getCurrentIp()}")
                val parent = parentCache.getOrPut(prevIp!!) { getParent(tree = tree, targetIndex = prevIp) }
                if (parent is AnnotatedKsplangTree.Block && parent.name?.startsWith("fun_wrapper_") ?: false) {
                    // Function return
                    echo("$counter Returning from call to IP=${state.getCurrentIp()} (name = ${parent.name})")
                    if (callstack.isNotEmpty()) {
                        callstack.removeAt(callstack.size - 1)
                    }
                }
            }

            if (state.getCurrentIp() in ops.indices) {
                prevOp = ops[state.getCurrentIp()]
                prevIp = state.getCurrentIp()
            } else {
                echo("$counter Looks like we are ending")
            }

            val result = state.runNextOp()
            result.fold({
                echo("Counter = $counter, IP = ${state.getCurrentIp()}, error: $it")
                break
            }, { end ->
                if (end) {
                    echo("End")
                    break
                }
            })
        }
        //echo("Finished, IP=${state.getCurrentIp()}, current stack ${state.getStack()}")
        echo("Finished, IP=${state.getCurrentIp()}, instruction count = ${state.operationsRun()}")
        if (printStack) {
            echo("STACK")
            state.getStack().forEach {
                echo("$it")
            }
        }
    }

    private fun getParent(
        tree: AnnotatedKsplangTree,
        targetIndex: Int
    ): AnnotatedKsplangTree? {
        var currentIndex = 0

        fun traverse(node: AnnotatedKsplangTree, parent: AnnotatedKsplangTree?): AnnotatedKsplangTree? {
            when (node) {
                is AnnotatedKsplangTree.Op -> {
                    if (currentIndex == targetIndex) {
                        return parent
                    }
                    currentIndex++
                    return null
                }
                is AnnotatedKsplangTree.Root -> {
                    for (child in node.children) {
                        val result = traverse(child, node)
                        if (result != null) return result
                    }
                    return null
                }
                is AnnotatedKsplangTree.Block -> {
                    for (child in node.children) {
                        val result = traverse(child, node)
                        if (result != null) return result
                    }
                    return null
                }
            }
        }

        return traverse(tree, null)
    }

    private fun extractOps(
        tree: AnnotatedKsplangTree,
    ): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        fun traverse(node: AnnotatedKsplangTree) {
            when (node) {
                is AnnotatedKsplangTree.Op -> {
                    list.add(node.instruction)
                }

                is AnnotatedKsplangTree.Root -> {
                    for (child in node.children) {
                        traverse(child)
                    }
                }

                is AnnotatedKsplangTree.Block -> {
                    for (child in node.children) {
                        traverse(child)
                    }
                }
            }
        }

        traverse(tree)

        return list
    }
}