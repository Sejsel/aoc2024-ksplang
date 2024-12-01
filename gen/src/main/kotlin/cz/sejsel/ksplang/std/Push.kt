package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.SimpleFunction
import kotlin.math.abs

fun SimpleFunction.push(n: Long) {
    // TODO: Port "short pushes"
    if (n == 0L) {
        // Requires a non-empty stack.
        // CS CS lensum will take any value down to 0-5
        // CS duplicates it
        // funkcia turns two duplicates into 0
        // result = [CS, CS, lensum, CS, funkcia]
        CS()
        CS()
        lensum()
        CS()
        funkcia()
    } else if (n in 1..15) {
        // We must handle n == 1 here.
        // This is by no means optimal.
        push(0)
        repeat(n.toInt()) {
            inc()
        }
    } else if (n > 0) {
        val bitLength = Long.SIZE_BITS - n.countLeadingZeroBits()
        var numsToAdd = 0

        for (i in 0 until bitLength) {
            if (n and (1L shl i) != 0L) {
                push(1)
                if (i == 1) {
                    // We can duplicate the 1 by using m or CS
                    m()
                } else {
                    push(i.toLong())
                }
                bitshift()
                numsToAdd += 1
            }
        }

        repeat(numsToAdd - 1) {
            add()
        }
    } else {
        push(abs(n))
        negate()
    }
}
