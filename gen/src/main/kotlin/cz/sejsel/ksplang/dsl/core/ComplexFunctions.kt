package cz.sejsel.ksplang.dsl.core

import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.sgn
import cz.sejsel.ksplang.std.zeroNot
import cz.sejsel.ksplang.std.zeroNotPositive

// Instructions are individual instructions (single words)
// SimpleFunction contains Instructions or SimpleFunctions
// ComplexFunction contains ComplexFunctions or SimpleFunctions or Instructions

@KsplangMarker
sealed interface Block {
    fun addChild(block: SimpleBlock)

    // These are functions and not extension functions very much on purpose.
    // They do not have to be imported, and they end up colored differently from (extension) functions.
    fun function(name: String? = null, init: SimpleFunction.() -> Unit): SimpleFunction {
        val f = SimpleFunction(name)
        f.init()
        addChild(f)
        return f
    }

    fun CS() {
        addChild(CS)
    }

    fun inc() {
        addChild(inc)
    }

    fun pop() {
        addChild(pop)
    }

    fun pop2() {
        addChild(pop2)
    }

    fun swap() {
        addChild(swap)
    }

    fun tetr() {
        addChild(tetr)
    }

    fun tetr2() {
        addChild(tetr2)
    }

    fun funkcia() {
        addChild(funkcia)
    }

    fun lswap() {
        addChild(lswap)
    }

    /** a b -> b % a */
    fun modulo() {
        addChild(modulo)
    }

    fun lensum() {
        addChild(lensum)
    }

    fun REM() {
        addChild(REM)
    }

    fun bitshift() {
        addChild(bitshift)
    }

    fun qeq() {
        addChild(qeq)
    }

    /**
     * Rotates right (assuming the stack grows right).
     *
     * Signature: `dist len ->`, also moves top len elements right (circularly).
     */
    fun lroll() {
        addChild(lroll)
    }

    /**
     * Universal arithmetic operation.
     *
     * Pops the top value from the stack, this is the operation id. If operation id is:
     * - 0, replaces next two top values with their sum.
     * - 1, replaces next two top values with their absolute difference.
     * - 2, replaces next two top values with their product.
     * - 3, divides the top value by the second top value,
     *     - if the result is an integer, replaces the two values with the quotient
     *     - if the result is not an integer, replaces the two values with the remainder (like `a % b` in C)
     * - 4, replaces the top value with its factorial of its absolute value
     * - 5, replaces the top value with its sign (1 for positive, -1 for negative, 0 for zero)
     * - otherwise, program execution ends with an error.
     */
    fun u() {
        addChild(u)
    }

    fun gcd() {
        addChild(gcd)
    }

    fun d() {
        addChild(d)
    }

    fun bitand() {
        addChild(bitand)
    }

    fun praise() {
        addChild(praise)
    }

    fun m() {
        addChild(m)
    }

    fun brz() {
        addChild(brz)
    }

    fun j() {
        addChild(j)
    }

    fun call() {
        addChild(call)
    }

    fun goto() {
        addChild(goto)
    }

    fun bulkxor() {
        addChild(bulkxor)
    }

    fun max2() {
        addChild(max2)
    }

    fun sumall() {
        addChild(sumall)
    }

    fun ff() {
        addChild(ff)
    }

    fun kpi() {
        addChild(kpi)
    }

    fun rev() {
        addChild(rev)
    }

    fun deez() {
        addChild(deez)
    }

    fun spanek() {
        addChild(spanek)
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

    override fun addChild(block: SimpleBlock) {
        children.add(block)
    }
}

fun ComplexBlock.ifZero(popChecked: Boolean = false, init: IfZero.() -> Unit): IfZero {
    val f = IfZero(popChecked = popChecked)
    f.init()
    children.add(f)
    return f
}

@KsplangMarker
data class IfZero(override var children: MutableList<Block> = mutableListOf(), var orElse: ComplexFunction? = null, var popChecked: Boolean = false) : ComplexBlock {
    override fun addChild(block: SimpleBlock) {
        children.add(block)
    }
}

infix fun IfZero.otherwise(init: ComplexFunction.() -> Unit) {
    val f = ComplexFunction()
    f.init()
    this.orElse = f
}

@KsplangMarker
data class WhileNonZero(override var children: MutableList<Block> = mutableListOf()) : ComplexBlock {
    override fun addChild(block: SimpleBlock) {
        children.add(block)
    }
}

/**
 * Given x on the stack, executes the inner loop until the top value is zero at the end of the loop.
 * Does not pop the x value after checking. Does pop the value at the end.
 *
 * Example which will run x times:
 * ```
 * // x
 * whileNonZero {
 *   // x
 *   dec()
 *   // x-1
 * }
 * // nothing
 * ```
 *
 * Example which will remove all zeros from the end of the stack:
 * ```
 * whileNonZero {
 *   pop()
 * }
 * ```
 */
fun ComplexBlock.whileNonZero(init: WhileNonZero.() -> Unit): WhileNonZero {
    val f = WhileNonZero()
    f.init()
    children.add(f)
    return f
}

@KsplangMarker
data class DoWhileZero(override var children: MutableList<Block> = mutableListOf()) : ComplexBlock {
    override fun addChild(block: SimpleBlock) {
        children.add(block)
    }
}

/**
 * Consumes the value at the end of the stack at the end of each loop.
 */
fun ComplexBlock.doWhileZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    children.add(f)
    return f
}

/**
 * Consumes the value at the end of the stack at the end of each loop.
 */
fun ComplexBlock.doWhileNonZero(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    f.apply {
        zeroNot()
    }
    children.add(f)
    return f
}

/**
 * Consumes the value at the end of the stack at the end of each loop.
 */
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

fun ComplexBlock.doWhilePositive(init: DoWhileZero.() -> Unit): DoWhileZero {
    val f = DoWhileZero()
    f.init()
    f.apply {
        // x
        sgn()
        // sgn(x)
        dec()
        // sgn(x)-1   -- negative is -2, 0 is -1, positive is 0
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