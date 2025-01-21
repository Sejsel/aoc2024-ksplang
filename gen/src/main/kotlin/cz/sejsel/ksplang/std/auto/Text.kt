package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun2
import cz.sejsel.ksplang.std.*

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parseNegativeNum(' '.code) will parse a number that is followed by a space.
 *
 * Signature: i -> number index_of_terminator
 *
 * Value 1: number
 * Value 2: index_of_terminator
 */
fun Scope.parseNonNegativeNum(indexFrom: Parameter, terminator: Parameter, base: Int = 10) =
    when (terminator) {
        is Constant -> runFun2(indexFrom) { parseNonNegativeNum(terminator.value, base = base) }
        is Variable -> runFun2(indexFrom, terminator) { parseNonNegativeNum2(base = base) }
    }

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parseNegativeNum(' '.code) will parse a number that is followed by a space.
 *
 * Signature: i -> number index_of_terminator
 *
 * Value 1: number
 * Value 2: index_of_terminator
 */
fun Scope.parseNonNegativeNum(indexFrom: Parameter, terminator: Long, base: Int = 10) =
    parseNonNegativeNum(indexFrom, const(terminator), base)

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parseNegativeNum(' '.code) will parse a number that is followed by a space.
 *
 * Signature: i -> number index_of_terminator
 *
 * Value 1: number
 * Value 2: index_of_terminator
 */
fun Scope.parseNonNegativeNum(indexFrom: Parameter, terminator: Int, base: Int = 10) =
    parseNonNegativeNum(indexFrom, terminator.toLong(), base)