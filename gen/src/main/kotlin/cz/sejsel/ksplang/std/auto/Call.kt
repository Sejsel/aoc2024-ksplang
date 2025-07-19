package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.dsl.auto.runFun2
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To0
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To1
import cz.sejsel.ksplang.dsl.core.ProgramFunction0To2
import cz.sejsel.ksplang.dsl.core.ProgramFunction2To1
import cz.sejsel.ksplang.dsl.core.call

fun Scope.call(function: ProgramFunction0To0) = runFun0 {
    call(function)
}

fun Scope.call(function: ProgramFunction0To1) = runFun1 {
    call(function)
}

fun Scope.call(function: ProgramFunction0To2) = runFun2 {
    call(function)
}

fun Scope.call(function: ProgramFunction2To1, a: Parameter, b: Parameter) = runFun1(a, b) {
    call(function)
}
