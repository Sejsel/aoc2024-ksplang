package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.countOccurrences
import cz.sejsel.ksplang.std.copySlice

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

/**
 * Copies a slice to a location starting at a given index (len comes from the slice).
 * Make sure there is enough space in the destination.
 *
 * Goes from left to right, which is relevant in case of overlap.
 */
fun Scope.copySlice(slice: Slice, destinationIndex: Parameter) = runFun0(slice.from, destinationIndex, slice.len) {
    copySlice()
}