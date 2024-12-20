package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.KsplangRunner
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
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

                countOccurrences(const(4), slice) { setTo(from) }

                keepOnly(from)
            }
        })
        runner.run(program, prefix + input) shouldContainExactly prefix + input + listOf(3L)
    }

})

