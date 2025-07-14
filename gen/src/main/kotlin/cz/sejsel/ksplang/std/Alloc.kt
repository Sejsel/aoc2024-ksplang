package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.whileNonZero

/**
 * Inserts a block of zeroes into the stack, starting from position `from` and with length `len`.
 *
 * Signature: from len ->
 * */
fun ComplexBlock.allocNoReturn() = complexFunction("allocNoReturn") {
    // from len
    pushManyAndKeepLen(0)
    // from [len * 0] len
    dup()
    // from [len * 0] len len
    inc(); inc()
    // from [len * 0] len len+2
    push(-1)
    // from [len * 0] len len+2 -1
    swap2()
    // from [len * 0] len -1 len+2
    lroll()
    // [len * 0] len from
    stacklen()
    // [len * 0] len from stacklen
    add(-2)
    // [len * 0] len from stacklen-2
    swap2()
    sub()
    // [len * 0] len stacklen-from-2
    lroll()
}

/**
 * Inserts a block of zeroes into the stack, starting from position `from` and with length `len`.
 * Returns the position after the allocated block.
 *
 * Signature: from len -> from+len
 * */
fun ComplexBlock.alloc() = complexFunction("alloc") {
    // from len
    dupAb()
    // from len from len
    allocNoReturn()
    // from len
    add()
    // from+len
}

// TODO: Uninitialized memory allocator, we can use praise() and a few pops to make that super fast.

private const val ZEROES_PER_ITERATION = 100
/**
 * Allocates a block of zeroes into the stack, starting from position `from` and with length `len`.
 *
 * Signature: from ->
 */
fun ComplexBlock.allocNoReturnConstLen(count: Long) = complexFunction("allocNoReturnConstLen($count)") {
    require(count >= 0L) { "Count must be non-negative, got $count" }
    if (count == 0L) {
        pop()
        return@complexFunction
    }

    if (count < ZEROES_PER_ITERATION) {
        push(0)
        repeat((count - 1).toInt()) {
            CS()
        }
        // from [count * 0]
    } else {
        val iterations = count / ZEROES_PER_ITERATION
        val remainder = count % ZEROES_PER_ITERATION
        // from
        push(iterations)
        // from i
        whileNonZero {
            // from [000] i
            push(0)
            repeat(ZEROES_PER_ITERATION - 1) {
                CS()
            }
            // from [000] i [ZEROES_PER_ITERATION * 0]
            roll((ZEROES_PER_ITERATION + 1).toLong(), -1)
            // from [000] i
            dec()
            // from [000] i-1
        }
        // from [000]
        repeat(remainder.toInt()) {
            CS()
        }
    }
    // from [count * 0]
    push(count)
    // from [count * 0] count
    roll(count + 2, -1)
    // [count * 0] count from

    // Now, we need move the zeroes. Unfortunately, we do not know the stack length. There are two ways we could do this instead:
    // - do the whole thing in `rev`, where we would know the stacklen (it would be from)
    // - stacklen, but we know that there are at least from + count elements on the stack

    dup()
    push(count)
    add()
    // [count * 0] count from from+count
    stacklenWithMin()
    // [count * 0] count from stacklen
    add(-2)
    // [count * 0] count from stacklen-2
    swap2()
    sub()
    // [count * 0] count stacklen-from-2
    lroll()
}