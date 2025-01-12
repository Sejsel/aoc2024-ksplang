package cz.sejsel.ksplang.days

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.aoc.concat
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConcatTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { auto("a", "b") { a, b ->
        val res = concat(a, b)
        keepOnly(res)
    } })

    test("concat 15 6 = 156") {
        runner.run(program, listOf(15, 6)) shouldBe listOf<Long>(156)
    }
})
