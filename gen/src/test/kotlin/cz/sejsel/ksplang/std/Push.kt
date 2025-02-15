package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import kotlin.random.Random

class PushTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..16L) {
        context("push($i) onto single value should add $i") {
            withData(VALUES_PER_DIGIT_SUM) {
                val program = builder.build(buildFunction {
                    push(i)
                })
                runner.run(program, listOf(it)) shouldContainExactly listOf(it, i)
            }
        }
    }

    val randomValueCount = 500
    val random = Random(42)
    val values = (0..<randomValueCount).map { random.nextLong() }
    val stackTops = (0..<randomValueCount).map { random.nextLong() }

    context("random tests") {
        withData(values.zip(stackTops)) { (n, stackTop) ->
            val program = builder.build(buildFunction {
                push(n)
            })
            runner.run(program, listOf(stackTop)) shouldContainExactly listOf(stackTop, n)
        }
    }
})

class PushOptimizedTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("short push programs") {
        withData(ShortPushes.sequencesByNumber.keys) { i ->
            val program = builder.build(buildFunction {
                push(i)
            })
            withData(VALUES_PER_DIGIT_SUM) {
                runner.run(program, listOf(it)) shouldContainExactly listOf(it, i)
            }
        }
    }
})


class PushPaddedToTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..16L) {
        context("pushPaddedTo($i, 100) onto single value should add $i") {
            val func = buildFunction { pushPaddedTo(i, 100) }
            func.getInstructions() shouldHaveSize 100

            withData(VALUES_PER_DIGIT_SUM) {
                val program = builder.build(func)
                runner.run(program, listOf(it)) shouldContainExactly listOf(it, i)
            }
        }
    }
})

class PushOnTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val params = buildList {
        for (top in -1L..16L) {
            for (n in -1L..16L) {
                add(top to n)
            }
        }
    }
    context("pushOn") {
        withData(params) { (top, n) ->
            val func = buildFunction { pushOn(top, n) }
            val program = builder.build(func)
            runner.run(program, listOf(top)) shouldContainExactly listOf(top, n)
        }
    }
})

class PushManyTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("pushMany 4 7") {
        val program = builder.build(buildComplexFunction {
            pushMany(4, 7)
        })
        runner.run(program, listOf(42)) shouldContainExactly listOf(42, 4, 4, 4, 4, 4, 4, 4)
    }
    test("pushMany 4 0") {
        val program = builder.build(buildComplexFunction {
            pushMany(4, 0)
        })
        runner.run(program, listOf(42)) shouldContainExactly listOf(42)
    }
})

class PushManyLenTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildComplexFunction {
        pushManyAndKeepLen(4)
    })

    test("pushManyLen(4) 7") {
        runner.run(program, listOf(42, 7)) shouldContainExactly listOf(42, 4, 4, 4, 4, 4, 4, 4, 7)
    }
    test("pushManyLen(4) 0") {
        runner.run(program, listOf(42, 0)) shouldContainExactly listOf(42, 0)
    }
})

class PushManyBottomTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("pushManyBottom 4 7") {
        val program = builder.build(buildComplexFunction {
            pushManyBottom(4, 7)
        })
        runner.run(program, listOf(42)) shouldContainExactly listOf(4, 4, 4, 4, 4, 4, 4, 42)
    }
    test("pushManyBottom 4 0") {
        val program = builder.build(buildComplexFunction {
            pushManyBottom(4, 0)
        })
        runner.run(program, listOf(42)) shouldContainExactly listOf(42)
    }
})
