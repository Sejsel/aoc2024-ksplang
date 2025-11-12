package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class SortTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    // TODO: More tests

    test("sort(3) 321 -> 123") {
        val tree = buildComplexFunction { sort() }
        val program = builder.build(tree)
        val input = listOf(3L, 2L, 1L)
        val result = runner.run(program, input + listOf(input.size.toLong()))
        result shouldContainExactly listOf(1, 2, 3)
    }
    test("sort(2) 321 -> 231") {
        val tree = buildComplexFunction { sort() }
        val program = builder.build(tree)
        val input = listOf(3L, 2L, 1L)
        val result = runner.run(program, input + listOf(2))
        result shouldContainExactly listOf(2, 3, 1)
    }
})
