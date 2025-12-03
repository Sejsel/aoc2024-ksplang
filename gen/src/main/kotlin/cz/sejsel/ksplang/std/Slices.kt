package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.whileNonZero

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
    } otherwise {
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

/**
 * Copy from a slice to another slice (of the same len).
 * Goes from left to right (lowest index to higher index), which is relevant when the slices overlap.
 *
 * Signature: `fromIndex toIndex len ->`
 */
fun ComplexBlock.copySlice() = complexFunction("copySlice") {
    // from to len
    ifZero {
        // from to len
    } otherwise {
        // from to len
        swap2()
        dec()
        swap2()
        // from to-1 len
        permute("from to len", "to len from")
        // to-1 len from
        dec()
        // to-1 len from-1
        permute("to len from", "from to len")
        // from-1 to-1 len
        doWhileNonZero {
            // from to i+1
            dec()
            // from to i
            permute("from to i", "i from to")
            inc()
            // i from to+1
            swap2()
            inc()
            // i to+1 from+1
            dup()
            yoink()
            // i to+1 from+1 s[from+1]
            dupThird()
            // i to+1 from+1 s[from+1] to+1
            yeet()
            // i to+1 from+1
            permute("i to from", "from to i")
            CS()
            // from to i i
        }
        // from to 0
    }
    pop(); pop(); pop()
}

/**
 * Yoinks a slice nondestructively, skipping one element.
 * That is, performs a copy of a slice onto the top of the stack, except for the *gap*-th element (from the bottom).
 * The length is kept at the top (otherwise this would be unusable in most cases).
 *
 * `len` must be positive, `gap` has to be in range 0..len-1.
 *
 * For example a slice of `10 20 30 40 50` with `gap` 3 would result in `10 20 30 50` being copied.
 *
 * Signature: `from len gap -> s[from:from+gap) s[from+gap+1:from+len) len-1`
 */
fun ComplexBlock.yoinkSliceWithGap() = complexFunction("yoinkSliceWithGap") {
    // from len gap
    dupThird()
    // from len gap from
    swap2()
    // from len from gap
    yoinkSlice()
    // from len s[from:from+gap) gap
    dup(); inc(); inc(); inc()
    // from len s[from:from+gap) gap gap+3
    moveNthToTop()
    // len s[from:from+gap) gap from
    dupSecond(); inc(); inc(); inc()
    // len s[from:from+gap) gap from gap+3
    moveNthToTop()
    // s[from:from+gap) gap from len
    dupThird(); dupThird()
    // s[from:from+gap) gap from len gap from
    add(); inc()
    // s[from:from+gap) gap from len from+gap+1
    dupSecond()
    // s[from:from+gap) gap from len from+gap+1 len
    dupFifth()
    // s[from:from+gap) gap from len from+gap+1 len gap
    negate(); add(); dec()
    // s[from:from+gap) gap from len from+gap+1 len-gap
    // s[from:from+gap) gap from len from+gap+1 len-gap-1
    pop3()
    pop3()
    // s[from:from+gap) gap from+gap+1 len-gap-1
    yoinkSlice()
    // s[from:from+gap) gap s[from+gap+1:from+len) len-gap-1
    dup(); inc(); inc()
    // s[from:from+gap) gap s[from+gap+1:from+len) len-gap-1 len-gap+1
    moveNthToTop()
    // s[from:from+gap) s[from+gap+1:from+len) len-gap-1 gap
    add()
    // s[from:from+gap) s[from+gap+1:from+len) len-1
}

/**
 * Sets all elements of a slice to a given value.
 *
 * Signature: `from len val ->`
 */
fun ComplexBlock.setSlice() = complexFunction("setSliceTo") {
    // from len val
    swap2()
    // from val len
    // from val i
    whileNonZero {
        // from val i
        dec()
        // from val i-1
        dupThird()
        // from val i-1 from
        dupSecond()
        // from val i-1 from i-1
        add()
        // from val i-1 from+i-1
        dupThird()
        // from val i-1 from+i-1 val
        swap2()
        // from val i-1 val from+i-1
        yeet()
        // from val i-1
    }
    // from val
    pop(); pop()
}

/**
 * Finds max value in elements of a slice.
 *
 * Signature: `from len -> max`
 */
fun ComplexBlock.findSliceMax() = complexFunction("findSliceMax") {
    // from len
    push(Long.MIN_VALUE)
    swap2()
    // from MIN len
    // from val len
    whileNonZero {
        // i val len_remaining+1
        dec()
        // i val len_remaining
        dupThird()
        // i val len_remaining i
        yoink()
        // i val len_remaining s[i]
        roll(3, 2)
        // i len_remaining s[i] val
        max2()
        // i len_remaining max(s[i],val)
        swap2()
        // i max(s[i],val) len_remaining
        roll(3, 2)
        // max(s[i],val) len_remaining i
        inc()
        // max(s[i],val) len_remaining i+1
        roll(3, 1)
        // i+1 max(s[i],val) len_remaining
    }
    // i max(s[i],val)
    pop2()
}
