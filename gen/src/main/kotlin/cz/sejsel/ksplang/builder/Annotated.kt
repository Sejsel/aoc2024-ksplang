package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.dsl.core.Instruction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A flat representation of annotated ksplang.
 * See also [AnnotatedKsplangTree] for a version which has been transformed into a tree.
 */
sealed interface AnnotatedKsplangSegment {
    /** A ksplang operation. */
    data class Op(val instruction: String) : AnnotatedKsplangSegment
    /** A marker showing the start of a code block, match with corresponding [BlockEnd] using the [id]. */
    data class BlockStart(val name: String?, val id: Int, val type: BlockType) : AnnotatedKsplangSegment
    /** A marker showing the end of a code block, match with corresponding [BlockStart] using the [id]. */
    data class BlockEnd(val id: Int) : AnnotatedKsplangSegment
}

@Serializable
sealed interface BlockType {
    @Serializable
    @SerialName("inlined_function")
    data object InlinedFunction : BlockType
    @Serializable
    @SerialName("function_call")
    data object FunctionCall : BlockType
}

@Serializable
sealed interface AnnotatedKsplangTree {
    @Serializable
    @SerialName("op")
    data class Op(val instruction: String) : AnnotatedKsplangTree
    @Serializable
    @SerialName("block")
    data class Block(
        val name: String?,
        @SerialName("blockType") val type: BlockType,
        val children: List<AnnotatedKsplangTree>
    ) : AnnotatedKsplangTree
    @Serializable
    @SerialName("root")
    data class Root(val children: List<AnnotatedKsplangTree>) : AnnotatedKsplangTree
}

private data class WorkInProgressTree(
    val name: String?,
    val id: Int,
    val type: BlockType,
    val children: MutableList<AnnotatedKsplangTree>
)

fun List<AnnotatedKsplangSegment>.toTree(): AnnotatedKsplangTree {
    val stack = ArrayDeque<WorkInProgressTree>()
    val rootChildren = mutableListOf<AnnotatedKsplangTree>()
    for (segment in this) {
        when (segment) {
            is AnnotatedKsplangSegment.Op -> {
                val opNode = AnnotatedKsplangTree.Op(segment.instruction)
                if (stack.isEmpty()) {
                    rootChildren.add(opNode)
                } else {
                    stack.last().children.add(opNode)
                }
            }

            is AnnotatedKsplangSegment.BlockStart -> {
                stack.add(WorkInProgressTree(segment.name, segment.id, segment.type, mutableListOf()))
            }

            is AnnotatedKsplangSegment.BlockEnd -> {
                val (name, id, type, children) = stack.removeLast()
                check(id == segment.id) { "Malformed flat annotated representation - mismatched block $name end id: expected $id, got ${segment.id}" }
                val blockNode = AnnotatedKsplangTree.Block(name, type, children)
                if (stack.isEmpty()) {
                    rootChildren.add(blockNode)
                } else {
                    stack.last().children.add(blockNode)
                }
            }
        }
    }

    check(stack.isEmpty()) { "Malformed flat annotated representation - some blocks were not closed: ${stack.map { it.name }}" }
    return AnnotatedKsplangTree.Root(rootChildren)
}