package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly

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