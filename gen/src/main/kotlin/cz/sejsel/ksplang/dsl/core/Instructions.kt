@file:Suppress("ClassName")

package cz.sejsel.ksplang.dsl.core

/** A code block which can be formed into a list of instructions regardless of where they are in the program. **/
sealed interface SimpleBlock : ComplexOrSimpleBlock {
    fun getInstructions(): List<Instruction>
}

@Suppress("FunctionName")
@KsplangMarker
class SimpleFunction(name: String? = null, children: List<SimpleBlock> = emptyList()) : SimpleBlock {
    var children = children.toMutableList()

    override fun getInstructions(): List<Instruction> {
        return children.flatMap { it.getInstructions() }
    }

    @KsplangMarker
    fun function(init: SimpleFunction.() -> Unit) {
        val f = SimpleFunction()
        f.init()
        children.add(f)
    }

    operator fun SimpleFunction.unaryPlus() {
        this@SimpleFunction.children.add(this@unaryPlus)
    }

    fun CS() = children.add(CS)
    fun inc() = children.add(inc)
    fun pop() = children.add(pop)
    fun pop2() = children.add(pop2)
    fun swap() = children.add(swap)
    fun tetr() = children.add(tetr)
    fun tetr2() = children.add(tetr2)
    fun funkcia() = children.add(funkcia)
    fun lswap() = children.add(lswap)
    fun modulo() = children.add(modulo)
    fun lensum() = children.add(lensum)
    fun REM() = children.add(REM)
    fun bitshift() = children.add(bitshift)
    fun qeq() = children.add(qeq)
    fun lroll() = children.add(lroll)
    fun u() = children.add(u)
    fun gcd() = children.add(gcd)
    fun d() = children.add(d)
    fun bitand() = children.add(bitand)
    fun praise() = children.add(praise)
    fun m() = children.add(m)
    fun brz() = children.add(brz)
    fun j() = children.add(j)
    fun call() = children.add(call)
    fun goto() = children.add(goto)
    fun bulkxor() = children.add(bulkxor)
    fun max2() = children.add(max2)
    fun sumall() = children.add(sumall)
    fun ff() = children.add(ff)
    fun kpi() = children.add(kpi)
    fun rev() = children.add(rev)
    fun deez() = children.add(deez)
    fun spanek() = children.add(spanek)
}


sealed class Instruction(val text: String) : SimpleBlock {
    override fun getInstructions(): List<Instruction> {
        return listOf(this)
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
fun function(name: String? = null, init: SimpleFunction.() -> Unit): SimpleFunction {
    val f = SimpleFunction()
    f.init()
    return f
}