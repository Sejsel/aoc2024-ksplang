package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonNegative
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.extract
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.orIfNonZero
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 2
// https://adventofcode.com/2024/day/2
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day2Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/2-1.ksplang").writeText(program)
    println("Generated program for day 2 part 1, $instructionCount instructions")
}

fun day2Part1() = day2(validityCheck = checkValidity())

// We are using text input for this - we need the newlines.
fun day2(validityCheck: ComplexFunction) = buildComplexFunction("day2") {
    // [stack]
    stacklen()
    // [stack] inputlen
    push('\n'.code)
    // [stack] inputlen 10
    push(0)
    // [stack] inputlen 10 0
    dupThird()
    // [stack] inputlen 10 0 inputlen
    countOccurrences()
    // [stack] inputlen #newlines
    push(0)
    push(0)
    // [stack] inputlen #newlines 0      0
    // [stack] inputlen #newlines result pos
    permute("newlines result pos", "result pos newlines")
    // [stack] inputlen result pos newlines
    // [stack] inputlen result pos i

    doWhileNonZero { // over i (newlines remaining)
        // [stack] inputlen result pos i+1
        dec()
        // [stack] inputlen result pos i
        dupSecond()
        // [stack] inputlen result pos i pos
        push('\n'.code)
        // [stack] inputlen result pos i pos 10
        findUnsafe()
        // [stack] inputlen result pos i index(newline)
        push(' '.code)
        // [stack] inputlen result pos i index(newline) 32
        dupFourth()
        // [stack] inputlen result pos i index(newline) 32 pos
        permute("index 32 pos", "32 pos index")
        // [stack] inputlen result pos i 32 pos index(newline)
        dupSecond()
        subabs()
        // [stack] inputlen result pos i 32 pos index(newline)-pos
        countOccurrences()
        // [stack] inputlen result pos i #spaces
        dup()
        // [stack] inputlen result pos i #spaces j
        doWhileNonZero { // over j (words)
            // [stack] inputlen result [numbers] pos i #spaces j+1
            dec()
            // [stack] inputlen result [numbers] pos i #spaces j
            permute("pos i spaces j", "i spaces j pos")
            // [stack] inputlen result [numbers] i #spaces j pos
            parseNonNegativeNum(' '.code)
            // [stack] inputlen result [numbers] i #spaces j new pos
            inc()
            // [stack] inputlen result [numbers] i #spaces j new pos+1
            permute("i spaces j new pos", "new pos i spaces j")
            // [stack] inputlen result [numbers|new] pos i #spaces j
            CS()
        }
        pop()
        // [stack] inputlen result [numbers] pos i #spaces
        permute("pos i spaces", "i spaces pos")
        // [stack] inputlen result [numbers] i #spaces pos
        parseNonNegativeNum('\n'.code)
        // [stack] inputlen result [numbers] i #spaces new pos
        inc()
        // [stack] inputlen result [numbers] i #spaces new pos+1
        // [stack] inputlen result [numbers] i #spaces new pos
        permute("i spaces new pos", "new i spaces pos")
        // [stack] inputlen result [numbers|new] i #spaces pos
        // [stack] inputlen result [numbers] i #spaces pos
        +validityCheck
        // [stack] inputlen result pos i
        swap2()
        // [stack] inputlen result pos i
        CS()
    }
    pop()
    pop()
    leaveTop()
}

fun checkValidity() = buildComplexFunction("isValid") {
    // result [numbers] i #spaces pos
    dupFifth()
    // result [numbers] i #spaces pos secondLastNum
    dupFifth()
    // result [numbers] i #spaces pos secondLastNum lastNum
    negate()
    // result [numbers] i #spaces pos secondLastNum -lastNum
    add()
    // result [numbers] i #spaces pos secondLastNum-lastNum
    sgn()
    // result [numbers] i #spaces pos sgn(secondLastNum-lastNum)
    //                                                   ^ -1 for ascending sequence (from stack bottom)
    //                                                   ^  0 for invalid sequence (can treat as either)
    //                                                   ^  1 for descending sequence
    inc()
    // result [numbers] i #spaces pos isAscending?0:1/2
    ifZero {
        // if ascending
        push(1)
        push(3)
    } orIfNonZero {
        // if descending
        push(-3)
        push(-1)
    }
    // result [numbers] i #spaces pos isAscending?0:1/2 minDiff maxDiff
    pop3()
    // result [numbers] i #spaces pos minDiff maxDiff
    push(0)
    // result [numbers] i #spaces pos minDiff maxDiff 0
    // result [numbers] i #spaces pos minDiff maxDiff mistakes
    permute("spaces pos min max mistakes", "pos mistakes min max spaces")
    // result [numbers] i pos mistakes minDiff maxDiff spaces
    doWhileNonZero { // over spaces
        // result [numbers] i pos mistakes minDiff maxDiff spaces+1
        dec()
        // result [numbers] i pos mistakes minDiff maxDiff spaces
        dupEighth()
        dupEighth()
        // result [numbers] i pos mistakes minDiff maxDiff spaces prevLast last
        swap2(); negate(); add()
        // result [numbers] i pos mistakes minDiff maxDiff spaces last-prevLast
        dupFourth()
        dupFourth()
        // result [numbers] i pos mistakes minDiff maxDiff spaces last-prevLast minDiff maxDiff
        isInRange()
        // result [numbers] i pos mistakes minDiff maxDiff spaces ok?1:0
        zeroNotPositive()
        // result [numbers] i pos mistakes minDiff maxDiff spaces ok?0:1
        permute("mistakes min max spaces ok", "min max spaces ok mistakes")
        // result [numbers] i pos minDiff maxDiff spaces ok?0:1 mistakes
        add()
        // result [numbers] i pos minDiff maxDiff spaces mistakes
        permute("min max spaces mistakes", "mistakes min max spaces")
        // result [numbers] i pos mistakes minDiff maxDiff spaces
        popNth(7) // remove last number in [numbers]
        CS()
    }
    // result firstnum i pos mistakes minDiff maxDiff 0
    pop(); pop(); pop()
    // result firstnum i pos mistakes
    pop4()
    // result i pos mistakes
    ifZero {
        // increment result
        pop()
        permute("result i pos", "i pos result")
        inc()
        permute("i pos result", "result i pos")
    } orIfNonZero {
        pop()
    }
    // result i pos
}

