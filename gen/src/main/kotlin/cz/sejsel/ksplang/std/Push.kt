package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.extract
import cz.sejsel.ksplang.dsl.core.function
import kotlin.math.abs

/** Pushes a constant number to the top of the stack. */
fun SimpleFunction.push(n: Int) = push(n.toLong())

/** Pushes a constant number to the top of the stack. */
fun SimpleFunction.push(n: Long): SimpleFunction {
    return function("push($n)") {
        // TODO: Port "short pushes"
        if (n == 0L) {
            // Requires a non-empty stack.
            // CS CS lensum will take any value down to 0-5
            // CS duplicates it
            // funkcia turns two duplicates into 0
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
}

/**
 * Pushes a constant number to the top of the stack, using the given number of operations exactly.
 * @throws PaddingFailureException if we don't have a short enough push to fit into the opCount.
 */
fun SimpleFunction.pushPaddedTo(n: Long, opCount: Int) = function("pushPaddedTo($n, $opCount)") {
    val f = extract { push(n) }
    val len = f.getInstructions().size

    // This cannot fit already.
    if (len > opCount) {
        throw PaddingFailureException(opCount, len)
    }

    // Add the push(n)
    +f

    if (len != opCount) {
        // We need to pad, we can only do that with 2 or more instructions (CS pop) or (CS [++ ...] pop)
        val padding = opCount - len
        if (padding == 1) {
            // We can only add one instruction, this is not satisfiable
            throw PaddingFailureException(opCount, len + 2)
        }

        CS()
        repeat(padding.toInt() - 2) {
            inc()
        }
        pop()
    }
}

class PaddingFailureException(opCount: Int, minOpCount: Int) :
    Exception("Failed to pad push to $opCount instructions, requires at least $minOpCount instructions.")