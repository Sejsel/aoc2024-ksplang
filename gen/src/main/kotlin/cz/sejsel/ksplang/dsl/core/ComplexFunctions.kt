package cz.sejsel.ksplang.dsl.core

import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.std.zeroNot

// Instructions are individual instructions (single words)
// SimpleFunction contains Instructions or SimpleFunctions
// ComplexFunction contains ComplexFunctions or SimpleFunctions or Instructions

sealed interface ComplexOrSimpleBlock {
    fun add(block: SimpleBlock)

    // These are functions and not extension functions very much on purpose.
    // They do not have to be imported, and they end up colored differently from (extension) functions.
    @KsplangMarker
    fun function(name: String? = null, init: SimpleFunction.() -> Unit): SimpleFunction {
        val f = SimpleFunction(name)
        f.init()
        add(f)
        return f
    }

    @KsplangMarker
    fun CS() {
        add(CS)
    }

    @KsplangMarker
    fun inc() {
        add(inc)
    }

    @KsplangMarker
    fun pop() {
        add(pop)
    }

    @KsplangMarker
    fun pop2() {
        add(pop2)
    }

    @KsplangMarker
    fun swap() {
        add(swap)
    }

    @KsplangMarker
    fun tetr() {
        add(tetr)
    }

    @KsplangMarker
    fun tetr2() {
        add(tetr2)
    }

    @KsplangMarker
    fun funkcia() {
        add(funkcia)
    }

    @KsplangMarker
    fun lswap() {
        add(lswap)
    }

    @KsplangMarker
    fun modulo() {
        add(modulo)
    }

    @KsplangMarker
    fun lensum() {
        add(lensum)
    }

    @KsplangMarker
    fun REM() {
        add(REM)
    }

    @KsplangMarker
    fun bitshift() {
        add(bitshift)
    }

    @KsplangMarker
    fun qeq() {
        add(qeq)
    }

    @KsplangMarker
    fun lroll() {
        add(lroll)
    }

    @KsplangMarker
    fun u() {
        add(u)
    }

    @KsplangMarker
    fun gcd() {
        add(gcd)
    }

    @KsplangMarker
    fun d() {
        add(d)
    }

    @KsplangMarker
    fun bitand() {
        add(bitand)
    }

    @KsplangMarker
    fun praise() {
        add(praise)
    }

    @KsplangMarker
    fun m() {
        add(m)
    }

    @KsplangMarker
    fun brz() {
        add(brz)
    }

    @KsplangMarker
    fun j() {
        add(j)
    }

    @KsplangMarker
    fun call() {
        add(call)
    }

    @KsplangMarker
    fun goto() {
        add(goto)
    }

    @KsplangMarker
    fun bulkxor() {
        add(bulkxor)
    }

    @KsplangMarker
    fun max2() {
        add(max2)
    }

    @KsplangMarker
    fun sumall() {
        add(sumall)
    }

    @KsplangMarker
    fun ff() {
        add(ff)
    }

    @KsplangMarker
    fun kpi() {
        add(kpi)
    }

    @KsplangMarker
    fun rev() {
        add(rev)
    }

    @KsplangMarker
    fun deez() {
        add(deez)
    }

    @KsplangMarker
    fun spanek() {
        add(spanek)
    }
}

sealed interface ComplexBlock : ComplexOrSimpleBlock {
    var children: MutableList<ComplexOrSimpleBlock>
}

data class ComplexFunction(override var children: MutableList<ComplexOrSimpleBlock> = mutableListOf()) : ComplexBlock {
    constructor(vararg children: ComplexOrSimpleBlock) : this(children.toMutableList())

    override fun add(block: SimpleBlock) {
        children.add(block)
    }
}

fun ComplexFunction.ifZero(init: IfZero.() -> Unit): IfZero {
    val f = IfZero()
    f.init()
    children.add(f)
    return f
}

data class IfZero(override var children: MutableList<ComplexOrSimpleBlock> = mutableListOf(), var orElse: ComplexFunction? = null) : ComplexBlock {
    override fun add(block: SimpleBlock) {
        children.add(block)
    }
}

infix fun IfZero.orIfNonZero(init: ComplexFunction.() -> Unit) {
    val f = ComplexFunction()
    f.init()
    this.orElse = f
}

data class DoWhileZero(override var children: MutableList<ComplexOrSimpleBlock> = mutableListOf()) : ComplexBlock {
    override fun add(block: SimpleBlock) {
        children.add(block)
    }
}

fun ComplexFunction.doWhileZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    children.add(f)
    return f
}

fun ComplexFunction.doWhileNonZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    f.apply {
        zeroNot()
    }
    children.add(f)
    return f
}

@KsplangMarker
fun complex(init: ComplexFunction.() -> Unit): ComplexFunction {
    val f = ComplexFunction()
    f.init()
    return f
}