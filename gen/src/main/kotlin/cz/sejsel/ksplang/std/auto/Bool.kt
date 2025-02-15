package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.*
import cz.sejsel.ksplang.dsl.core.bitand

/**
 * Logical AND on two "boolean" values. Handles any values, unlike [bitand].
 *
 * Booleans: 0 is false, anything else is true.
 * */
fun Scope.and(a: Parameter, b: Parameter) = runFun1(a, b) {
    and()
}

/** Logical OR on two "boolean" values. Handles any values, unlike [bitand].
 *
 * Booleans: 0 is false, anything else is true.
 * */
fun Scope.or(a: Parameter, b: Parameter) = runFun1(a, b) {
    or()
}

/**
 * Logical NOT on a "boolean" value.
 *
 * Booleans: 0 is false, anything else is true.
 */
fun Scope.not(a: Parameter) = runFun1(a) {
    not()
}