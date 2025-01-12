package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun1
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
    val program2 = builder.build(day7Part2())
    val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    File("ksplang/7-2.ksplang").writeText(program2)
    println("Generated program for day 7 part 2, $instructionCount2 instructions")
}

val NEWLINE = const('\n'.code)
val SPACE = const(' '.code)

fun day7Part1() = day7(isSolvableCheck = Scope::tryAddAndMul)
fun day7Part2() = day7(isSolvableCheck = Scope::tryAddMulAndConcat)

fun day7(isSolvableCheck: Scope.(target: Variable, opCount: Variable) -> Variable) = buildComplexFunction {
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

            val isSolvable = isSolvableCheck(target, opCount)

            ifBool(isSolvable) {
                set(totalSolvableTargets) to add(totalSolvableTargets, target)
            }
        }

        keepOnly(totalSolvableTargets)
    }
    leaveTop()
}

private fun Scope.tryAddAndMul(
    target: Variable,
    opCount: Variable,
): Variable {
    val isSolvable = variable(false)
    // there are 2^(words-1) combinations of the operators
    val combinations = bitshift(const(1), opCount)

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

    return isSolvable
}

private fun Scope.tryAddMulAndConcat(
    target: Variable,
    opCount: Variable,
): Variable {
    val isSolvable = variable(false)
    // there are 3^(words-1) combinations of the operators
    val combinations = map(opCount, listOf(1, 3, 9, 27, 81, 243, 729, 2187, 6561, 19683, 59049, 177147, 531441, 1594323, 4782969, 14348907))

    doNTimes(combinations) { i ->
        val result = yoink(const(0))

        val opTernary = copy(i)

        doNTimes(opCount) { op ->
            val operation = mod(opTernary, const(3))
            set(opTernary) to div(opTernary, const(3))

            ifBool(eq(operation, const(0))) {
                set(result) to add(result, yoink(add(op, 1)))
            }

            ifBool(eq(operation, const(1))) {
                set(result) to mul(result, yoink(add(op, 1)))
            }

            ifBool(eq(operation, const(2))) {
                set(result) to concat(result, yoink(add(op, 1)))
            }
        }
        ifBool(eq(result, target)) {
            set(isSolvable) to true
        }
    }

    return isSolvable
}

fun Scope.concat(a: Parameter, b: Parameter) = runFun1(a, b) {
    // a b
    dup()
    // a b b
    push(0)
    // a b b 0
    lensum()
    // a b len(b)
    map(listOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000, 100000000000, 1000000000000, 10000000000000, 100000000000000, 1000000000000000, 10000000000000000, 100000000000000000, 1000000000000000000))
    // a b 10^len(b)
    permute("a b len", "b len a")
    // b 10^len(b) a
    mul()
    // b 10^len(b)*a
    add()
    // 10^len(b)*a+b
}