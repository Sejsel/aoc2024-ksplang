@file:Suppress("ClassName")

package cz.sejsel.ksplang.dsl.core

/** A code block which can be formed into a list of instructions regardless of where they are in the program. **/
@KsplangMarker
sealed interface SimpleBlock : Block {
    fun getInstructions(): List<Instruction>
    fun appendInstructions(list: MutableList<Instruction>)
    fun asSequence(): Sequence<Instruction>
}

@KsplangMarker
data class SimpleFunction(val name: String? = null, var children: MutableList<SimpleBlock> = mutableListOf()) : SimpleBlock {
    override fun getInstructions(): List<Instruction> {
        return children.flatMap { it.getInstructions() }
    }

    override fun appendInstructions(list: MutableList<Instruction>) {
        for (child in children) {
            child.appendInstructions(list)
        }
    }


    override fun asSequence(): Sequence<Instruction> = sequence {
        for (child in children) {
            if (child is Instruction) {
                yield(child)
                continue
            }

            for (inst in child.asSequence()) {
                yield(inst)
            }
        }
    }

    operator fun SimpleFunction.unaryPlus() {
        this@SimpleFunction.children.add(this@unaryPlus)
    }

    override fun addChild(block: SimpleBlock) {
        children.add(block)
    }

    // TODO: Really need to fix the type hierarchy
    fun asComplexFunction(): ComplexFunction {
        return buildComplexFunction(name) {
            this@SimpleFunction.children.forEach {
                +it
            }
        }
    }
}


@KsplangMarker
sealed class Instruction(val text: String) : SimpleBlock {
    override fun getInstructions(): List<Instruction> = listOf(this)
    override fun appendInstructions(list: MutableList<Instruction>) {
        list.add(this)
    }
    override fun asSequence(): Sequence<Instruction> = sequenceOf(this)
    override fun addChild(block: SimpleBlock) {
        // This is quite ugly API-wise, but not having Instructions
        // implement ComplexOrSimpleBlock ends up even worse.
        throw UnsupportedOperationException("Instructions cannot contain other blocks.")
    }

    companion object {
        fun fromText(text: String): Instruction? = when (text.lowercase()) {
            "cs" -> CS
            "++" -> inc
            "pop" -> pop
            "pop2" -> pop2
            "swap" -> swap
            "tetr" -> tetr
            "^^" -> tetr2
            "funkcia" -> funkcia
            "l-swap" -> lswap
            "%" -> modulo
            "lensum" -> lensum
            "rem" -> REM
            "bitshift" -> bitshift
            "qeq" -> qeq
            "lroll" -> lroll
            "u" -> u
            "gcd" -> gcd
            "d" -> d
            "and" -> bitand
            "praise" -> praise
            "m" -> m
            "brz" -> brz
            "j" -> j
            "call" -> call
            "goto" -> goto
            "bulkxor" -> bulkxor
            "max" -> max2
            "sum" -> sumall
            "-ff" -> ff
            "kpi" -> kpi
            "rev" -> rev
            "deez" -> deez
            "spanek" -> spanek
            else -> null
        }
    }
}

data object CS : Instruction("CS")
data object inc : Instruction("++")
data object pop : Instruction("pop")
data object pop2 : Instruction("pop2")
data object swap : Instruction("swap")
data object tetr : Instruction("tetr")
data object tetr2 : Instruction("^^")
data object funkcia : Instruction("funkcia")
data object lswap : Instruction("l-swap")
data object modulo : Instruction("%")
data object lensum : Instruction("lensum")
data object REM : Instruction("rem")
data object bitshift : Instruction("bitshift")
data object qeq : Instruction("qeq")
data object lroll : Instruction("lroll")
data object u : Instruction("u")
data object gcd : Instruction("gcd")
data object d : Instruction("d")
data object bitand : Instruction("and")
data object praise : Instruction("praise")
data object m : Instruction("m")
data object brz : Instruction("brz")
data object j : Instruction("j")
data object call : Instruction("call")
data object goto : Instruction("goto")
data object bulkxor : Instruction("bulkxor")
data object max2 : Instruction("max")
data object sumall : Instruction("sum")
data object ff : Instruction("-ff")
data object kpi : Instruction("kpi")
data object rev : Instruction("rev")
data object deez : Instruction("deez")
data object spanek : Instruction("spanek")

@KsplangMarker
fun buildFunction(name: String? = null, init: SimpleFunction.() -> Unit): SimpleFunction {
    val f = SimpleFunction(name)
    f.init()
    return f
}

/**
 * Extracts a single function from a block.
 * This is mostly a helper function to avoid the need to separate builders
 * and the underlying [SimpleFunction] / [ComplexFunction] in the `std` package.
 *
 * Example use:
 * ```kt
 * val f = extract { push(1) }
 * assert(f.name == "push(1)"))
 * ```
 *
 * @throws AssertionError if the block does not contain exactly one function.
 */
@KsplangMarker
fun extract(init: SimpleFunction.() -> Unit): SimpleFunction {
    val f = SimpleFunction()
    f.init()

    assert(f.children.size == 1)
    val extracted = f.children[0] as SimpleFunction
    return extracted
}
