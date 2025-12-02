package cz.sejsel.ksplang.aoc.days

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import cz.sejsel.ksplang.dsl.core.ProgramFunction1To1
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 2
// https://adventofcode.com/2025/day/2
fun main() {
    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(day2Part1())
    File("aoc25/ksplang/2-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/2-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 2 part 1")
    val program2 = builder.buildAnnotated(day2Part2())
    File("aoc25/ksplang/2-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/2-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 2 part 2")
}

val powersLookup = listOf(
    1,
    10,
    100,
    1000,
    10000,
    100000,
    1000000,
    10000000,
    100000000,
    1000000000,
    10000000000,
    100000000000,
    1000000000000,
    10000000000000,
    100000000000000,
    1000000000000000,
    10000000000000000,
    100000000000000000,
    1000000000000000000
)

fun day2Part1(): KsplangProgram = day2(ProgramFunction1To1("is_invalid_id", buildComplexFunction {
    // id
    dup()
    push(0)
    lensum() // lensum(id, 0) is just a pure i10log
    // id len
    dup()
    push(2)
    swap2()
    modulo()
    // id len len%2
    ifZero {
        //    v len is even
        // id len 0
        pop()
        // id len
        div(2)
        // id half_len
        yoink() // Look up power of zero in lookup table at bottom of stack
        // id 10^half_len
        dupAb()
        // id 10^half_len id 10^half_len
        swap2()
        modulo()
        // id 10^half_len id%10^half_len
        // id 10^half_len bottom_half
        roll(3, 1)
        // bottom_half id 10^half_len
        swap2()
        div()
        // bottom_half id/10^half_len
        // bottom_half top_half
        subabs() // 0 if same
        zeroNotPositive() // 1 if same
        // bottom_half==top_half ? 1 : 0
    } otherwise {
        //    v len is odd, we know this cannot be a repeating pattern
        // id len 1
        pushOn(1, 0)
        // id len 1 0
        pop2()
        pop2()
        pop2()
        // 0
    }
}))

fun day2Part2(): KsplangProgram = day2(ProgramFunction1To1("is_invalid_id", buildComplexFunction {
    // TODO: We need breaks in the auto scopes, this is super inefficient, unfortunately
    auto("id") { id ->
        val len = i10log(id)
        val halfLen = div(len, 2)
        val isValid = variable(1)
        forRangeInclusive(const(1), halfLen) { partLen ->
            val mod = mod(len, partLen)
            ifNotBool(mod) {
                val partCount = div(len, partLen)
                val partsAreEqual = variable(1)
                val keepRunning = variable(1)
                val powerOfTen = yoink(partLen) // 10^partlen

                val prevPart = mod(id, powerOfTen)
                val remaining = div(id, powerOfTen)
                val partIndex = variable(1)
                whileNonZero({ bitand(partsAreEqual, keepRunning) }) {
                    val currentPart = mod(remaining, powerOfTen)
                    set(remaining) to div(remaining, powerOfTen)
                    set(partsAreEqual) to bitand(partsAreEqual, zeroNot(subabs(currentPart, prevPart)))
                    set(prevPart) to currentPart

                    set(partIndex) to inc(partIndex)
                    ifNotBool(subabs(partIndex, partCount)) {
                        set(keepRunning) to 0
                    }
                }

                ifBool(partsAreEqual) {
                    set(isValid) to 0
                }
            }
        }
        keepOnly(isValid)
    }
    zeroNotPositive()
}))

fun day2(isInvalidFunction: ProgramFunction1To1) = program {
    installFunction(isInvalidFunction)

    body {
        // Replace final line break with comma so we don't need to special case that
        pop() // remove last separator
        push(','.code)

        stacklen()
        // stacklen
        // Now set up a lookup table for powers of 10 at the bottom of the stack
        powersLookup.forEach { push(it) }
        // inputlen [1,10,100,...]
        dupKthZeroIndexed(powersLookup.size)
        // inputlen [1,10,100,...] inputlen
        add(powersLookup.size + 1L)
        // inputlen [1,10,100,...] stacklen
        push(powersLookup.size)
        swap2()
        // inputlen [1,10,100,...] stacklen
        lroll()
        // [1,10,100,...] [input] inputlen

        push(','.code)
        push(powersLookup.size)
        // inputlen ',' start
        roll(3, 2)
        // ',' start inputlen
        countOccurrences()
        // range_count
        push(0)
        push(powersLookup.size)
        // range_count result input_index
        roll(3, 2)
        // result input_index range_count
        doWhileNonZero {
            // result input_index range_count+1
            dec()
            // result input_index range_count
            roll(3, 1)
            // range_count result input_index
            parseNonNegativeNum('-'.code)
            inc()
            // range_count result from input_index
            parseNonNegativeNum(','.code)
            inc()
            // range_count result from to input_index
            roll(4, 1)
            // range_count input_index result from to
            swap2()
            // range_count input_index result to from
            dec()
            doWhileNonZero {
                // result to from-1
                inc()
                // result to from
                dup()
                call(isInvalidFunction)
                // result to from is_invalid
                ifZero {
                    // result to from 0
                    pop()
                } otherwise {
                    // result to from 1
                    pop()
                    // result to from
                    roll(3, 2)
                    // to from result
                    dupSecond()
                    // to from result from
                    add()
                    // to from result+from
                    // to from result
                    roll(3, 1)
                    // result to from
                }
                // result to from
                dupAb()
                // result to from to from
                subabs()
                // result to from abs(to-from)
            }
            // result to to
            pop()
            pop()
            // range_count input_index result
            permute("range_count input_index result", "result input_index range_count")
            // result input_index range_count
            CS()
        }
        // result input_index 0
        pop()
        pop()
        leaveTop()
    }
}
