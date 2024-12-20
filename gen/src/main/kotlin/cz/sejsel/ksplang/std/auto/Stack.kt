package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.*

/**
 * Sorts k values on the bottom of the stack.
 *
 * Uses a state-of-the-art sorting algorithm ICan'tBelieveItCanSort, for further reference see paper: https://arxiv.org/abs/2110.01111
 */
fun RestrictedAutoBlock.stacklen(useResult: CallResult1.() -> Unit) =
    runFun(useResult) {
        stacklen()
    }

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Does not destroy the value.
 *
 * Fairly expensive, if the value can be destroyed, use [yoinkDestructive] instead
 */
fun RestrictedAutoBlock.yoink(index: Parameter, useResult: CallResult1.() -> Unit) =
    runFun(index, useResult) {
        yoink()
    }

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Destroys the value
 * (leaves a random value in its place - the digit sum of the index).
 *
 * Cheaper than [yoink] if the value can be destroyed.
 *
 */
fun RestrictedAutoBlock.yoinkDestructive(index: Parameter, useResult: CallResult1.() -> Unit) =
    runFun(index, useResult) {
        yoinkDestructive()
    }

/**
 * Yeets a value from to a specific index of the stack (inverse of [yoink]).
 */
fun RestrictedAutoBlock.yeet(index: Parameter, value: Parameter) =
    runFun(value, index) {
        yeet()
    }

/**
 * Finds the first instance of a value from a given index (inclusive).
 * If the [value] does not exist, this will behave unpredictably (crash or wrong results).
 * If [indexFrom] is outside of the stack, this will crash.
 */
fun RestrictedAutoBlock.findUnsafe(indexFrom: Parameter, value: Parameter, useResult: CallResult1.() -> Unit) =
    runFun(indexFrom, value, useResult) {
        findUnsafe()
    }

