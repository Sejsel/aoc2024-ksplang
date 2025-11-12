package cz.sejsel.ksplang.annotools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import kotlinx.serialization.json.Json
import kotlin.collections.forEach
import kotlin.io.path.readText

class BrowseCommand : CliktCommand(name = "browse") {
    private val inputFile by option(
        "--ksplang", help = "Input JSON file containing an AnnotatedKsplangTree"
    ).path(mustExist = true, canBeDir = false, mustBeReadable = true).required()

    private val listFunctions by option(
        "--functions", help = "List top-level functions"
    ).flag(default = false)

    private val listChildren by option(
        "--list-children", help = "List children of block name (a.b.c)"
    )

    override fun run() {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val jsonContent = inputFile.readText()
        val tree = json.decodeFromString<AnnotatedKsplangTree>(jsonContent) as AnnotatedKsplangTree.Root

        if (listFunctions) {
            echo("Functions:")
            tree.children.forEach { child ->
                when (child) {
                    is AnnotatedKsplangTree.Block -> {
                        val name = child.name ?: "<unnamed>"
                        if (name.startsWith("fun_wrapper_")) {
                            echo("\tBlock: $name (${child.type})")
                        }
                    }

                    is AnnotatedKsplangTree.Op -> {}

                    is AnnotatedKsplangTree.Root -> error("root as child")
                }
            }
        }

        listChildren?.let { path ->
            val pathParts = path.split(".")
            val blocks = mutableListOf<AnnotatedKsplangTree>()
            var currentNode: AnnotatedKsplangTree = tree
            if (path == ".") {
                // Nothing, use root
            } else {
                val indexRegex = """\[(\d+)]""".toRegex()
                for (part in pathParts) {
                    val indexMatch = indexRegex.matchEntire(part)
                    val nextNode = if (indexMatch != null) {
                        val index = indexMatch.groupValues[1].toInt()
                        findDirectChildByIndex(currentNode, index)
                    } else {
                        findDirectChildByName(currentNode, part)
                    }

                    if (nextNode == null) {
                        echo(
                            "Block '$part' not found under '${(currentNode as? AnnotatedKsplangTree.Block)?.name ?: "root"}'",
                            err = true
                        )
                        return@let
                    } else {
                        blocks.add(nextNode)
                        currentNode = nextNode
                    }
                }
            }

            echo("Children of block '$path':")
            val children = currentNode.childrenWithInstructionIndices(tree)
            children.forEachIndexed { index, pair ->
                val (child, instrIndex) = pair
                val instr = instrIndex?.toString() ?: "empty"
                when (child) {
                    is AnnotatedKsplangTree.Block -> {
                        val name = child.name ?: "<unnamed>"
                        echo("\t[$index] $instr Block: $name (${child.type})")
                    }

                    is AnnotatedKsplangTree.Op -> {
                        echo("\t[$index] $instr Op: ${child.instruction}")
                    }

                    is AnnotatedKsplangTree.Root -> error("root as child")
                }
            }
        }
    }

    private fun findDirectChildByName(
        node: AnnotatedKsplangTree,
        name: String
    ): AnnotatedKsplangTree? {
        if (node is AnnotatedKsplangTree.Block) {
            node.children.forEach { child ->
                if (child is AnnotatedKsplangTree.Block && child.name == name) {
                    return child
                }
            }
        } else if (node is AnnotatedKsplangTree.Root) {
            node.children.forEach { child ->
                if (child is AnnotatedKsplangTree.Block && child.name == name) {
                    return child
                }
            }
        }
        return null
    }

    private fun findDirectChildByIndex(
        node: AnnotatedKsplangTree,
        index: Int
    ): AnnotatedKsplangTree? {
        val children = when (node) {
            is AnnotatedKsplangTree.Op -> return null
            is AnnotatedKsplangTree.Root -> node.children
            is AnnotatedKsplangTree.Block -> node.children
        }
        return children[index]
    }

    // In one pass, assign each block the index of the first Op it contains (potentially nested in more blocks)
    private fun AnnotatedKsplangTree.childrenWithInstructionIndices(
        root: AnnotatedKsplangTree,
    ): List<Pair<AnnotatedKsplangTree, Int?>> {
        val result = mutableListOf<Pair<AnnotatedKsplangTree, Int?>>()
        val directParent = this

        var currentIndex = 0
        fun traverse(node: AnnotatedKsplangTree, parent: AnnotatedKsplangTree?) {
            when (node) {
                is AnnotatedKsplangTree.Op -> {
                    currentIndex++
                    if (parent == directParent) {
                        result.add(Pair(node, currentIndex))
                    }
                }
                is AnnotatedKsplangTree.Root -> {
                    for (child in node.children) traverse(child, node)
                }
                is AnnotatedKsplangTree.Block -> {
                    val potentialStartIndex = currentIndex
                    for (child in node.children) {
                        traverse(child, node)
                    }
                    if (parent == directParent) {
                        if (currentIndex >= potentialStartIndex) {
                            // there is a child with an Op inside this block
                            result.add(Pair(node, potentialStartIndex))
                        } else {
                            result.add(Pair(node, null))
                        }
                    }
                }
            }
        }

        traverse(root, null)

        return result
    }
}