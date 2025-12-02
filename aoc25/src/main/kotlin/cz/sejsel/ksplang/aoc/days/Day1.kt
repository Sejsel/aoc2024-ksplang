package cz.sejsel.ksplang.aoc.days

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 1
// https://adventofcode.com/2025/day/1
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day1Part1())
    File("aoc25/ksplang/1-1.ksplang").writeText(program)
    println("Generated program for day 1 part 1")
    //val program2 = builder.build(day1Part2())
    //File("aoc25/ksplang/1-2.ksplang").writeText(program2)
    //println("Generated program for day 1 part 2")
}

// Text input, numeric output
fun day1Part1() = buildComplexFunction("day1") {
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
    // lines result
    push(50)
    // lines result dial_pos
    push(0)
    // lines result dial_pos input_index
    roll(4, -1)
    // result dial_pos input_index lines
    doWhileNonZero {
        // result dial_pos input_index remaining_lines+1
        dec()
        // result dial_pos input_index remaining_lines
        swap2()
        // result dial_pos remaining_lines input_index
        dup(); yoink()
        // result dial_pos remaining_lines input_index L/R
        push('L'.code)
        subabs()
        ifZero {
            // this is L
            // result dial_pos remaining_lines input_index 0
            dec()
            // result dial_pos remaining_lines input_index -1
        } otherwise {
            // this is R
            // result dial_pos remaining_lines input_index 6
            pushOn(6, 1)
            pop2()
            // result dial_pos remaining_lines input_index 1
        }
        // result dial_pos remaining_lines input_index mult
        swap2()
        // result dial_pos remaining_lines mult input_index
        inc()
        // result dial_pos remaining_lines mult input_index+1
        parseNonNegativeNum('\n'.code)
        // result dial_pos remaining_lines mult num eol
        inc()
        // result dial_pos remaining_lines mult num eol+1
        // result dial_pos remaining_lines mult num next_input_index
        permute("result dial_pos remaining_lines mult num next_input_index", "next_input_index remaining_lines result dial_pos mult num")
        // next_input_index remaining_lines result dial_pos mult num
        mul()
        // next_input_index remaining_lines result dial_pos offset
        add()
        push(100)
        swap2()
        modulo()
        // next_input_index remaining_lines result (dial_pos+offset)%100
        // next_input_index remaining_lines result dial_pos
        ifZero {
            // result++
            swap2(); inc(); swap2()
        }
        // next_input_index remaining_lines result dial_pos
        roll(4, 2)
        // result dial_pos next_input_index remaining_lines
        CS()
    }
    // result dial_pos next_input_index 0
    pop()
    pop()
    pop()
    // result
    leaveTop()
}
