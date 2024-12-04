package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.orIfNonZero

/* Python original:
cmp = ComplexFunction("cmp", [
    dup_ab,
    # a b a b
    sgn,
    # a b a sgn(b)
    roll(2, 1),
    # a b sgn(b) a
    sgn,
    # a b sgn(b) sgn(a)
    roll(2, 1),
    # a b sgn(a) sgn(b)
    negate,
    add,
    # a b sgn(a)-sgn(b)
    IfZero(
        then=[
            pop,
            # a b
            dup_ab,
            # a b a b
            # We need to ensure that if b is MIN, we do not negate it. We just add -sgn() to both a and b,
            # that does not change the result.
            dup, sgn, negate, add,
            # a b a b-sgn(b)
            negate,
            # a b a -(b-sgn(b))
            roll(2, 1),
            # a b -(b-sgn(b)) a
            dup, sgn, negate, add,
            # a b -(b-sgn(b)) a-sgn(a)
            add,  # a and b have the same sign -> this is never more than 2**63-1
            # a b a-b
            sgn,
            # a b sgn(a-b)
        ],
        otherwise=[
            sgn,
            # a b sgn(sgn(a)-sgn(b))
        ]
    ),
    # a b sgn(a-b)
    pop2,
    pop2
])
"""A comparison of two values on the stack. Handles all i64 values.
a > b => 1
a < b => -1
a == b => 0

Signature: a b => sgn(a-b)
"""

 */

/** A comparison of two values on the stack. Handles all i64 values.
 * a > b => 1
 * a < b => -1
 * a == b => 0
 *
 * Signature: a b => sgn(a-b)
 */
fun ComplexBlock.cmp() = complexFunction {
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
    } orIfNonZero {
        sgn()
        // a b sgn(sgn(a)-sgn(b))
    }
    // a b sgn(a-b)
    pop2()
    pop2()
}

