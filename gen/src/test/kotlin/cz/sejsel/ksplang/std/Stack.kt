package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildFunction
import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly

class RollTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("roll(0, 0) does nothing") {
        val program = builder.build(buildFunction { roll(0, 0) })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2)
    }

    test("roll(1, 1) does nothing") {
        val program = builder.build(buildFunction { roll(1, 1) })
        runner.run(program, listOf(1, 2)) shouldContainExactly listOf(1, 2)
    }

    context("roll(i, 0) does nothing") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, 0) })
            runner.run(program, stack) shouldContainExactly stack
        }
    }

    context("roll(i, 1) rotates one right") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, 1) })
            runner.run(program, stack) shouldContainExactly listOf(stack[stack.size - 1]) + stack.dropLast(1)
        }
    }

    context("roll(i, -1) rotates one left") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, -1) })
            runner.run(program, stack) shouldContainExactly stack.drop(1) + stack[0]
        }
    }

    context("roll(i, i) does nothing") {
        withData(3L..16L) {
            val stack = (1..it).toList()
            val program = builder.build(buildFunction { roll(it, it) })
            runner.run(program, stack) shouldContainExactly stack
        }
    }
})

class StacklenTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("stacklen on non-empty stack") {
        val program = builder.build(buildComplexFunction { stacklen() })
        val input = listOf(1L, 2L, 3L)
        runner.run(program, input) shouldContainExactly input + listOf(input.size.toLong())
    }

    test("stacklen on all CS values") {
        val program = builder.build(buildComplexFunction { stacklen() })
        val input = VALUES_PER_DIGIT_SUM
        runner.run(program, input) shouldContainExactly input + listOf(input.size.toLong())
    }

    test("stacklen on max and min values") {
        val program = builder.build(buildComplexFunction { stacklen() })
        val input = listOf(Long.MAX_VALUE, Long.MIN_VALUE)
        runner.run(program, input) shouldContainExactly input + listOf(2)
    }

    test("stacklenWithMin on non-empty stack") {
        val program = builder.build(buildComplexFunction { stacklenWithMin() })
        val input = listOf(1L, 2L, 3L, 2L)
        runner.run(program, input) shouldContainExactly input.dropLast(1) + listOf(input.size.toLong() - 1)
    }

    test("stacklenWithMin with size of stack excluding the param") {
        val program = builder.build(buildComplexFunction { stacklenWithMin() })
        val input = listOf(1L, 2L, 3L, 3L)
        runner.run(program, input) shouldContainExactly input.dropLast(1) + listOf(input.size.toLong() - 1)
    }
})


class YoinkTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("yoink index $i") {
            val program = builder.build(buildComplexFunction { yoink() })
            runner.run(program, (1L..10L).toList() + listOf(i)) shouldContainExactly (1L..10L).toList() + listOf(i + 1)
        }
    }

    // TODO: test yoinkDestructive
})

class YeetTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("yeet index $i") {
            val program = builder.build(buildComplexFunction { yeet() })
            val input = (1L..10L).toList() + listOf(-1, i)
            val output = (1L..10L).toMutableList().apply {
                set(i.toInt(), -1)
            }.toList()
            runner.run(program, input) shouldContainExactly output
        }
    }
})

class LeaveTopTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("leaveTop $i") {
            val program = builder.build(buildComplexFunction { leaveTop() })
            val input = (0L..i).toList() + listOf(42L)
            runner.run(program, input) shouldContainExactly listOf(42L)
        }
    }
})

class PopManyTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    for (i in 0L..9L) {
        test("popMany $i") {
            val program = builder.build(buildComplexFunction { popMany() })
            val input = (1..10).map { 42L } + listOf(i)
            runner.run(program, input) shouldContainExactly (1..(10 - i)).map { 42L }
        }
    }
})

class PopKthTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("popKth(1)") {
        val program = builder.build(buildFunction { popKth(1) })
        runner.run(program, listOf(1, 2, 3, 4, 5)) shouldContainExactly listOf<Long>(1, 2, 3, 4)
    }

    test("popKth(2)") {
        val program = builder.build(buildFunction { popKth(2) })
        runner.run(program, listOf(1, 2, 3, 4, 5)) shouldContainExactly listOf<Long>(1, 2, 3, 5)
    }

    test("popKth(3)") {
        val program = builder.build(buildFunction { popKth(3) })
        runner.run(program, listOf(1, 2, 3, 4, 5)) shouldContainExactly listOf<Long>(1, 2, 4, 5)
    }

    test("popKth(4)") {
        val program = builder.build(buildFunction { popKth(4) })
        runner.run(program, listOf(1, 2, 3, 4, 5)) shouldContainExactly listOf<Long>(1, 3, 4, 5)
    }

    test("popKth(5)") {
        val program = builder.build(buildFunction { popKth(5) })
        runner.run(program, listOf(1, 2, 3, 4, 5)) shouldContainExactly listOf<Long>(2, 3, 4, 5)
    }
})


class PopNthTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildFunction { popNth() })

    test("popNth 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 1)) shouldContainExactly listOf<Long>(1, 2, 3, 4)
    }

    test("popNth 2") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 2)) shouldContainExactly listOf<Long>(1, 2, 3, 5)
    }

    test("popNth 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 3)) shouldContainExactly listOf<Long>(1, 2, 4, 5)
    }

    test("popNth 4") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 4)) shouldContainExactly listOf<Long>(1, 3, 4, 5)
    }

    test("popNth 5") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 5)) shouldContainExactly listOf<Long>(2, 3, 4, 5)
    }
})

class MoveNthToTopTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildFunction { moveNthToTop() })

    test("moveNthToTop 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 1)) shouldContainExactly listOf<Long>(1, 2, 3, 4, 5)
    }

    test("moveNthToTop 2") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 2)) shouldContainExactly listOf<Long>(1, 2, 3, 5, 4)
    }

    test("moveNthToTop 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 3)) shouldContainExactly listOf<Long>(1, 2, 4, 5, 3)
    }

    test("moveNthToTop 4") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 4)) shouldContainExactly listOf<Long>(1, 3, 4, 5, 2)
    }

    test("moveNthToTop 5") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 5)) shouldContainExactly listOf<Long>(2, 3, 4, 5, 1)
    }
})

class SetNthTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()
    val program = builder.build(buildFunction { setNth() })

    test("setNth 1") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 1, 42)) shouldContainExactly listOf<Long>(1, 2, 3, 4, 42)
    }

    test("setNth 2") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 2, 42)) shouldContainExactly listOf<Long>(1, 2, 3, 42, 5)
    }

    test("setNth 3") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 3, 42)) shouldContainExactly listOf<Long>(1, 2, 42, 4, 5)
    }

    test("setNth 4") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 4, 42)) shouldContainExactly listOf<Long>(1, 42, 3, 4, 5)
    }

    test("setNth 5") {
        runner.run(program, listOf(1, 2, 3, 4, 5, 5, 42)) shouldContainExactly listOf<Long>(42, 2, 3, 4, 5)
    }
})

class SetKthTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("setKth 1") {
        val program = builder.build(buildFunction { setKth(1) })
        runner.run(program, listOf(1, 2, 3, 4, 5, 42)) shouldContainExactly listOf<Long>(1, 2, 3, 4, 42)
    }

    test("setKth 2") {
        val program = builder.build(buildFunction { setKth(2) })
        runner.run(program, listOf(1, 2, 3, 4, 5, 42)) shouldContainExactly listOf<Long>(1, 2, 3, 42, 5)
    }

    test("setKth 3") {
        val program = builder.build(buildFunction { setKth(3) })
        runner.run(program, listOf(1, 2, 3, 4, 5, 42)) shouldContainExactly listOf<Long>(1, 2, 42, 4, 5)
    }

    test("setKth 4") {
        val program = builder.build(buildFunction { setKth(4) })
        runner.run(program, listOf(1, 2, 3, 4, 5, 42)) shouldContainExactly listOf<Long>(1, 42, 3, 4, 5)
    }

    test("setKth 5") {
        val program = builder.build(buildFunction { setKth(5) })
        runner.run(program, listOf(1, 2, 3, 4, 5, 42)) shouldContainExactly listOf<Long>(42, 2, 3, 4, 5)
    }
})

class PermuteTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    test("permute(a b c, a b c) does nothing") {
        val program = builder.build(buildComplexFunction { permute("a b c", "a b c") })
        val input = listOf<Long>(1, 2, 3, 4, 5, 6)
        val output = listOf<Long>(1, 2, 3, 4, 5, 6)
        runner.run(program, input) shouldContainExactly output
    }

    test("permute(a b c, b a c)") {
        val program = builder.build(buildComplexFunction { permute("a b c", "b a c") })
        val input = listOf<Long>(1, 2, 3, 4, 5, 6)
        val output = listOf<Long>(1, 2, 3, 5, 4, 6)
        runner.run(program, input) shouldContainExactly output
    }

    test("permute(a b c, c a b)") {
        val program = builder.build(buildComplexFunction { permute("a b c", "c a b") })
        val input = listOf<Long>(1, 2, 3, 4, 5, 6)
        val output = listOf<Long>(1, 2, 3, 6, 4, 5)
        runner.run(program, input) shouldContainExactly output
    }

    test("permute(a b c d e f, f e d c b a)") {
        val program = builder.build(buildComplexFunction { permute("a b c d e f", "f e d c b a") })
        val input = listOf<Long>(1, 2, 3, 4, 5, 6)
        val output = listOf<Long>(6, 5, 4, 3, 2, 1)
        runner.run(program, input) shouldContainExactly output
    }

    test("permute(a b c d e f, a e d c b f)") {
        val program = builder.build(buildComplexFunction { permute("a b c d e f", "a e d c b f") })
        val input = listOf<Long>(1, 2, 3, 4, 5, 6)
        val output = listOf<Long>(1, 5, 4, 3, 2, 6)
        runner.run(program, input) shouldContainExactly output
    }
})

class FindUnsafeTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { findUnsafe() })

    test("findUnsafe finds at the end") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 42, 0, 42)
        val output = listOf<Long>(1, 2, 3, 4, 5, 6, 42, 6)
        runner.run(program, input) shouldContainExactly output
    }

    test("findUnsafe finds at the start") {
        val input = listOf<Long>(42, 2, 3, 4, 5, 6, 42, 0, 42)
        val output = listOf<Long>(42, 2, 3, 4, 5, 6, 42, 0)
        runner.run(program, input) shouldContainExactly output
    }
})

class FindUnsafeSubAbsTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val program = builder.build(buildComplexFunction { findUnsafeSubabs() })

    test("findUnsafeSubabs finds at the end") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 42, 0, 42)
        val output = listOf<Long>(1, 2, 3, 4, 5, 6, 42, 6)
        runner.run(program, input) shouldContainExactly output
    }

    test("findUnsafeSubabs finds at the start") {
        val input = listOf<Long>(42, 2, 3, 4, 5, 6, 42, 0, 42)
        val output = listOf<Long>(42, 2, 3, 4, 5, 6, 42, 0)
        runner.run(program, input) shouldContainExactly output
    }

    test("findUnsafeSubabs finds negative values") {
        val input = listOf<Long>(42, 2, 3, 4, 5, 6, -42, 0, -42)
        val output = listOf<Long>(42, 2, 3, 4, 5, 6, -42, 6)
        runner.run(program, input) shouldContainExactly output
    }
})

class FindTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val annotated = builder.buildAnnotated(buildComplexFunction { find() })
    val program = annotated.toRunnableProgram()

    test("find finds at the start") {
        val input = listOf<Long>(42, 2, 3, 4, 5, 6, 42, 0, 7, 42)
        val output = listOf<Long>(42, 2, 3, 4, 5, 6, 42, 0)
        runner.run(program, input) shouldContainExactly output
    }

    test("find finds one after the start") {
        val input = listOf<Long>(1, 42, 3, 4, 5, 6, 7, 0, 7, 42)
        val output = listOf<Long>(1, 42, 3, 4, 5, 6, 7, 1)
        runner.run(program, input) shouldContainExactly output
    }

    test("find finds one before end") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 42, 7, 0, 7, 42)
        val output = listOf<Long>(1, 2, 3, 4, 5, 42, 7, 5)
        runner.run(program, input) shouldContainExactly output
    }

    test("find finds at the end") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 42, 0, 7, 42)
        val output = listOf<Long>(1, 2, 3, 4, 5, 6, 42, 6)
        runner.run(program, input) shouldContainExactly output
    }

    test("find finds negative values") {
        val input = listOf<Long>(42, 2, 3, 4, 5, 6, -42, 0, 7, -42)
        val output = listOf<Long>(42, 2, 3, 4, 5, 6, -42, 6)
        runner.run(program, input) shouldContainExactly output
    }

    test("find does not find if it's right before range") {
        val input = listOf<Long>(1, 42, 3, 4, 5, 6, 7, 2, 4, 42)
        val output = listOf<Long>(1, 42, 3, 4, 5, 6, 7, -1)
        runner.run(program, input) shouldContainExactly output
    }

    test("find does not find if it's right after range") {
        val input = listOf<Long>(1, 2, 3, 4, 42, 6, 7, 2, 4, 42)
        val output = listOf<Long>(1, 2, 3, 4, 42, 6, 7, -1)
        runner.run(program, input) shouldContainExactly output
    }
})
