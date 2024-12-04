package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.doWhileZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.orIfNonZero


/**
 * Rotate the top `length` elements of the stack by `distance` right (towards stack top).
 */
fun Block.roll(length: Long, distance: Long): SimpleFunction = function("roll($length, $distance)") {
    // TODO: Optimize -1 case, use it when it's shorter and vice versa
    push(distance)
    push(length)
    lroll()
}

/**
 * Swap the top two elements of the stack.
 */
fun Block.swap2() = roll(2, 1)

/**
 * Finds the length of the stack.
 *
 * Signature: `<stack> -> <stack> len(stack)`
 */
fun ComplexBlock.stacklen(): ComplexFunction = complexFunction("stacklen") {
    push(0)
    // 0
    // i
    doWhileZero {
        // i
        push(0)
        // i 0
        swap2()
        // 0 i
        dup()
        dup()
        // 0 i i i
        CS()
        // 0 i i i X
        swap2()
        // 0 i i X i
        swap()
        // 0/X i i s[i]
        // if dup_fourth is not zero, we found the end.
        // we can do a "cs_fourth" instead even though it replaces the 0/X with its CS
        // - this replaces the 0? - zero/X with its CS, and we will be checking the zero/X
        // - this only works because we know the stack is not empty (CS of correct i is not zero)
        roll(4, 3)
        // i i s[i] 0/X
        CS()
        // i i s[i] 0/X CS(0/X)
        roll(5, 1)
        // CS(0/X) i i s[i] 0/X
        roll(3, 1)
        // CS(0/X) i 0/X i s[i]
        swap2()
        // CS(0/X) i 0/X s[i] i
        swap()
        // 0 i 0/X X/CS(X)
        pop()
        // 0 i 0/X
        roll(3, 1)
        // 0/X 0 i
        inc()
        // 0/X 0 i+1
        pop2()
        // 0/X i+1
        swap2()
        // i+1 0?
    }
    decPositive()
}

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Does not destroy the value.
 *
 * Fairly expensive, if the value can be destroyed, use [yoinkDestructive] instead
 *
 * Signature: ```n -> s[n]; s[n] stays s[n]```
 */
fun ComplexBlock.yoink(): ComplexFunction = complexFunction("yoink") {
    // n
    dup()
    // We need to push something there temporarily,
    // so we just use CS as that is efficient.
    CS()
    // n n CS(n)
    roll(2, 1)
    // n CS(n) n
    swap()
    // n s[n]
    dup()
    // n s[n] s[n]
    roll(3, 2)
    // s[n] s[n] n
    swap()
    // s[n] CS(n)
    pop()
    // s[n]
}

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Destroys the value
 * (leaves a random value in its place - the digit sum of the index).
 *
 * Cheaper than [yoink] if the value can be destroyed.
 *
 * Signature: ```n -> s[n]; s[n] is removed```
 */
fun ComplexBlock.yoinkDestructive(): ComplexFunction = complexFunction("yoinkDestructive") {
    // n
    CS()
    // n X
    swap2()
    // X n
    swap()
    // s[n] = CS(n)
    // s[n]
}

/**
 * Yeets a value from the top of the stack to a specific index of the stack (inverse of [yoink]).
 *
 * Signature: ```x i -> <nothing>; stack[i] = x```
 */
fun Block.yeet(): SimpleFunction = function("yeet") {
    swap()
    pop()
}

/** Clears the entire stack, leaving only the top value. */
fun ComplexBlock.leaveTop() = complexFunction {
    // [stack]
    stacklen()
    // [stack] stacklen
    push(1); swap2()
    // [stack|top] 1 stacklen
    lroll()
    // [top|stack]
    stacklen()
    // [top|stack] stacklen
    dec()
    doWhileNonZero {
        dec()
        // [top|stack] i-1
        pop2()
        // [top|stack] i
        CS()
    }
    pop()
    // [stack]
}

/**
 * With n on top of the stack, pops n elements from the stack (not counting n), n is also consumed.
 *
 * e.g. `1 2 3 4 2 -> 1 2` (*n* was 2)
 */
fun ComplexBlock.popN() = complexFunction {
    ifZero {
        pop()
    } orIfNonZero {
        doWhileNonZero {
            pop2()
            dec()
            CS()
        }
        pop()
    }
}
