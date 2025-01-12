package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.buildFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.streams.toList

class MapTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    test("map 1 value") {
        val program = builder.build(buildFunction { map(listOf(10)) })
        runner.run(program, listOf(42, 0)) shouldContainExactly listOf<Long>(42, 10)
    }

    test("map 3 values [0]") {
        val program = builder.build(buildFunction { map(listOf(10, 20, 30)) })
        runner.run(program, listOf(42, 0)) shouldContainExactly listOf<Long>(42, 10)
    }

    test("map 3 values [1]") {
        val program = builder.build(buildFunction { map(listOf(10, 20, 30)) })
        runner.run(program, listOf(42, 1)) shouldContainExactly listOf<Long>(42, 20)
    }

    test("map 3 values [2]") {
        val program = builder.build(buildFunction { map(listOf(10, 20, 30)) })
        runner.run(program, listOf(42, 2)) shouldContainExactly listOf<Long>(42, 30)
    }
})
