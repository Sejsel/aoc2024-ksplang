package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.std.*

/**
 * Pushes `count` times `value` to the bottom of the stack.
 */
fun RestrictedAutoBlock.pushManyBottom(value: Constant, count: Constant) =
    runFun {
        pushManyBottom(value.value, count.value)
    }
