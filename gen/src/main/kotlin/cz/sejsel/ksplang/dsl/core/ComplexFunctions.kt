package cz.sejsel.ksplang.dsl.core

import cz.sejsel.ksplang.std.sgn
import cz.sejsel.ksplang.std.zeroNot
import cz.sejsel.ksplang.std.zeroNotPositive

// Instructions are individual instructions (single words)
// SimpleFunction contains Instructions or SimpleFunctions
// ComplexFunction contains ComplexFunctions or SimpleFunctions or Instructions

@KsplangMarker
sealed interface Block {
    fun add(block: SimpleBlock)

    // These are functions and not extension functions very much on purpose.
    // They do not have to be imported, and they end up colored differently from (extension) functions.
    fun function(name: String? = null, init: SimpleFunction.() -> Unit): SimpleFunction {
        val f = SimpleFunction(name)
        f.init()
        add(f)
        return f
    }

    fun CS() {
        add(CS)
    }

    fun inc() {
        add(inc)
    }

    fun pop() {
        add(pop)
    }

    fun pop2() {
        add(pop2)
    }

    fun swap() {
        add(swap)
    }

    fun tetr() {
        add(tetr)
    }

    fun tetr2() {
        add(tetr2)
    }

    fun funkcia() {
        add(funkcia)
    }

    fun lswap() {
        add(lswap)
    }

    fun modulo() {
        add(modulo)
    }

    fun lensum() {
        add(lensum)
    }

    fun REM() {
        add(REM)
    }

    fun bitshift() {
        add(bitshift)
    }

    fun qeq() {
        add(qeq)
    }

    /**
     * Rotates right (assuming the stack grows right).
     *
     * Signature: `dist len ->`, also moves top len elements right (circularly).
     */
    fun lroll() {
        add(lroll)
    }

    fun u() {
        add(u)
    }

    fun gcd() {
        add(gcd)
    }

    fun d() {
        add(d)
    }

    fun bitand() {
        add(bitand)
    }

    fun praise() {
        add(praise)
    }

    fun m() {
        add(m)
    }

    fun brz() {
        add(brz)
    }

    fun j() {
        add(j)
    }

    fun call() {
        add(call)
    }

    fun goto() {
        add(goto)
    }

    fun bulkxor() {
        add(bulkxor)
    }

    fun max2() {
        add(max2)
    }

    fun sumall() {
        add(sumall)
    }

    fun ff() {
        add(ff)
    }

    fun kpi() {
        add(kpi)
    }

    fun rev() {
        add(rev)
    }

    fun deez() {
        add(deez)
    }

    fun spanek() {
        add(spanek)
    }
}

@KsplangMarker
sealed interface ComplexBlock : Block {
    var children: MutableList<Block>

    operator fun Block.unaryPlus() {
        this@ComplexBlock.children.add(this@unaryPlus)
    }

    @KsplangMarker
    fun complexFunction(name: String? = null, init: ComplexFunction.() -> Unit): ComplexFunction {
        val f = ComplexFunction(name)
        f.init()
        children.add(f)
        return f
    }
}

@KsplangMarker
data class ComplexFunction(val name: String? = null, override var children: MutableList<Block> = mutableListOf()) : ComplexBlock {
    constructor(name: String? = null, vararg children: Block) : this(name, children.toMutableList())

    override fun add(block: SimpleBlock) {
        children.add(block)
    }
}

fun ComplexBlock.ifZero(init: IfZero.() -> Unit): IfZero {
    val f = IfZero()
    f.init()
    children.add(f)
    return f
}

@KsplangMarker
data class IfZero(override var children: MutableList<Block> = mutableListOf(), var orElse: ComplexFunction? = null, var popChecked: Boolean = false) : ComplexBlock {
    override fun add(block: SimpleBlock) {
        children.add(block)
    }
}

infix fun IfZero.otherwise(init: ComplexFunction.() -> Unit) {
    val f = ComplexFunction()
    f.init()
    this.orElse = f
}

@KsplangMarker
data class DoWhileZero(override var children: MutableList<Block> = mutableListOf()) : ComplexBlock {
    override fun add(block: SimpleBlock) {
        children.add(block)
    }
}

fun ComplexBlock.doWhileZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    children.add(f)
    return f
}

fun ComplexBlock.doWhileNonZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    f.apply {
        zeroNot()
    }
    children.add(f)
    return f
}

fun ComplexBlock.doWhileNonNegative(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    f.apply {
        // x
        sgn()
        // sgn(x)
        inc()
        // sgn(x)+1   -- negative is 0, 0 is 1, positive is 2
        zeroNotPositive()
        // negative is 1, 0 is 0, positive is 0
    }
    children.add(f)
    return f
}

@KsplangMarker
fun buildComplexFunction(name: String? = null, init: ComplexFunction.() -> Unit): ComplexFunction {
    val f = ComplexFunction(name)
    f.init()
    return f
}