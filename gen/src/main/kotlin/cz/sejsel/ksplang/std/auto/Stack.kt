package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.std.*

/**
 * Counts how many values are on the stack.
 * Works with any int64 values on the stack.
 */
fun Scope.stacklen() = runFun1 {
    stacklen()
}

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Does not destroy the value.
 *
 * Fairly expensive, if the value can be destroyed, use [yoinkDestructive] instead
 */
fun Scope.yoink(index: Parameter) = runFun1(index) {
    yoink()
}

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Destroys the value
 * (leaves a random value in its place - the digit sum of the index).
 *
 * Cheaper than [yoink] if the value can be destroyed.
 *
 */
fun Scope.yoinkDestructive(index: Parameter) = runFun1(index) {
    yoinkDestructive()
}

/**
 * Yeets a value to a specific index of the stack (inverse of [yoink]).
 */
fun Scope.yeet(index: Parameter, value: Parameter) = runFun0(value, index) {
    yeet()
}

/**
 * Finds the first instance of a value from a given index (inclusive) and returns the index.
 * If the [value] does not exist, this will behave unpredictably (crash or wrong results).
 * If [indexFrom] is outside of the stack, this will crash.
 */
fun Scope.findUnsafe(indexFrom: Parameter, value: Parameter) = runFun1(indexFrom, value) {
    findUnsafe()
}

