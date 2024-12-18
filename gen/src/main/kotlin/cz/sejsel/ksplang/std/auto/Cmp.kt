package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.*

/** A comparison of two values. Handles all i64 values.
 *
 * - `a > b` → 1
 * - `a < b` → -1
 * - `a = b` → 0
 *
 * Signature: a b => sgn(a-b)
 */
fun RestrictedAutoBlock.cmp(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, b, useResult) {
        cmp()
    }

/**
 * Checks whether a number *x* is in a given range (inclusive start and end).
 * Result is 1 if the number is in the range, 0 otherwise.
 *
 * Signature: x from to -> 1 if x is in [from, to], 0 otherwise
 */
fun RestrictedAutoBlock.isInRange(x: Variable, from: Variable, to: Variable, useResult: CallResult1.() -> Unit) =
    runFun(x, from, to, useResult) {
        isInRange()
    }
