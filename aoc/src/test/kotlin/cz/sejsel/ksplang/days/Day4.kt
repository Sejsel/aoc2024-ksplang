package cz.sejsel.ksplang.days

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.aoc.checkDo
import cz.sejsel.ksplang.aoc.checkDont
import cz.sejsel.ksplang.aoc.checkMul
import cz.sejsel.ksplang.aoc.getXY
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GetXYTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { getXY() })

    val data = listOf<Long>(1, 2, 3, 4, -1, 5, 6, 7, 8, -1, 9, 10, 11, 12, -1, 13, 14, 15, 16, -1)
    val width = 4L
    val height = 4L

    /** width height x y -> width height s[x,y] */
    test("getXY 0 0") {
        val input = data + listOf<Long>(width, height, 0, 0)
        runner.run(program, input) shouldBe data + listOf<Long>(width, height, 1)
    }

    test("getXY 3 3") {
        val input = data + listOf<Long>(width, height, 3, 3)
        runner.run(program, input) shouldBe data + listOf<Long>(width, height, 16)
    }
})
