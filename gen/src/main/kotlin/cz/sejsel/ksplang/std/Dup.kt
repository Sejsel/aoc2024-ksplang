package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block

/**
 * Duplicates the top value on the stack.
 *
 * Signature: `a -> a a`
 */
fun Block.dup() = function("dup") {
    push(0); push(3); m()
    // x 0 3 clamp(x, 0-3)
    CS(); CS(); inc(); gcd(); inc()  // push(2) onto 0-3
    // x 0 3 clamp(x, 0-3) 2
    max2()
    // x 0 3 clamp(x, 2-3)
    CS(); CS(); modulo()  // push(0) onto 2-3
    // x 0 3 clamp(x, 2-3) 0
    qeq()
    // now different things happen for 2/3
    // x 0 OR x 0 -1
    CS(); CS(); CS(); inc(); inc()  // push 0 0 2 / 1 1 3
    // x 0 0 0 2 OR x 0 -1 1 1 3
    qeq()
    // x 0 0 OR x 0 -1
    pop2()
    // x 0 OR x -1
    // With the CS j inc prefix, there is no better suffix,
    // we checked all 9 instruction suffixes and none produce -2^63 (this one is 10 instructions)
    CS(); j(); inc(); CS(); praise(); qeq(); qeq(); pop2(); funkcia(); funkcia(); inc(); modulo(); bitshift()  // push -2^63 onto 0/-1
    // x 0 -2^63 OR x -1 -2^63
    CS(); CS(); gcd()  // push 1 onto -2^63
    CS(); inc()  // push 2 onto 1
    lroll()  // roll(2, 1)
    // x -2^63 0 OR x -2^63 -1
    CS()
    // x -2^63 0 0 OR x -2^63 -1 1
    u()
    // x -2^63 OR x 2^63-1
    // There are 12 ways to push 3 onto -2^63/2^63-1 in 5 instructions
    CS(); CS(); pop2(); CS(); lensum()  // push 3 onto -2^63/2^63-1
    // x<=2 -2^63 3 OR x>=3 2^63-1 3
    m()
    // x -2^63 3 x OR x 2^63-1 3 x
    pop2(); pop2()
    // x x
}


/**
 * Duplicates the top two values on the stack.
 *
 * Signature: `a b -> a b a b`
 */
fun Block.dupAb() = function("dup_ab") {
    // a b
    dup()
    // a b b
    roll(3, 2)
    // b b a
    dup()
    // b b a a
    roll(4, 1)
    // a b b a
    roll(2, 1)
    // a b a b
}

/**
 * Duplicates the *n*-th element on the stack, indexed from zero.
 *
 * dupNthZeroIndexed(0) = a -> a a
 * dupNthZeroIndexed(1) = a b -> a b a
 * dupNthZeroIndexed(2) = a b c -> a b c a
 */
fun Block.dupNthZeroIndexed(n: Long) = function("dup_nth_zero_indexed($n)") {
    assert(n >= 0)
    // a [...]
    roll(n + 1, n)
    // [...] a
    dup()
    // [...] a a
    roll(n + 2, 1)
    // a [...] a
}

/**
 * Duplicates the second element on the stack.
 *
 * Signature: `a b -> a b a`
 */
fun Block.dupSecond() = dupNthZeroIndexed(1)
/**
 * Duplicates the third element on the stack.
 *
 * Signature: `a b c -> a b c a`
 */
fun Block.dupThird() = dupNthZeroIndexed(2)
/**
 * Duplicates the fourth element on the stack.
 *
 * Signature: `a b c d -> a b c d a`
 */
fun Block.dupFourth() = dupNthZeroIndexed(3)
/**
 * Duplicates the fifth element on the stack.
 *
 * Signature: `a b c d e -> a b c d e a`
 */
fun Block.dupFifth() = dupNthZeroIndexed(4)
/**
 * Duplicates the sixth element on the stack.
 *
 * Signature: `a b c d e f -> a b c d e f a`
 */
fun Block.dupSixth() = dupNthZeroIndexed(5)
/**
 * Duplicates the seventh element on the stack.
 *
 * Signature: `a b c d e f g  -> a b c d e f g a`
 */
fun Block.dupSeventh() = dupNthZeroIndexed(6)
/**
 * Duplicates the eighth element on the stack.
 *
 * Signature: `a b c d e f g h -> a b c d e f g h a`
 */
fun Block.dupEighth() = dupNthZeroIndexed(7)
/**
 * Duplicates the ninth element on the stack.
 *
 * Signature: `a b c d e f g h i -> a b c d e f g h i a`
 */
fun Block.dupNinth() = dupNthZeroIndexed(8)
/**
 * Duplicates the tenth element on the stack.
 *
 * Signature: `a b c d e f g h i j -> a b c d e f g h i j a`
 */
fun Block.dupTenth() = dupNthZeroIndexed(9)
