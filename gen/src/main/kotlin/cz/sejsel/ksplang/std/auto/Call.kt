package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.dsl.auto.runFun2
import cz.sejsel.ksplang.dsl.auto.runFun3
import cz.sejsel.ksplang.dsl.auto.runFun4
import cz.sejsel.ksplang.dsl.auto.runFun5
import cz.sejsel.ksplang.dsl.auto.runFun6
import cz.sejsel.ksplang.dsl.auto.runFun7
import cz.sejsel.ksplang.dsl.auto.runFun8
import cz.sejsel.ksplang.dsl.core.*

fun Scope.call(function: ProgramFunction0To0) = runFun0 { call(function) }
fun Scope.call(function: ProgramFunction0To1) = runFun1 { call(function) }
fun Scope.call(function: ProgramFunction0To2) = runFun2 { call(function) }
fun Scope.call(function: ProgramFunction0To3) = runFun3 { call(function) }
fun Scope.call(function: ProgramFunction0To4) = runFun4 { call(function) }
fun Scope.call(function: ProgramFunction0To5) = runFun5 { call(function) }
fun Scope.call(function: ProgramFunction0To6) = runFun6 { call(function) }
fun Scope.call(function: ProgramFunction0To7) = runFun7 { call(function) }
fun Scope.call(function: ProgramFunction0To8) = runFun8 { call(function) }

fun Scope.call(function: ProgramFunction1To0, a: Parameter) = runFun0(a) { call(function) }
fun Scope.call(function: ProgramFunction1To1, a: Parameter) = runFun1(a) { call(function) }
fun Scope.call(function: ProgramFunction1To2, a: Parameter) = runFun2(a) { call(function) }
fun Scope.call(function: ProgramFunction1To3, a: Parameter) = runFun3(a) { call(function) }
fun Scope.call(function: ProgramFunction1To4, a: Parameter) = runFun4(a) { call(function) }
fun Scope.call(function: ProgramFunction1To5, a: Parameter) = runFun5(a) { call(function) }
fun Scope.call(function: ProgramFunction1To6, a: Parameter) = runFun6(a) { call(function) }
fun Scope.call(function: ProgramFunction1To7, a: Parameter) = runFun7(a) { call(function) }
fun Scope.call(function: ProgramFunction1To8, a: Parameter) = runFun8(a) { call(function) }

fun Scope.call(function: ProgramFunction2To0, a: Parameter, b: Parameter) = runFun0(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To1, a: Parameter, b: Parameter) = runFun1(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To2, a: Parameter, b: Parameter) = runFun2(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To3, a: Parameter, b: Parameter) = runFun3(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To4, a: Parameter, b: Parameter) = runFun4(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To5, a: Parameter, b: Parameter) = runFun5(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To6, a: Parameter, b: Parameter) = runFun6(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To7, a: Parameter, b: Parameter) = runFun7(a, b) { call(function) }
fun Scope.call(function: ProgramFunction2To8, a: Parameter, b: Parameter) = runFun8(a, b) { call(function) }

fun Scope.call(function: ProgramFunction3To0, a: Parameter, b: Parameter, c: Parameter) = runFun0(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To1, a: Parameter, b: Parameter, c: Parameter) = runFun1(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To2, a: Parameter, b: Parameter, c: Parameter) = runFun2(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To3, a: Parameter, b: Parameter, c: Parameter) = runFun3(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To4, a: Parameter, b: Parameter, c: Parameter) = runFun4(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To5, a: Parameter, b: Parameter, c: Parameter) = runFun5(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To6, a: Parameter, b: Parameter, c: Parameter) = runFun6(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To7, a: Parameter, b: Parameter, c: Parameter) = runFun7(a, b, c) { call(function) }
fun Scope.call(function: ProgramFunction3To8, a: Parameter, b: Parameter, c: Parameter) = runFun8(a, b, c) { call(function) }

fun Scope.call(function: ProgramFunction4To0, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun0(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To1, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun1(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To2, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun2(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To3, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun3(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To4, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun4(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To5, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun5(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To6, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun6(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To7, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun7(a, b, c, d) { call(function) }
fun Scope.call(function: ProgramFunction4To8, a: Parameter, b: Parameter, c: Parameter, d: Parameter) = runFun8(a, b, c, d) { call(function) }

fun Scope.call(function: ProgramFunction5To0, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun0(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To1, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun1(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To2, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun2(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To3, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun3(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To4, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun4(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To5, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun5(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To6, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun6(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To7, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun7(a, b, c, d, e) { call(function) }
fun Scope.call(function: ProgramFunction5To8, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter) = runFun8(a, b, c, d, e) { call(function) }

fun Scope.call(function: ProgramFunction6To0, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun0(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To1, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun1(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To2, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun2(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To3, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun3(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To4, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun4(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To5, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun5(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To6, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun6(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To7, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun7(a, b, c, d, e, f) { call(function) }
fun Scope.call(function: ProgramFunction6To8, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter) = runFun8(a, b, c, d, e, f) { call(function) }

fun Scope.call(function: ProgramFunction7To0, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun0(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To1, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun1(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To2, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun2(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To3, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun3(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To4, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun4(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To5, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun5(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To6, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun6(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To7, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun7(a, b, c, d, e, f, g) { call(function) }
fun Scope.call(function: ProgramFunction7To8, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter) = runFun8(a, b, c, d, e, f, g) { call(function) }

fun Scope.call(function: ProgramFunction8To0, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun0(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To1, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun1(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To2, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun2(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To3, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun3(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To4, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun4(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To5, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun5(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To6, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun6(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To7, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun7(a, b, c, d, e, f, g, h) { call(function) }
fun Scope.call(function: ProgramFunction8To8, a: Parameter, b: Parameter, c: Parameter, d: Parameter, e: Parameter, f: Parameter, g: Parameter, h: Parameter) = runFun8(a, b, c, d, e, f, g, h) { call(function) }