package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun1

/**
 * Returns a copy of the passed parameter.
 */
fun Scope.copy(a: Parameter) = runFun1(a) {}
