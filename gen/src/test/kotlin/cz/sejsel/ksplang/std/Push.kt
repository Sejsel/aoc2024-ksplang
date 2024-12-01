package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.function
import cz.sejsel.ksplang.KsplangRunner
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
                val program = builder.build(function {
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
            val program = builder.build(function {
                push(n)
            })
            runner.run(program, listOf(stackTop)) shouldContainExactly listOf(stackTop, n)
        }
    }
})

class PushPaddedToTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..16L) {
        context("pushPaddedTo($i, 100) onto single value should add $i") {
            val func = function { pushPaddedTo(i, 100) }
            func.getInstructions() shouldHaveSize 100

            withData(VALUES_PER_DIGIT_SUM) {
                val program = builder.build(func)
                runner.run(program, listOf(it)) shouldContainExactly listOf(it, i)
            }
        }
    }
})
