package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.*

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parseNegativeNum(' '.code) will parse a number that is followed by a space.
 *
 * Signature: i -> number index_of_terminator
 */
fun RestrictedAutoBlock.parseNonNegativeNum(indexFrom: Parameter, terminator: Constant, useResult: CallResult1.() -> Unit) =
    runFun(indexFrom, useResult) {
        parseNonNegativeNum(terminator.value, base = 10)
    }
