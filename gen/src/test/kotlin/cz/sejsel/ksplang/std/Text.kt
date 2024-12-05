package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.streams.toList

class ParseNumTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val pairs = listOf<Pair<String, Long>>(
        "0 " to 0,
        "100 " to 100,
        "123 " to 123,
        "1234567890 " to 1234567890,
    )
    val terminator = ' '
    val program = builder.build(buildComplexFunction { parseNonNegativeNum(terminator.code) })

    context("parseNumTests, terminator space, base 10, from start") {
        withData(pairs) { (text, num) ->
            val textCodepoints = text.codePoints().toList().map { it.toLong() }

            val input = textCodepoints + listOf<Long>(0)
            val expectedResult = textCodepoints + listOf<Long>(num, text.indexOf(terminator).toLong())

            runner.run(program, input) shouldContainExactly expectedResult
        }
    }
})
