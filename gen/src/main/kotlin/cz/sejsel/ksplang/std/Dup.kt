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
