package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.*

/**
 * Maps index to `values[index]`. For example map([1, 4, 9]) will map index 0 to 1, 1 to 4, 2 to 9.
 * Undefined behavior with negative index values and index values greater or equal to the length of the array.
 *
 * Keep in mind that all of the values will be placed on the stack, so this is not efficient unless [values] is kept small.
 */
@JvmName("mapLong")
fun Scope.map(index: Parameter, values: List<Long>) = map(index, values.map { const(it) })

/**
 * Maps index to `values[index]`. For example map([1, 4, 9]) will map index 0 to 1, 1 to 4, 2 to 9.
 * Undefined behavior with negative index values and index values greater or equal to the length of the array.
 *
 * Keep in mind that all of the values will be placed on the stack, so this is not efficient unless [values] is kept small.
 */
@JvmName("mapInt")
fun Scope.map(index: Parameter, values: List<Constant>) = runFun1(index) {
    map(values.map { it.value })
}