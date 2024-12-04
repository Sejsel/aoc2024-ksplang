package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.doWhileNonZero

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
