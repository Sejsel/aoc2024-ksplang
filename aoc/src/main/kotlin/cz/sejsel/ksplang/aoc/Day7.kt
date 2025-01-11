package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 7
// https://adventofcode.com/2024/day/7
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day7Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/7-1.ksplang").writeText(program)
    println("Generated program for day 7 part 1, $instructionCount instructions")
    //val program2 = builder.build(day7Part2())
    //val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    //File("ksplang/7-2.ksplang").writeText(program2)
    //println("Generated program for day 7 part 2, $instructionCount2 instructions")
}

val NEWLINE = const('\n'.code)
val SPACE = const(' '.code)

fun day7Part1() = buildComplexFunction("day6part1") {
    // [input]
    stacklen()
    // [input] stacklen
    auto("stacklen") { stacklen ->
        val input = Slice(const(0), stacklen)

        val pos = variable(0)
        val lines = countOccurrences(NEWLINE, input)

        val totalSolvableTargets = variable(0)

        doNTimes(lines) {
            val (target, newPos) = parseNonNegativeNum(pos, ':'.code)
            set(pos) to add(newPos, 2) // there is a space after the colon

            val eol = findUnsafe(pos, NEWLINE)
            val nums = Slice(pos, subabs(eol, pos))
            val opCount = countOccurrences(SPACE, nums)
            doNTimes(inc(opCount)) { i ->
                val end = variable(SPACE)
                ifBool(eq(i, opCount)) { // opCount is the last number
                    set(end) to NEWLINE
                }

                val (num, newPos) = parseNonNegativeNum(pos, end)
                set(pos) to add(newPos, 1) // there is a space after the number

                // We can just save the number sequence over the start of the input as we
                // are guaranteed to read "faster" than overwrite it.
                yeet(i, num)
            }

            // s[0:words] now contains the numbers

            // there are 2^(words-1) combinations of the operators
            val combinations = bitshift(const(1), opCount)

            val isSolvable = variable(false)
            doNTimes(combinations) { i ->
                val result = yoink(const(0))

                doNTimes(opCount) { op ->
                    val isBitSet = bitand(i, bitshift(const(1), op))
                    ifBool(isBitSet) {
                        set(result) to add(result, yoink(add(op, 1)))
                    } otherwise {
                        set(result) to mul(result, yoink(add(op, 1)))
                    }
                }
                ifBool(eq(result, target)) {
                    set(isSolvable) to true
                }
            }

            ifBool(isSolvable) {
                set(totalSolvableTargets) to add(totalSolvableTargets, target)
            }
        }

        keepOnly(totalSolvableTargets)
    }
    leaveTop()
}
