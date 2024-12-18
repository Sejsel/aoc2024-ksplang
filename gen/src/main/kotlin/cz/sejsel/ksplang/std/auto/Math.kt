package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.*

/**
 * Adds two values (a + b). Crashes in case of overflow.
 */
fun RestrictedAutoBlock.add(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, b, useResult) {
        add()
    }

/**
 * Subtracts two values and returns the absolute value of the differencee: |a-b|.
 */
fun RestrictedAutoBlock.subabs(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, b, useResult) {
        subabs()
    }

/**
 * Multiplies the two values (a * b). Crashes in case of overflow.
 */
fun RestrictedAutoBlock.mul(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, b, useResult) {
        mul()
    }

/**
 * Divides the values or calculates the modulo if they are not cleanly divisible.
 *
 * `a % b == 0 ? a // b : a % b`
 */
fun RestrictedAutoBlock.cursedDiv(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(b, a, useResult) { // note the a, b swap
        cursedDiv()
    }

/**
 * Divides the values (a // b). Crashes with division by zero.
 */
fun RestrictedAutoBlock.div(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(b, a, useResult) { // note the a, b swap
        div()
    }


/**
 * Returns the minimum of the two values.
 */
fun RestrictedAutoBlock.min2(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, b, useResult) {
        min2()
    }

/**
 * Returns the maximum of the two values.
 */
fun RestrictedAutoBlock.max2(a: Variable, b: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, b, useResult) {
        max2()
    }

/**
 * Returns the negation of the value.
 */
fun RestrictedAutoBlock.negate(a: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        negate()
    }

/**
 * Negates the value.
 */
fun Variable.negate() {
    this.parentAutoBlock.negate(this) f@{
        setTo(this@negate)
    }
}

/**
 * Adds a constant to the value.
 */
fun RestrictedAutoBlock.add(a: Variable, const: Long, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        add(const)
    }

/**
 * Adds a constant to the value.
 */
fun Variable.add(const: Long) {
    this.parentAutoBlock.add(this, const) f@{
        setTo(this@add)
    }
}

/**
 * Multiplies the value by a constant.
 */
fun RestrictedAutoBlock.mul(a: Variable, const: Long, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        mul(const)
    }

/**
 * Multiplies the value by a constant.
 */
fun Variable.mul(const: Long) {
    this.parentAutoBlock.mul(this, const) f@{
        setTo(this@mul)
    }
}

/**
 * Divides the value by a constant.
 */
fun RestrictedAutoBlock.div(a: Variable, const: Long, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        div(const)
    }

/**
 * Divides the value by a constant.
 * Crashes with division by zero.
 */
fun Variable.div(const: Long) {
    this.parentAutoBlock.div(this, const) f@{
        setTo(this@div)
    }
}

/**
 * Returns the sign of the value.
 */
fun RestrictedAutoBlock.sgn(a: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        sgn()
    }

/**
 * Sets the value to its sign.
 */
fun Variable.sgn() {
    this.parentAutoBlock.sgn(this) f@{
        setTo(this@sgn)
    }
}

/**
 * Returns the absolute value of the value.
 */
fun RestrictedAutoBlock.abs(a: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        abs()
    }

/**
 * Sets the value to its absolute value.
 */
fun Variable.abs() {
    this.parentAutoBlock.abs(this) f@{
        setTo(this@abs)
    }
}

/**
 * Returns 1 if the value is zero, 0 otherwise. A negation of "zeroity".
 */
fun RestrictedAutoBlock.zeroNot(a: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        zeroNot()
    }

/**
 * Sets the value to 1 if it is zero, 0 otherwise. A negation of "zeroity".
 */
fun Variable.zeroNot() {
    this.parentAutoBlock.zeroNot(this) f@{
        setTo(this@zeroNot)
    }
}

/**
 * Increments the value.
 */
fun RestrictedAutoBlock.inc(a: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        inc()
    }

/**
 * Increments the value.
 */
fun Variable.inc() {
    this.parentAutoBlock.inc(this) f@{
        setTo(this@inc)
    }
}

/**
 * Decrements the value.
 */
fun RestrictedAutoBlock.dec(a: Variable, useResult: CallResult1.() -> Unit) =
    runFun(a, useResult) {
        dec()
    }

/**
 * Decrements the value.
 */
fun Variable.dec() {
    this.parentAutoBlock.dec(this) f@{
        setTo(this@dec)
    }
}
