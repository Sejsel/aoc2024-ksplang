package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block

/**
 * Adds the top two values on the stack. Crashes in case of overflow.
 *
 * Signature: `a b -> a+b`
 */
fun Block.add() = function("add") {
    push(0)
    u()
}

/**
 * Subtracts the top two values and returns the absolute value of the difference.
 *
 * Signature: `a b -> |a-b|`
 */
fun Block.subabs() = function("subabs") {
    push(1)
    u()
}

/**
 * Multiplies the top two values on the stack. Crashes in case of overflow.
 *
 * Signature: `a b -> a*b`
 */
fun Block.mul() = function("mul") {
    push(2)
    u()
}

/**
 * Divides the top values or calculates the modulo if they are not cleanly divisible.
 *
 * Signature: `a b -> b % a == 0 ? b // a : b % a`
 */
fun Block.cursedDiv() = function("cursedDiv") {
    push(3)
    u()
}

/**
 * Divides the top two values on the stack. Crashes with division by zero.
 *
 * Signature: `a b -> b // a`
 */
fun Block.div() = function("div") {
    // a b
    dupAb()
    // a b a b
    REM()
    // a b b%a
    negate()
    // a b -b%a
    add()
    // a b-b%a
    cursedDiv()
}


/**
 * Negates the top value on the stack. Crashes with overflow with -2^63.
 *
 * Signature: `a -> -a`
 */
fun Block.negate() = function("negate") {
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
fun Block.sgn() = function("sgn") {
    push(5)
    u()
}

/**
 * Returns the absolute value of the top value on the stack.
 * Crashes due to overflow with -2^63:
 *
 * Signature: `a -> |a|`
 */
fun Block.abs() = function("abs") {
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
fun Block.zeroNot() = function("zeroNot") {
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

/**
 * Decrements the top value on the stack.
 *
 * Signature: `a -> a-1`
 */
fun Block.dec() = function("dec") {
    push(0)
    CS()
    inc()
    CS()
    qeq()
    u()
}

/**
 * Decrements the top **positive** value on the stack.
 * For nonpositive values (including 0), the behavior is undefined.
 *
 * Signature: `a -> a-1`
 */
fun Block.decPositive() = function("decPositive") {
    push(1)
    CS()
    u()
}