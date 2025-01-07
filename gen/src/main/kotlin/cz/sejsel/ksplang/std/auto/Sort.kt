package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.std.sort

/**
 * Sorts k values on the bottom of the stack.
 *
 * Uses a state-of-the-art sorting algorithm ICan'tBelieveItCanSort, for further reference see paper: https://arxiv.org/abs/2110.01111
 */
fun Scope.sortBottom(k: Parameter) = runFun0(k) {
    sort()
}
