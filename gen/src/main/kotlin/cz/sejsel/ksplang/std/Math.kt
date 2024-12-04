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
 * Adds a constant to the top value on the stack. Crashes in case of overflow.
 *
 * Signature: `a -> a+n`
 */
fun Block.add(n: Long) = function("add($n)") {
    if (n != 0L) {
        push(n)
        add()
    }
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
 * Multiplies the top value on the stack by a constant. Crashes in case of overflow.
 *
 * Signature: `a -> a*n`
 */
fun Block.mul(n: Long) = function("mul($n)") {
    if (n == 0L) {
        // Shorter
        pop()
        push(0)
    } else {
        push(n)
        mul()
    }
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
 * Divides the top value on the stack by a constant. Crashes with division by zero.
 *
 * Signature: `a -> a // n`
 */
fun Block.div(n: Long) = function("div($n)") {
    // a
    push(n)
    // a n
    swap2()
    // n a
    div()
    // a//n
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
 * Returns 1 if the top value on the stack is zero or negative, 0 otherwise.
 * A negation of "zeroity" where negative values are treated as zero.
 *
 * Signature: `a -> a == 0 ? 1 : 0`
 */
fun Block.zeroNotPositive() = function("zeroNot") {
    push(1)
    CS()
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

/*
 * min(a, b) based on Filip Hejsek's solution to the removed 36-2-4 task.
 * The initial pushes are optimized, but the rest is the same.
 * The jump section has to be exactly the same as the "conditional" jumps rely
 * on the length of the those sections.
 * print_expand("""
 *     IntMin IntMin 5 m 4 max % CS CS CS ++ gcd ++ ++ ++ u j j
 *     ++ ++ pop2 pop2 pop2 m pop2 7 j
 *     pop ++ m pop2 pop2 pop2 CS pop
 *     pop2 pop2
 * """)
 *
 * Signature: `a b -> min(a, b)`
 */
fun Block.min2() = function("min2") {
    push(Long.MIN_VALUE); push(Long.MIN_VALUE); push(5); m(); push(4); max2(); modulo(); CS(); CS(); CS(); inc(); gcd(); inc(); inc(); inc(); u(); j(); j()
    inc(); inc(); pop2(); pop2(); pop2(); m(); pop2()
    // push(7) of the correct length
    CS(); inc(); CS(); pop2(); CS(); pop2(); CS(); modulo(); repeat(7) { inc() }
    j()
    pop(); inc(); m(); pop2(); pop2(); pop2(); CS(); pop(); pop2(); pop2()
}
