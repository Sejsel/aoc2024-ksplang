package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly


class CountOccurrencesTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val inputs = listOf(
        4L to listOf<Long>(1, 2, 3, 4, 5, 6),
        4L to listOf<Long>(1, 2, 3, 0, 5, 6),
        4L to listOf<Long>(4, 2, 3, 0, 5, 6),
        4L to listOf<Long>(1, 2, 3, 0, 5, 4),
        4L to listOf<Long>(4, 2, 3, 0, 5, 4),
        4L to listOf<Long>(4, 4, 4, 4, 4, 4),
        4L to listOf<Long>(Long.MAX_VALUE, Long.MIN_VALUE, 4, 0),
        4L to listOf<Long>(4),
        Long.MAX_VALUE to listOf<Long>(Long.MAX_VALUE, Long.MIN_VALUE, 4, 0),
        Long.MIN_VALUE to listOf<Long>(Long.MAX_VALUE, Long.MIN_VALUE, 4, 0),
        0L to listOf<Long>(Long.MAX_VALUE, Long.MIN_VALUE, 4, 0),
    )

    context("cmp") {
        withData(inputs) { (of, slice) ->
            val program = builder.build(buildComplexFunction { countOccurrences() })
            val input = slice + listOf(of, 0L, slice.size.toLong())
            val count = slice.count { it == of }.toLong()
            runner.run(program, input) shouldContainExactly slice + listOf(count)
        }
    }

    context("cmp with prefix") {
        withData(inputs) { (of, slice) ->
            val program = builder.build(buildComplexFunction { countOccurrences() })
            val prefix = listOf(of, 0, of) // not searched
            val input = prefix + slice + listOf(of, 3L, slice.size.toLong())
            val count = slice.count { it == of }.toLong()
            runner.run(program, input) shouldContainExactly prefix + slice + listOf(count)
        }
    }
})
