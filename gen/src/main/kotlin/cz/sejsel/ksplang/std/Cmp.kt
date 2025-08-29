package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise

/** A comparison of two values on the stack. Handles all i64 values.
 *
 * - `a > b` → 1
 * - `a < b` → -1
 * - `a = b` → 0
 *
 * Signature: a b => sgn(a-b)
 */
fun ComplexBlock.cmp() = complexFunction("cmp") {
    // a b
    dupAb()
    // a b a b
    sgn()
    // a b a sgn(b)
    roll(2, 1)
    // a b sgn(b) a
    sgn()
    // a b sgn(b) sgn(a)
    roll(2, 1)
    // a b sgn(a) sgn(b)
    negate()
    add()
    // a b sgn(a)-sgn(b)
    ifZero {
        // a b 0
        pop()
        // a b
        dup(); sgn(); negate(); add()
        // a b-sgn(b)
        negate()
        // a -(b-sgn(b))
        swap2()
        // -(b-sgn(b)) a
        dup(); sgn(); negate(); add()
        // -(b-sgn(b)) a-sgn(a)
        add()
        // a-b
        sgn()
        // sgn(a-b)
    } otherwise {
        sgn()
        // a b sgn(sgn(a)-sgn(b))
        pop2()
        pop2()
    }
    // sgn(a-b)
}

/**
 * Checks whether a number *x* is in a given range (inclusive start and end).
 * Result is 1 if the number is in the range, 0 otherwise.
 *
 * Signature: x from to -> 1 if x is in [from, to], 0 otherwise
 */
fun ComplexBlock.isInRange() = complexFunction("isInRange") {
    // x from to
    dupThird()
    // x from to x
    cmp(); inc(); zeroNotPositive()
    // x from !(cmp(to,x)+1)
    permute("x f c", "c x f")
    // cmp(to,x)+1 x from
    cmp(); inc(); zeroNotPositive()
    // cmp(to,x)+1 !(cmp(x,from)+1)
    add()
    // 0 if x is in range, something positive otherwise
    zeroNotPositive()
    // 0 if x is out of range, 1 otherwise
}

/**
 * Checks whether a >= b.
 *
 * Signature: `a b -> 1 if a >= b, 0 otherwise`
 */
fun ComplexBlock.geq() = complexFunction("geq") {
    // a b
    cmp()
    // sgn(a-b)
    inc()
    // sgn(a-b)+1
    sgn()
}

/**
 * Checks whether a > b.
 *
 * Signature: `a b -> 1 if a > b, 0 otherwise`
 */
fun ComplexBlock.gt() = complexFunction("gt") {
    // a b
    cmp()
    // sgn(a-b)
    zeroNotPositive()
    zeroNotPositive()
}


/**
 * Checks whether a <= b.
 *
 * Signature: `a b -> 1 if a <= b, 0 otherwise`
 */
fun ComplexBlock.leq() = complexFunction("leq") {
    // a b
    cmp()
    // sgn(a-b)
    negate()
    // -sgn(a-b)
    inc()
    // -sgn(a-b)+1
    sgn()
}

/**
 * Checks whether a < b.
 *
 * Signature: `a b -> 1 if a < b, 0 otherwise`
 */
fun ComplexBlock.lt() = complexFunction("lt") {
    // a b
    cmp()
    // sgn(a-b)
    inc()
    zeroNotPositive()
}