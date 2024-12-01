package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.SimpleFunction

/**
 * Rotate the top `length` elements of the stack by `distance` right (towards stack top).
 *
 */
fun SimpleFunction.roll(length: Long, distance: Long): SimpleFunction = function("roll($length, $distance)") {
    // TODO: Optimize -1 case, use it when it's shorter and vice versa
    push(distance)
    push(length)
    lroll()
}