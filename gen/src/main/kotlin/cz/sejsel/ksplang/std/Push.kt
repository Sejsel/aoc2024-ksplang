package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.Instruction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.extract
import cz.sejsel.ksplang.dsl.core.whileNonZero
import java.util.zip.GZIPInputStream
import kotlin.math.abs

object ShortPushes {
    val sequencesByNumber: Map<Long, List<Instruction>> = GZIPInputStream(this::class.java.getResourceAsStream("/short_pushes.txt.gz")).bufferedReader()
        .readText()
        .lines()
        .map { it.trim() }
        .filter { !it.startsWith("#") && !it.isBlank() }
        .associate {
            val number = it.split(" ")[0].trim().toLong()
            val program = it.substringAfter(' ').trim()
            val instructions = program.split(" ").map { Instruction.fromText(it)!! }
            number to instructions
        }

    /** Programs by  **/
    val pushOnSequencesByNumbers: Map<Pair<Long, Long>, List<Instruction>> = this::class.java.getResourceAsStream("/short_push_ons.txt")?.bufferedReader()!!
        .readText()
        .lines()
        .map { it.trim() }
        .filter { !it.startsWith("#") && !it.isBlank() }
        .associate {
            val numbers = it.split(":")[0].trim().removePrefix("(").removeSuffix(")").split(",").map { it.trim().toLong() }
            check(numbers.size == 2)
            val on = numbers[0]
            val number = numbers[1]
            val program = it.split(":")[1].split("'")[1].trim()
            val instructions = program.split(" ").map { Instruction.fromText(it)!! }
            (on to number) to instructions
        }
}


/** Pushes a constant number to the top of the stack. */
fun Block.push(n: Int) = push(n.toLong())

/** Pushes a constant number to the top of the stack. */
fun Block.push(n: Long): SimpleFunction {
    return function("push($n)") {
        ShortPushes.sequencesByNumber[n]?.let {
            for (instruction in it) {
                addChild(instruction)
            }
            return@function
        }

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
        } else if (n > 0 && (n + 1) in ShortPushes.sequencesByNumber) {
            push(n + 1)
            dec()
        } else if (n > 0) {
            val bitLength = Long.SIZE_BITS - n.countLeadingZeroBits()
            var numsToAdd = 0

            for (i in 0 until bitLength) {
                if (n and (1L shl i) != 0L) {
                    push(1)
                    if (i == 1) {
                        // We can duplicate the 1 by using m or CS
                        CS()
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
        } else if (n == Long.MIN_VALUE) {
            push(-1)
            push(63)
            bitshift()
        } else {
            push(abs(n))
            negate()
        }
    }
}

/**
 * Pushes a constant number to the top of the stack if there is a specific value on top of the stack already.
 *
 * Generally more useful as an optimization for generating ksplang code than being used manually.
 */
fun Block.pushOn(stackTop: Long, n: Long): SimpleFunction = function("pushOn($stackTop, $n)") {
    ShortPushes.pushOnSequencesByNumbers[stackTop to n]?.let {
        for (instruction in it) {
            addChild(instruction)
        }
        return@function
    }

    push(n)
}

/**
 * Pushes a constant number to the top of the stack if there is a specific value on top of the stack already.
 *
 * Generally more useful as an optimization for generating ksplang code than being used manually.
 */
fun Block.pushOn(stackTop: Long, n: Int): SimpleFunction = pushOn(stackTop, n.toLong())

/**
 * Pushes a constant number to the top of the stack, using the given number of operations exactly.
 * @throws PaddingFailureException if we don't have a short enough push to fit into the opCount.
 */
fun Block.pushPaddedTo(n: Long, opCount: Int) = function("pushPaddedTo($n, $opCount)") {
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

/**
 * Pushes `count` times `n` to the bottom of the stack.
 */
fun ComplexBlock.pushManyBottom(n: Long, count: Long) = complexFunction("pushBottomMany($n, $count)") {
    // [stack]
    pushMany(n, count)
    // [stack] [count * n]
    stacklen()
    push(count)
    swap2()
    // [stack] [count * n] count stacklen
    lroll()
    // [count * n] [stack]
}

/**
 * Pushes `count` times `n` to the top of the stack.
 *
 * Does so using a loop, not *count* push instructions. This keeps the instruction count down which means that
 * later parts of the program will needed lower constants for branching.
 *
 * Signature: count -> [count * n]
 */
fun ComplexBlock.pushMany(n: Long) = complexFunction("pushMany($n)") {
    // count
    doWhileNonZero {
        // count
        push(n)
        // count n
        swap2()
        // n count
        dec()
        // n count-1
        CS()
    }
    // [count * n] 0
    pop()
}

/**
 * Pushes `count` times `n` to the top of the stack. Also keeps the count on top of the stack.
 *
 * Signature: count -> [count * n] count
 */
fun ComplexBlock.pushManyAndKeepLen(n: Long) = complexFunction("pushManyAndKeepLen($n)") {
    // count
    dup()
    // count count
    // count i
    whileNonZero {
        // count i
        push(n)
        // count i n
        roll(3, 1)
        // n count i
        dec()
        // n count i-1
    }
    // [count * n] count
}


/**
 * Pushes `count` times `n` to the top of the stack.
 *
 * Does so using a loop, not *count* push instructions. This keeps the instruction count down which means that
 * later parts of the program will needed lower constants for branching.
 */
fun ComplexBlock.pushMany(n: Long, count: Long) = complexFunction("pushMany($n, $count)") {
    check(count >= 0) { "count must be non-negative" }

    if (count == 0L) return@complexFunction

    push(count)
    // count
    pushMany(n)
}

class PaddingFailureException(opCount: Int, minOpCount: Int) :
    Exception("Failed to pad push to $opCount instructions, requires at least $minOpCount instructions.")