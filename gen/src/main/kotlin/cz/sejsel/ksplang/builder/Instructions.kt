package cz.sejsel.ksplang.builder

/** A code block which can be formed into a list of instructions regardless of where they are in the program. **/
sealed interface InstructionBlock {
    fun getInstructions(): List<Instruction>
}

@KsplangMarker
class SimpleFunction(children: List<InstructionBlock> = emptyList()) : InstructionBlock {
    private var children = children.toMutableList()

    override fun getInstructions(): List<Instruction> {
        return children.flatMap { it.getInstructions() }
    }

    /*
    @KsplangMarker
    fun function(init: SimpleFunction.() -> Unit) {
        val f = SimpleFunction()
        f.init()
        children.add(f)
    }
     */

    fun CS() {
        children.add(CS)
    }

    fun inc() {
        children.add(inc)
    }
}


sealed class Instruction(val text: String) : InstructionBlock {
    override fun getInstructions(): List<Instruction> {
        return listOf(this)
    }
}

data object CS : Instruction("CS")
@Suppress("ClassName")
data object inc : Instruction("++")
