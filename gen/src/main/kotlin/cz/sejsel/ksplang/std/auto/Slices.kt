package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.countOccurrences

data class Slice(
    val from: Parameter,
    val len: Parameter
)

/**
 * Given a slice, count the number of occurrences of a number.
 */
fun RestrictedAutoBlock.countOccurrences(value: Parameter, slice: Slice, useResult: CallResult1.() -> Unit) =
    runFun(value, slice.from, slice.len, useResult) {
        countOccurrences()
    }
