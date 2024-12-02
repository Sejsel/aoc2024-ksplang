package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block

/**
 * Adds the top two values on the stack. Crashes in case of overflow.
 *
 * Signature: `a b -> a+b`
 */
fun Block.add() {
    push(0)
    u()
}

/**
 * Subtracts the top two values and returns the absolute value of the difference.
 *
 * Signature: `a b -> |a-b|`
 */
fun Block.subabs() {
    push(1)
    u()
}

/**
 * Negates the top value on the stack. Crashes with overflow with -2^63.
 *
 * Signature: `a -> -a`
 */
fun Block.negate() {
    // This works by using qeq to solve the equation x + n = 0
    push(1)
    CS()
    CS()
    modulo()
    qeq()
}

/**
 * For a given value x, returns:
 * - 1 if x > 0
 * - 0 if x = 0
 * - -1 if x < 0
 *
 * Signature: `a -> sgn(a)`
 */
fun Block.sgn() {
    push(5)
    u()
}

/**
 * Returns the absolute value of the top value on the stack.
 * Crashes due to overflow with -2^63:
 *
 * Signature: `a -> |a|`
 */
fun Block.abs() {
    // This is push(0) + subabs
    push(0)
    CS()
    inc()
    u()
}

/**
 * Returns 1 if the top value on the stack is zero, 0 otherwise. A negation of "zeroity".
 *
 * Signature: `a -> a == 0 ? 1 : 0`
 */
fun Block.zeroNot() {
    // x
    sgn()
    // sgn(x)
    abs()
    // |sgn(x)|, i.e. 0 or 1
    CS(); j(); inc()
    // |sgn(x)| 1
    CS()
    // |sgn(x)| 1 1
    bulkxor()
}
