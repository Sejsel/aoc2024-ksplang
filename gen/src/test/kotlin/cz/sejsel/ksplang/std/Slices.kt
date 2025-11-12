package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly


class CountOccurrencesTests : FunSpec({
    val runner = DefaultKsplangRunner()
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

class YoinkSliceTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildComplexFunction { yoinkSlice() })

    test("yoink slice from 0, len 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 0, 3)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 1, 2, 3, 3)
    }

    test("yoink slice from 1, len 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 3)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 2, 3, 4, 3)
    }

    test("yoink slice from 2, len 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 2, 1)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 3, 1)
    }

    test("yoink slice from 1, len 0") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 0)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 0)
    }
})

class YoinkSliceWithGapTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildComplexFunction { yoinkSliceWithGap() })

    test("yoink slice from 0, len 3, gap 0") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 0, 3, 0)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 2, 3, 2)
    }
    test("yoink slice from 0, len 3, gap 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 0, 3, 1)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 1, 3, 2)
    }
    test("yoink slice from 0, len 3, gap 2") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 0, 3, 2)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 1, 2, 2)
    }

    test("yoink slice from 1, len 3, gap 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 3, 1)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 2, 4, 2)
    }

    test("yoink slice from 2, len 1, gap 0") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 2, 1, 0)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 0)
    }
})

class CopySliceTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildComplexFunction { copySlice() })

    test("copySlice slice from 0 to 3, len 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 0, 3, 3)) shouldContainExactly listOf(1, 2, 3, 1, 2, 3)
    }

    test("copySlice aliased slice from 1 to 3 len 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 3, 3)) shouldContainExactly listOf(1, 2, 3, 2, 3, 2)
    }

    test("copySlice slice from 2 to 3, len 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 2, 3, 1)) shouldContainExactly listOf(1, 2, 3, 3, 5, 6)
    }

    test("copySlice slice from 1 to 3, len 0") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 3, 0)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6)
    }
})

class SetSliceTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildComplexFunction { setSlice() })

    test("set slice from 0, len 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 0, 3, 42)) shouldContainExactly listOf(42, 42, 42, 4, 5, 6)
    }

    test("set slice from 1, len 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 3, 42)) shouldContainExactly listOf(1, 42, 42, 42, 5, 6)
    }

    test("set slice from 2, len 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 2, 1, 42)) shouldContainExactly listOf(1, 2, 42, 4, 5, 6)
    }

    test("set slice from 1, len 0") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 6, 1, 0, 42)) shouldContainExactly listOf(1, 2, 3, 4, 5, 6)
    }
})
