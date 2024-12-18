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
        // a b
        pop()
        // a
        dupAb()
        // a a b
        dup(); sgn(); negate(); add()
        // a a b-sgn(b)
        negate()
        // a a -(b-sgn(b))
        roll(2, 1)
        // a -(b-sgn(b)) a
        dup(); sgn(); negate(); add()
        // a -(b-sgn(b)) a-sgn(a)
        add()
        // a a-b
        sgn()
        // a sgn(a-b)
    } otherwise {
        sgn()
        // a b sgn(sgn(a)-sgn(b))
    }
    // a b sgn(a-b)
    pop2()
    pop2()
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