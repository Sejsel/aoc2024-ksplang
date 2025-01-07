package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.countOccurrences

data class Slice(
    val from: Parameter,
    val len: Parameter
)

/**
 * Given a slice, count the number of occurrences of a number.
 */
fun Scope.countOccurrences(value: Parameter, slice: Slice) = runFun1(value, slice.from, slice.len) {
    countOccurrences()
}
