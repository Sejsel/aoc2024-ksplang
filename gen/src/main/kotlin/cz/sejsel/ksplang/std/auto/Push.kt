package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.std.*

/**
 * Pushes `count` times `value` to the bottom of the stack.
 */
fun Scope.pushManyBottom(value: Constant, count: Constant) = runFun0 {
    pushManyBottom(value.value, count.value)
}
