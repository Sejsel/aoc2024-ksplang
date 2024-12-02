package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.complexFunction
import cz.sejsel.ksplang.dsl.core.doWhileZero


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
