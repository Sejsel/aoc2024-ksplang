package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.*

/**
 * Adds two values (a + b). Crashes in case of overflow.
 */
fun Scope.add(a: Parameter, b: Parameter) = runFun1(a, b) {
    add()
}

/**
 * Subtracts two values (a - b). Crashes in case of overflow.
 */
fun Scope.sub(a: Parameter, b: Parameter) = runFun1(a, b) {
    sub()
}

/**
 * Subtracts two values and returns the absolute value of the differencee: |a-b|.
 */
fun Scope.subabs(a: Parameter, b: Parameter) = runFun1(a, b) {
    subabs()
}

/**
 * Multiplies the two values (a * b). Crashes in case of overflow.
 */
fun Scope.mul(a: Parameter, b: Parameter) = runFun1(a, b) {
    mul()
}

/**
 * Divides the values or calculates the modulo if they are not cleanly divisible.
 *
 * `a % b == 0 ? a // b : a % b`
 */
fun Scope.cursedDiv(a: Parameter, b: Parameter) = runFun1(b, a) { // note the a, b swap
    cursedDiv()
}

/**
 * Divides the values (a // b). Crashes with division by zero.
 */
fun Scope.div(a: Parameter, b: Parameter) = runFun1(b, a) { // note the a, b swap
    div()
}

/**
 * Calculates the euclidean modulo of the values (a % b). Crashes with division by zero.
 */
fun Scope.mod(a: Parameter, b: Parameter) = runFun1(b, a) { // note the a, b swap
    modulo()
}


/**
 * Returns the minimum of the two values.
 */
fun Scope.min2(a: Parameter, b: Parameter) = runFun1(a, b) {
    min2()
}

/**
 * Returns the maximum of the two values.
 */
fun Scope.max2(a: Parameter, b: Parameter) = runFun1(a, b) {
    max2()
}

/**
 * Adds a constant to the value.
 */
fun Scope.add(a: Parameter, const: Long) = runFun1(a) {
    add(const)
}

/**
 * Multiplies the value by a constant.
 */
fun Scope.mul(a: Parameter, const: Long) = runFun1(a) {
    mul(const)
}


/**
 * Divides the value by a constant.
 */
fun Scope.div(a: Parameter, const: Long) = runFun1(a) {
    div(const)
}

/**
 * Returns the sign of the value.
 */
fun Scope.sgn(a: Parameter) = runFun1(a) {
    sgn()
}

/**
 * Returns the absolute value of the value.
 */
fun Scope.abs(a: Parameter) = runFun1(a) {
    abs()
}

/**
 * Negates the value (a -> -a). Crashes for -2^63.
 */
fun Scope.negate(a: Parameter) = runFun1(a) {
    negate()
}

/**
 * Returns 1 if the value is zero, 0 otherwise. A negation of "zeroity".
 */
fun Scope.zeroNot(a: Parameter) = runFun1(a) {
    zeroNot()
}


/**
 * Increments the value.
 */
fun Scope.inc(a: Parameter) = runFun1(a) {
    inc()
}


/**
 * Decrements the value.
 */
fun Scope.dec(a: Parameter) = runFun1(a) {
    dec()
}


/**
 * Does a bitwise AND on the two values.
 */
fun Scope.bitand(a: Parameter, b: Parameter) = runFun1(a, b) {
    bitand()
}

/**
 * Does a bitwise OR on two values.
 */
fun Scope.bitor(a: Parameter, b: Parameter) = runFun1(a, b) {
    bitor()
}

fun Scope.bitnot(a: Parameter) = runFun1(a) {
    bitnot()
}

/**
 * a << b
 */
fun Scope.bitshift(a: Parameter, b: Parameter) = runFun1(a, b) {
    bitshift()
}

fun Scope.i10log(a: Parameter) = runFun1(a) {
    push(0)
    lensum()
}