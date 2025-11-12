package cz.sejsel.ksplang.annotools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.types.path
import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import kotlinx.serialization.json.Json
import kotlin.collections.forEach
import kotlin.io.path.readText

class LocateCommand : CliktCommand(name = "locate") {
    private val index by argument(
        name = "index",
        help = "Index of the instruction to locate"
    ).convert { it.toInt() }

    private val inputFile by argument(
        name = "input",
        help = "Input JSON file containing an AnnotatedKsplangTree"
    ).path(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val jsonContent = inputFile.readText()
        val tree = json.decodeFromString<AnnotatedKsplangTree>(jsonContent)

        val result = findInstructionByIndex(tree, index)

        if (result == null) {
            echo("Instruction at index $index not found", err = true)
            return
        }

        echo("Instruction at index $index: ${result.instruction.instruction}")
        echo("Parent path (root to instruction):")
        result.parents.forEach { parent ->
            when (parent) {
                is AnnotatedKsplangTree.Root -> echo("  Root")
                is AnnotatedKsplangTree.Block -> {
                    val name = parent.name ?: "<unnamed>"
                    echo("  Block: $name (${parent.type})")
                }
                is AnnotatedKsplangTree.Op -> echo("  Op: ${parent.instruction}")
            }
        }
    }

    private data class InstructionLocation(
        val instruction: AnnotatedKsplangTree.Op,
        val parents: List<AnnotatedKsplangTree>
    )

    private fun findInstructionByIndex(
        tree: AnnotatedKsplangTree,
        targetIndex: Int,
        parents: List<AnnotatedKsplangTree> = emptyList()
    ): InstructionLocation? {
        var currentIndex = 0

        fun traverse(node: AnnotatedKsplangTree, nodeParents: List<AnnotatedKsplangTree>): InstructionLocation? {
            when (node) {
                is AnnotatedKsplangTree.Op -> {
                    if (currentIndex == targetIndex) {
                        return InstructionLocation(node, nodeParents)
                    }
                    currentIndex++
                    return null
                }
                is AnnotatedKsplangTree.Root -> {
                    for (child in node.children) {
                        val result = traverse(child, nodeParents + node)
                        if (result != null) return result
                    }
                    return null
                }
                is AnnotatedKsplangTree.Block -> {
                    for (child in node.children) {
                        val result = traverse(child, nodeParents + node)
                        if (result != null) return result
                    }
                    return null
                }
            }
        }

        return traverse(tree, parents)
    }
}