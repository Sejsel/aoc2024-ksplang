package cz.sejsel.ksplang.dumper

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.*
import java.io.File

fun main(args: Array<String>) {
    // Alloc
    dump("allocNoReturn") { allocNoReturn() }
    dump("alloc") { alloc() }
    dump("allocNoReturnConstLen50") { allocNoReturnConstLen(50) }
    dump("allocNoReturnConstLen1000") { allocNoReturnConstLen(1000) }
    // Bool
    dump("and") { and() }
    dump("or") { or() }
    dump("not") { not() }
    // Cmp
    dump("cmp") { cmp() }
    dump("isInRange") { isInRange() }
    dump("geq") { geq() }
    dump("gt") { gt() }
    dump("leq") { leq() }
    dump("lt") { lt() }
    // Dup
    dump("dup") { dup() }
    dump("dupAb") { dupAb() }
    dump("dupSecond") { dupSecond() }
    dump("dupThird") { dupThird() }
    dump("dupFourth") { dupFourth() }
    dump("dupFifth") { dupFifth() }
    dump("dupNth") { dupNth() }
    // Math
    dump("add") { add() }
    dump("sub") { sub() }
    dump("subabs") { subabs() }
    dump("mul") { mul() }
    dump("cursedDiv") { cursedDiv() }
    dump("div") { div() }
    dump("negate") { negate() }
    dump("sgn") { sgn() }
    dump("abs") { abs() }
    dump("zeroNot") { zeroNot() }
    dump("zeroNotPositive") { zeroNotPositive() }
    dump("dec") { dec() }
    dump("decPositive") { decPositive() }
    dump("min2") { min2() }
    dump("isMin") { isMin() }
    dump("isMinRaw") { isMinRaw() }
    dump("bitor") { bitor() }
    dump("bitxor") { bitxor() }
    dump("bitnotMinUnsafe") { bitnotMinUnsafe() }
    dump("bitnot") { bitnot() }
    // Push
    dump("push42") { push(42) }
    dump("pushOn8_42") { pushOn(8, 42) }
    dump("pushPaddedTo42_100") { pushOn(42, 100) }
    dump("pushManyBottom42_10") { pushManyBottom(42, 10) }
    dump("pushMany42") { pushMany(42) }
    dump("pushManyAndKeepLen42") { pushManyAndKeepLen(42) }
    // Slices
    dump("countOccurrences") { countOccurrences() }
    dump("yoinkSlice") { yoinkSlice() }
    dump("copySlice") { copySlice() }
    // Sort
    dump("sort") { sort() }
    // Stack
    dump("roll4_1") { roll(4, 1) }
    dump("swap2") { swap2() }
    dump("stacklen") { stacklen() }
    dump("stacklenWithMin") { stacklenWithMin() }
    dump("yoink") { yoink() }
    dump("yoinkDestructive") { yoinkDestructive() }
    dump("yeet") { yeet() }
    dump("leaveTop") { leaveTop() }
    dump("popMany") { popMany() }
    dump("popNth") { popNth() }
    dump("pop3") { pop3() }
    dump("pop4") { pop4() }
    dump("moveNthToTop") { moveNthToTop() }
    dump("setNth") { setNth() }
    dump("permuteABCD_DACB") { permute("A B C D", "D A C B") }
    dump("findUnsafe") { findUnsafe() }
    // Text
    dump("parseNonNegativeNum_space") { parseNonNegativeNum(' '.code) }
    dump("parseNonNegativeNum2") { parseNonNegativeNum2() }
    dump("parseNonNegativeNumInRange") { parseNonNegativeNumInRange() }
    // Transform
    dump("map_149") { map(listOf(1, 4, 9)) }
}

val builder = KsplangBuilder()

fun dump(name: String, block: ComplexFunction.() -> Unit) {
    val program = buildComplexFunction {
        block()
    }
    val ksplang = builder.buildAnnotated(program)
    File("$name.ksplang").writeText(ksplang.toRunnableProgram())
}
