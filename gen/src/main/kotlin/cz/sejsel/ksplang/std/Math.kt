package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.SimpleFunction

/**
 * Adds the top two values on the stack. Crashes in case of overflow.
 *
 * Signature: `a b -> a+b`
 */
fun SimpleFunction.add() {
    push(0)
    u()
}

/**
 * Subtracts the top two values and returns the absolute value of the difference.
 *
 * Signature: `a b -> |a-b|`
 */
fun SimpleFunction.subabs() {
    push(1)
    u()
}

/**
 * Negates the top value on the stack. Do not use with 2^63.
 *
 * Signature: `a -> -a`
 */
fun SimpleFunction.negate() {
    // This works by using qeq to solve the equation x + n = 0
    push(1)
    CS()
    CS()
    modulo()
    qeq()
}
