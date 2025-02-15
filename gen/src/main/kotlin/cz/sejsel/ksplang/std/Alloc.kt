package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock

/**
 * Inserts a block of zeroes into the stack, starting from position `from` and with length `len`.
 *
 * Signature: from len ->
 * */
fun ComplexBlock.allocNoReturn() = complexFunction("allocNoReturn") {
    // from len
    pushManyAndKeepLen(0)
    // from [count * len] len
    dup()
    // from [count * len] len len
    inc(); inc()
    // from [count * len] len len+2
    push(-1)
    // from [count * len] len len+2 -1
    swap2()
    // from [count * len] len -1 len+2
    lroll()
    // [count * len] len from
    stacklen()
    // [count * len] len from stacklen
    add(-2)
    // [count * len] len from stacklen-2
    swap2()
    sub()
    // [count * len] len stacklen-from-2
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
