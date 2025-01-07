package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.bitand

/**
 * Logical AND on two "boolean" values. Handles any values, unlike [bitand].
 *
 * Booleans: 0 is false, anything else is true.
 *
 * Signature: a b -> !!a&!!b
 * */
fun Block.and() = function("AND") {
    // a b
    zeroNot()
    zeroNotPositive()
    // a !!b
    swap2()
    // !!b a
    zeroNot()
    zeroNotPositive()
    // !!b !!a
    bitand()
    // !!b&!!a
}
