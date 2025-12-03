package cz.sejsel.ksplang.aoc.days

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.KsplangProgram
import cz.sejsel.ksplang.dsl.core.createLabel
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.gotoLabel
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 3
// https://adventofcode.com/2025/day/3
fun main() {
    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(day3Part1())
    File("aoc25/ksplang/3-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/3-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 3 part 1")
    /*
    val program2 = builder.buildAnnotated(day3Part2())
    File("aoc25/ksplang/3-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/3-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 3 part 2")
     */
}

fun day3Part1() = program {
    // Very straightforward really:
    // for each line go i = 9..1, find leftmost $i in [0..len-1)
    // then find highest digit to the right of this digit

    val lineEndLabel = createLabel("end")

    body {
        stacklen()
        // input_len
        push('\n'.code)
        push(0)
        // input_len newline 0
        roll(3, -1)
        // newline 0 input_len
        countOccurrences()
        // lines
        push(0)
        push(0)
        // lines result pos
        roll(3, 2)
        // result pos lines
        doWhileNonZero {
            // result pos lines+1
            dec()
            // result pos lines
            roll(3, 1)
            // lines result pos
            dup()
            push('\n'.code)
            // lines result pos pos newline
            findUnsafeSubabs() // we know all values on stack are in ascii range even
            // lines result pos newline_pos
            push(9)
            // lines result pos newline_pos 9
            // lines result pos newline_pos left
            doWhileNonZero {
                // result pos newline_pos left
                CS() // dup, left is 1-9
                // result pos newline_pos left left
                dupFourth()
                // result pos newline_pos left left pos
                dupFourth()
                // result pos newline_pos left left pos newline_pos
                dec()
                // result pos newline_pos left left pos newline_pos-1
                roll(3, 2)
                // result pos newline_pos left pos newline_pos-1 left
                add('0'.code.toLong())
                // result pos newline_pos left pos newline_pos-1 ascii_left
                find()
                // result pos newline_pos left found_pos?
                dup()
                inc()
                ifZero {
                    // this left digit is not in the line
                    // result pos newline_pos left -1 0
                    pop()
                    pop()
                    // result pos newline_pos left
                } otherwise {
                    // now we need to find max value in [found_pos+1..newline_pos)
                    // result pos newline_pos left found_pos found_pos+1
                    pop2()
                    // result pos newline_pos left found_pos+1
                    dupThird()
                    // result pos newline_pos left found_pos+1 newline_pos
                    dupSecond()
                    // result pos newline_pos left found_pos+1 newline_pos found_pos+1
                    subabs()
                    // result pos newline_pos left found_pos+1 len
                    findSliceMax()
                    // result pos newline_pos left max_to_right_of_left
                    // result pos newline_pos left right_ascii
                    add(-'0'.code.toLong())
                    // result pos newline_pos left right
                    swap2()
                    mul(10)
                    // result pos newline_pos right left*10
                    add()
                    // result pos newline_pos left*10+right
                    roll(4, -1)
                    // pos newline_pos left*10+right result
                    add()
                    // pos newline_pos result+left*10+right
                    // pos newline_pos result
                    roll(3, 1)
                    // result pos newline_pos
                    pop2() // update pos to newline_pos
                    // result newline_pos
                    inc() // go past newline
                    // lines result newline_pos+1
                    roll(3, -1)
                    // result newline_pos+1 lines
                    gotoLabel(lineEndLabel)
                }
                // result pos newline_pos left

                dec() // last iteration is 1
                CS()
            }

            +lineEndLabel
            // result pos lines
            CS()
        }
        // result pos lines
        pop()
        pop()
        // result
        leaveTop()
    }
}

fun day3Part2(): KsplangProgram = TODO()


