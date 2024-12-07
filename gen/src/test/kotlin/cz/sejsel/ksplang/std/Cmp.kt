package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual

class CmpTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val pairs = listOf(
        0L to 0L,
        1L to -1L,
        -1L to 0L,
        0L to 1L,
        42L to 15L,
        Long.MAX_VALUE to Long.MAX_VALUE,
        Long.MIN_VALUE to Long.MIN_VALUE,
        Long.MIN_VALUE to Long.MAX_VALUE,
        Long.MAX_VALUE to Long.MIN_VALUE,
    )

    context("cmp") {
        withData(pairs) { (a, b) ->
            val expectedResult = if (a > b) 1L else if (a < b) -1L else 0L
            val program = builder.build(buildComplexFunction { cmp() })
            runner.run(program, listOf(a, b)) shouldContainExactly listOf(expectedResult)
        }
    }
})

class IsInRangeTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val pairs = listOf<Pair<Long, LongRange>>(
        0L to 0L..10L,
        10L to 0L..10L,
        5L to 0L..10L,
        -1L to 0L..10L,
        -5L to 0L..10L,
        15L to 0L..10L,
        11L to 0L..10L,
        1L to -1L..0L,
        Long.MAX_VALUE to Long.MIN_VALUE..Long.MAX_VALUE,
        Long.MIN_VALUE to Long.MIN_VALUE..Long.MAX_VALUE,
        0L to Long.MIN_VALUE..Long.MAX_VALUE,
        42L to 42L..42L,
        42L to 50L..30L,
    )

    context("is in range") {
        withData(nameFn = { it.toString() }, pairs) { (n, range) ->
            val program = builder.build(buildComplexFunction { isInRange() })
            val output = runner.run(program, listOf(n, range.start, range.endInclusive))
            output shouldHaveSize 1
            output[0] shouldBeEqual if (range.contains(n)) 1L else 0L
        }
    }
})
