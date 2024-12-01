package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.function
import cz.sejsel.ksplang.KsplangRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly

class PushTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    context("push(0) onto single value should add 0") {
        withData(VALUES_PER_DIGIT_SUM) {
            val program = builder.build(function {
                push(0)
            })
            runner.run(program, listOf(it)) shouldContainExactly listOf(it, 0)
        }
    }
})