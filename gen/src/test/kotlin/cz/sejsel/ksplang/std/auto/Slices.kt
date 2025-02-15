package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class SliceTests : FunSpec({
    val runner = KsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("count occurrences") {
        val input = listOf<Long>(1, 2, 3, 4, 4, 5, 4, 5, 7)
        val program = builder.build(buildComplexFunction {
            auto {
                val from = variable("from", 3)
                val slice = Slice(from, const(6))

                set(from) to countOccurrences(const(4), slice)

                keepOnly(from)
            }
        })
        runner.run(program, prefix + input) shouldContainExactly prefix + input + listOf(3L)
    }


    test("set slice to zero") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val program = builder.build(buildComplexFunction {
            auto {
                val from = variable("from", 3)
                val slice = Slice(from, 3.const)

                setSliceTo(slice, 0.const)

                keepOnly()
            }
        })
        runner.run(program, input) shouldContainExactly listOf(1, 2, 3, 0, 0, 0, 7, 8, 9)
    }

    test("sliceForEach") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val program = builder.build(buildComplexFunction {
            auto {
                val from = variable(3)
                val slice = Slice(from, 3.const)

                val sum = variable(0)
                sliceForEach(slice) {
                    set(sum) to add(sum, it)
                }

                keepOnly(sum)
            }
        })
        runner.run(program, input) shouldContainExactly input + listOf(15L)
    }

    test("set(slice[1])") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val program = builder.build(buildComplexFunction {
            auto {
                val from = variable(3)
                val slice = Slice(from, 3.const)

                set(slice[1]) to 42

                keepOnly()
            }
        })
        runner.run(program, input) shouldContainExactly listOf(1, 2, 3, 4, 42, 6, 7, 8, 9)
    }

    test("set(slice[var])") {
        val input = listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val program = builder.build(buildComplexFunction {
            auto {
                val from = variable(3)
                val slice = Slice(from, 3.const)

                val index = variable(1)

                set(slice[index]) to 42

                keepOnly()
            }
        })
        runner.run(program, input) shouldContainExactly listOf(1, 2, 3, 4, 42, 6, 7, 8, 9)
    }
})

