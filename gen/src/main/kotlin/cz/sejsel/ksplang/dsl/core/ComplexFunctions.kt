package cz.sejsel.ksplang.dsl.core

import cz.sejsel.ksplang.dsl.core.ComplexFunction

// Instructions are individual instructions (single words)
// SimpleFunction contains Instructions or SimpleFunctions
// ComplexFunction contains ComplexFunctions or SimpleFunctions or Instructions

sealed interface ComplexOrSimpleBlock

sealed interface ComplexBlock : ComplexOrSimpleBlock {
    var children: MutableList<ComplexOrSimpleBlock>
}

data class ComplexFunction(override var children: MutableList<ComplexOrSimpleBlock> = mutableListOf()) : ComplexBlock {
    constructor(vararg children: ComplexOrSimpleBlock) : this(children.toMutableList())
}

fun ComplexFunction.ifZero(init: IfZero.() -> Unit): IfZero {
    val f = IfZero()
    f.init()
    children.add(f)
    return f
}

data class IfZero(override var children: MutableList<ComplexOrSimpleBlock> = mutableListOf(), var orElse: ComplexFunction? = null) : ComplexBlock

infix fun IfZero.orIfNonZero(init: ComplexFunction.() -> Unit) {
    val f = ComplexFunction()
    f.init()
    this.orElse = f
}

data class DoWhileZero(override var children: MutableList<ComplexOrSimpleBlock> = mutableListOf()) : ComplexBlock

// TODO: Maybe change syntax to something like
//  doWhile {
//  }.isZero()
fun ComplexFunction.doWhileZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    children.add(f)
    return f
}

@KsplangMarker
fun complex(init: ComplexFunction.() -> Unit): ComplexFunction {
    val f = ComplexFunction()
    f.init()
    return f
}

@KsplangMarker
fun ComplexBlock.CS() {
    children.add(CS)
}

@KsplangMarker
fun ComplexBlock.inc() {
    children.add(inc)
}

@KsplangMarker
fun ComplexBlock.pop() {
    children.add(pop)
}

@KsplangMarker
fun ComplexBlock.pop2() {
    children.add(pop2)
}

@KsplangMarker
fun ComplexBlock.swap() {
    children.add(swap)
}

@KsplangMarker
fun ComplexBlock.tetr() {
    children.add(tetr)
}

@KsplangMarker
fun ComplexBlock.tetr2() {
    children.add(tetr2)
}

@KsplangMarker
fun ComplexBlock.funkcia() {
    children.add(funkcia)
}

@KsplangMarker
fun ComplexBlock.lswap() {
    children.add(lswap)
}

@KsplangMarker
fun ComplexBlock.modulo() {
    children.add(modulo)
}

@KsplangMarker
fun ComplexBlock.lensum() {
    children.add(lensum)
}

@KsplangMarker
fun ComplexBlock.REM() {
    children.add(REM)
}

@KsplangMarker
fun ComplexBlock.bitshift() {
    children.add(bitshift)
}

@KsplangMarker
fun ComplexBlock.qeq() {
    children.add(qeq)
}

@KsplangMarker
fun ComplexBlock.lroll() {
    children.add(lroll)
}

@KsplangMarker
fun ComplexBlock.u() {
    children.add(u)
}

@KsplangMarker
fun ComplexBlock.gcd() {
    children.add(gcd)
}

@KsplangMarker
fun ComplexBlock.d() {
    children.add(d)
}

@KsplangMarker
fun ComplexBlock.bitand() {
    children.add(bitand)
}

@KsplangMarker
fun ComplexBlock.praise() {
    children.add(praise)
}

@KsplangMarker
fun ComplexBlock.m() {
    children.add(m)
}

@KsplangMarker
fun ComplexBlock.brz() {
    children.add(brz)
}

@KsplangMarker
fun ComplexBlock.j() {
    children.add(j)
}

@KsplangMarker
fun ComplexBlock.call() {
    children.add(call)
}

@KsplangMarker
fun ComplexBlock.goto() {
    children.add(goto)
}

@KsplangMarker
fun ComplexBlock.bulkxor() {
    children.add(bulkxor)
}

@KsplangMarker
fun ComplexBlock.max2() {
    children.add(max2)
}

@KsplangMarker
fun ComplexBlock.sumall() {
    children.add(sumall)
}

@KsplangMarker
fun ComplexBlock.ff() {
    children.add(ff)
}

@KsplangMarker
fun ComplexBlock.kpi() {
    children.add(kpi)
}

@KsplangMarker
fun ComplexBlock.rev() {
    children.add(rev)
}

@KsplangMarker
fun ComplexBlock.deez() {
    children.add(deez)
}

@KsplangMarker
fun ComplexBlock.spanek() {
    children.add(spanek)
}
