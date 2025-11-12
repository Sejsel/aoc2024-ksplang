package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.DefaultKsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly


class AllocTests : FunSpec({
    val runner = DefaultKsplangRunner()
    val builder = KsplangBuilder()

    val prefix = listOf<Long>(1000)

    test("alloc should allocate a block of zeroes") {
        val program = builder.build(buildComplexFunction {
            auto {
                val allocator = Allocator(variable(1))
                val block = alloc(allocator, 5.const)
                keepOnly()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(0, 0, 0, 0, 0)
    }

    test("alloc + set slice to") {
        val program = builder.build(buildComplexFunction {
            auto {
                val allocator = Allocator(variable(1))
                val block = alloc(allocator, 5.const)
                setSliceTo(block, 1.const)
                keepOnly()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1, 1, 1, 1, 1)
    }

    test("two allocs") {
        val program = builder.build(buildComplexFunction {
            auto {
                val allocator = Allocator(variable(1))
                val block1 = alloc(allocator, 2.const)
                setSliceTo(block1, 1.const)
                val block2 = alloc(allocator, 3.const)
                setSliceTo(block2, 2.const)
                keepOnly()
            }
        })
        runner.run(program, prefix) shouldContainExactly prefix + listOf(1, 1, 2, 2, 2)
    }
})

