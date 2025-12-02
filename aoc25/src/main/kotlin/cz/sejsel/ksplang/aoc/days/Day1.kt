package cz.sejsel.ksplang.aoc.days

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.std.*
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.swap2
import java.io.File

// Day 1
// https://adventofcode.com/2025/day/1
fun main() {
    val builder = KsplangBuilder()
    val program = builder.buildAnnotated(day1Part1())
    File("aoc25/ksplang/1-1.ksplang").writeText(program.toRunnableProgram())
    File("aoc25/ksplang/1-1.ksplang.json").writeText(program.toAnnotatedTreeJson())
    println("Generated program for day 1 part 1")
    val program2 = builder.buildAnnotated(day1Part2())
    File("aoc25/ksplang/1-2.ksplang").writeText(program2.toRunnableProgram())
    File("aoc25/ksplang/1-2.ksplang.json").writeText(program2.toAnnotatedTreeJson())
    println("Generated program for day 1 part 2")
}

// Just count zeroes
fun day1Part1() = day1 {
    // result dial_pos offset
    add()
    push(100)
    swap2()
    modulo()
    // result (dial_pos+offset)%100
    // result dial_pos
    ifZero {
        // result++
        swap2(); inc(); swap2()
    }
    // result dial_pos
}

// Calculate how many rotations past 0 were made:
// if new pos (before mod) is positive: new_pos/100
// otherwise: abs(new_pos)/100 + 1 UNLESS old_pos was 0, then it's just abs(new_pos)/100
fun day1Part2() = day1 {
    // result dial_pos offset
    dupSecond()
    add()
    // result dial_pos offset+dial_pos
    // result old_pos new_pos  -- no mod yet
    dup()
    zeroNotPositive()
    ifZero {
        //                v  new_pos is positive
        // result old_pos new_pos 0
        pushOn(0, 100)
        pop2()
        // result old_pos new_pos 100
        dupSecond()
        // result old_pos new_pos 100 new_pos
        div()
        // result old_pos new_pos new_pos/100
    } otherwise {
        //                v  new_pos is negative or 0
        // result old_pos new_pos 1
        pushOn(1, 100)
        pop2()
        // result old_pos new_pos 100
        dupSecond()
        abs()
        // result old_pos new_pos 100 abs(new_pos)
        div()
        // result old_pos new_pos abs(new_pos)/100
        // increase but only if we did not start at 0
        dupThird()
        ifZero {
            // result old_pos new_pos abs(new_pos)/100 0
            pop()
            // result old_pos new_pos abs(new_pos)/100
        } otherwise {
            // result old_pos new_pos abs(new_pos)/100 old_pos
            pop()
            inc()
            // result old_pos new_pos abs(new_pos)/100+1
        }
        // result old_pos new_pos rotations
    }
    // result old_pos new_pos rotations
    roll(4, 2)
    // new_pos rotations result old_pos
    pop()
    // new_pos rotations result
    add()
    // new_pos result+rotations
    swap2()
    // result+rotations new_pos
    push(100)
    swap2()
    modulo()
    // result+rotations new_pos%100
    // result dial_pos
}

/**
 *  Text input, numeric output
 *  @param handleRotation signature `result dial_pos offset -> result dial_pos`
 */
fun day1(handleRotation: ComplexBlock.() -> Unit) = buildComplexFunction("day1") {
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
        permute(
            "result dial_pos remaining_lines mult num next_input_index",
            "next_input_index remaining_lines result dial_pos mult num"
        )
        // next_input_index remaining_lines result dial_pos mult num
        mul()
        // next_input_index remaining_lines result dial_pos offset
        handleRotation()
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
