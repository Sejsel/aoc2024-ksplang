package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.MutableVariable
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.VarSetter
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.std.countOccurrences
import cz.sejsel.ksplang.std.copySlice
import cz.sejsel.ksplang.std.setSlice


data class Slice(
    val from: Parameter,
    val len: Parameter
) {
    operator fun get(index: Parameter): SliceElement = SliceElement(this, index)
    operator fun get(index: Int): SliceElement = SliceElement(this, index.const)
    operator fun get(index: Long): SliceElement = SliceElement(this, index.const)
}

data class SliceElement(
    val slice: Slice,
    val index: Parameter
)

/**
 * Given a slice, count the number of occurrences of a number.
 */
fun Scope.countOccurrences(value: Int, slice: Slice) = countOccurrences(const(value), slice)

/**
 * Given a slice, count the number of occurrences of a number.
 */
fun Scope.countOccurrences(value: Long, slice: Slice) = countOccurrences(const(value), slice)

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

/**
 * Sets all elements of a slice to the same value.
 */
fun Scope.setSliceTo(slice: Slice, value: Parameter) = runFun0(slice.from, slice.len, value) {
    setSlice()
}

/**
 * Executes a block for each element in a slice.
 *
 * Any changes to the value will be reflected in the slice.
 */
fun Scope.sliceForEach(slice: Slice, block: Scope.(MutableVariable) -> Unit) {
    val index = copy(slice.from)
    val max = add(slice.from, slice.len)
    whileNonZero({ subabs(index, max) }) {
        val value = yoink(index)
        block(value)
        yeet(index, value)
        set(index) to inc(index)
    }
}


fun Scope.set(index: SliceElement): VarSetter {
    val variable = variable()
    return VarSetter(this, variable) {
        yeet(add(index.slice.from, index.index), variable)
    }
}
