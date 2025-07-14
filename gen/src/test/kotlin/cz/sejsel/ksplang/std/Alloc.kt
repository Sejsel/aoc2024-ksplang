package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class AllocTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildComplexFunction {
        alloc()
    })
    println(program)

    test("alloc(pos=0, len=5)") {
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 0, 5)
        val output = listOf<Long>(0, 0, 0, 0, 0, 42, 43, 44, 45, 46, 47, 48, 5)
        runner.run(program, input) shouldContainExactly output
    }

    test("alloc(pos=2, len=5)") {
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 2, 5)
        val output = listOf<Long>(42, 43, 0, 0, 0, 0, 0, 44, 45, 46, 47, 48, 7)
        runner.run(program, input) shouldContainExactly output
    }

    test("alloc(pos=2, len=0)") {
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 2, 0)
        val output = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 2)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturn(pos=2, len=5)") {
        val program = builder.build(buildComplexFunction { allocNoReturn() })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 2, 5)
        val output = listOf<Long>(42, 43, 0, 0, 0, 0, 0, 44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturnConstLen(pos=0, len=0)") {
        val program = builder.build(buildComplexFunction { allocNoReturnConstLen(0) })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 0)
        val output = listOf<Long>(42, 43, 44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturnConstLen(pos=5, len=0)") {
        val program = builder.build(buildComplexFunction { allocNoReturnConstLen(0) })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 5)
        val output = listOf<Long>(42, 43, 44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturnConstLen(pos=2, len=5)") {
        val program = builder.build(buildComplexFunction { allocNoReturnConstLen(5) })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 2)
        val output = listOf<Long>(42, 43, 0, 0, 0, 0, 0, 44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturnConstLen(pos=0, len=5)") {
        val program = builder.build(buildComplexFunction { allocNoReturnConstLen(5) })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 0)
        val output = listOf<Long>(0, 0, 0, 0, 0, 42, 43, 44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturnConstLen(pos=0, len=105)") {
        val len = 105
        val program = builder.build(buildComplexFunction { allocNoReturnConstLen(len.toLong()) })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 0)
        val output = List(len) { 0L } + listOf(42, 43, 44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }

    test("allocNoReturnConstLen(pos=2, len=105)") {
        val len = 105
        val program = builder.build(buildComplexFunction { allocNoReturnConstLen(len.toLong()) })
        val input = listOf<Long>(42, 43, 44, 45, 46, 47, 48, 2)
        val output = listOf<Long>(42, 43) + List(len) { 0 } + listOf(44, 45, 46, 47, 48)
        runner.run(program, input) shouldContainExactly output
    }
})
