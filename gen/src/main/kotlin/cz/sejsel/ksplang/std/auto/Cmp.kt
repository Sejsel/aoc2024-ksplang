package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.*

/** A comparison of two values. Handles all i64 values.
 *
 * - `a > b` → 1
 * - `a < b` → -1
 * - `a = b` → 0
 *
 * Signature: a b => sgn(a-b)
 */
fun Scope.cmp(a: Parameter, b: Parameter) = runFun1(a, b) {
    cmp()
}

/**
 * Checks whether a number *x* is in a given range (inclusive start and end).
 * Result is 1 if the number is in the range, 0 otherwise.
 *
 * Signature: x from to -> 1 if x is in [from, to], 0 otherwise
 */
fun Scope.isInRange(x: Parameter, from: Parameter, to: Parameter) = runFun1(x, from, to) {
    isInRange()
}

/**
 * Returns 1 if the two values are equal, 0 otherwise.
 */
fun Scope.eq(a: Parameter, b: Parameter) = runFun1(a, b) {
    cmp()
    zeroNot()
}

/**
 * Returns 1 if the two values are equal, 0 otherwise.
 */
fun Scope.eq(a: Parameter, b: Long) = eq(a, const(b))

/**
 * Returns 1 if the two values are equal, 0 otherwise.
 */
fun Scope.eq(a: Parameter, b: Int) = eq(a, const(b))
