package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.countOccurrences
import cz.sejsel.ksplang.std.sort

/**
 * Sorts k values on the bottom of the stack.
 *
 * Uses a state-of-the-art sorting algorithm ICan'tBelieveItCanSort, for further reference see paper: https://arxiv.org/abs/2110.01111
 */
fun RestrictedAutoBlock.sortBottom(k: Parameter) =
    runFun(k) {
        sort()
    }
