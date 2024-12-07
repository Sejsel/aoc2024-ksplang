package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.orIfNonZero

/**
 * Given a slice, count the number of occurrences of a number
 *
 * Signature: `of from len -> count` where count is the amount of numbers of occurrs in stack slice [from,from+len)
 */
fun ComplexBlock.countOccurrences() = complexFunction("countOccurrences") {
    // of from len
    push(0)
    // of from len 0
    doWhileNonZero {
        // of from i+1 total
        permute("of from i total", "total of from i")
        // total of from i+1
        dec()
        dupAb()
        // total of from i from i
        add()
        yoink()
        // total of from i s[from+i]
        // total of from i val
        permute("total of from i val", "total from i of val")
        // total from i of val
        dupSecond()
        // total from i of val of
        cmp()
        // total from i of cmp(val,of)
        zeroNot()
        // total from i of 1/0
        permute("total from i of 1/0", "from i of 1/0 total")
        // from i of 1/0 total
        add()
        // from i of total
        permute("from i of total", "of from i total")
        // of from i total
        dupSecond()
        // of from i total i
    }
    // of from 0 total
    pop2()
    pop2()
    pop2()
    // total
}

/**
 * Yoinks a slice nondestructively.
 * That is, performs a copy of a slice onto the top of the stack.
 * The length is kept at the top (otherwise this would be unusable in most cases).
 *
 * Length must be non-negative (0 is fine, the result is just 0).
 *
 * Signature `from len -> s[from:from+len) len
 */
fun ComplexBlock.yoinkSlice() = complexFunction("yoinkSlice") {
    // from len
    ifZero {
        pop2()
        // len
    } orIfNonZero {
        // from len
        swap2()
        // len from
        dupSecond()
        // len from len
        add()
        dec()
        // len from+len-1
        // len to
        dupSecond()
        // len to len
        doWhileNonZero {
            // [copy] len to i+1
            dec()
            // [copy] len to i
            dup()
            // [copy] len to i i
            dupThird()
            // [copy] len to i i to
            subabs()
            // [copy] len to i to-i
            yoink()
            // [copy] len to i s[from+i]
            // [copy] len to i val
            permute("len to i val", "val len to i")
            // [copy|val] len to i
            CS()
        }
        // [copy] len to 0
        pop()
        pop()
        // [copy] len
    }
}

