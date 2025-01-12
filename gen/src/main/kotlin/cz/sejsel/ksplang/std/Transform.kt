package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.SimpleFunction

/**
 * Maps *i* to `values[i]`. For example map([1, 4, 9]) will map 0 to 1, 1 to 4, 2 to 9.
 * Undefined behavior with negative values and values greater than the length of the array.
 *
 * Keep in mind that all of the values will be placed on the stack, so this is not efficient unless kept small.
 */
fun Block.map(values: List<Long>): SimpleFunction = function("map($values)") {
    // i
    for (value in values.reversed()) {
        push(value)
    }
    // i [values]
    roll(values.size + 1L, -1)
    // [values] i
    inc()
    // [values] i+1
    moveNthToTop()
    // [values except one] values[i]
    repeat(values.size - 1) {
        pop2()
    }
    // values[i]
}

