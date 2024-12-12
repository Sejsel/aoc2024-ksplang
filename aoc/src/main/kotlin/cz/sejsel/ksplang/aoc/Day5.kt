package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonNegative
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.doWhilePositive
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 4
// https://adventofcode.com/2024/day/4
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day5Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/5-1.ksplang").writeText(program)
    println("Generated program for day 5 part 1, $instructionCount instructions")
    // val program2 = builder.build(day5Part2())
    // val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    // File("ksplang/5-2.ksplang").writeText(program2)
    // println("Generated program for day 5 part 2, $instructionCount2 instructions")
}

const val WIDTH = 100L
const val HEIGHT = 100L

const val INPUT_START_INDEX = WIDTH * HEIGHT

fun day5Part1() = buildComplexFunction {
    pushManyBottom(0, WIDTH * HEIGHT)
    // [matrix] [input]
    stacklen()
    // [matrix] [input] stacklen
    push(WIDTH * HEIGHT)
    subabs()
    // [matrix] [input] inputlen
    push('|'.code)
    push(INPUT_START_INDEX)
    // [matrix] [input] inputlen 124 10000
    dupThird()
    // [matrix] [input] inputlen 124 10000 inputlen
    countOccurrences()
    // [matrix] [input] inputlen pairs
    dup()
    // [matrix] [input] inputlen pairs pairs
    doWhileNonZero { // over pairs (the second ones)
        // pairs+1
        dec()
        // pairs
        dup()
        // pairs pairs
        mul(6)
        // pairs pairs*6
        push(INPUT_START_INDEX)
        // pairs pairs*6 10000
        add()
        // pairs 10000+pairs*6
        dup()
        add(3)
        // pairs 10000+pairs*6 10000+pairs*6+3
        parseNonNegativeNum('\n'.code); pop()
        // pairs 10000+pairs*6 num2
        swap2()
        // pairs num2 10000+pairs*6
        parseNonNegativeNum('|'.code); pop()
        // pairs num2 num1
        push(1)
        // pairs num2 num1 1
        permute("y x 1", "1 x y")
        // pairs 1 num1 num2
        setXY()
        // pairs
        CS()
    }
    pop()
    // [matrix] [input] inputlen pairs
    mul(6)
    add(INPUT_START_INDEX + 1)
    // [matrix] [input] inputlen 10000+pairs*6+1
    // [matrix] [input] inputlen pos
    dup()
    // [matrix] [input] inputlen pos pos
    dupThird()
    // [matrix] [input] inputlen pos pos inputlen
    add(INPUT_START_INDEX)
    // [matrix] [input] inputlen pos pos inputlen+10000
    subabs()
    // [matrix] [input] inputlen pos 10000+inputlen-pos
    dupSecond()
    // [matrix] [input] inputlen pos 10000+inputlen-pos pos
    push('\n'.code)
    // [matrix] [input] inputlen pos 10000+inputlen-pos pos 10
    permute("len from 10", "10 from len")
    countOccurrences()
    // [matrix] [input] inputlen pos lines
    push(0)
    // [matrix] [input] inputlen pos lines 0
    // [matrix] [input] inputlen pos lines valid         -- valid rows
    swap2()
    // [matrix] [input] inputlen pos valid lines
    doWhileNonZero { // over lines
        // inputlen pos valid lines+1
        dec()
        // inputlen pos valid lines
        permute("inputlen pos valid lines", "valid lines inputlen pos")
        // valid lines inputlen pos
        // valid lines inputlen pos
        dup()
        push('\n'.code)
        findUnsafe()
        // valid lines inputlen pos eol
        dupSecond()
        // valid lines inputlen pos eol pos
        subabs()
        // valid lines inputlen pos eol-pos
        add(1)
        div(3)
        // valid lines inputlen pos (eol-pos+1)/3
        // valid lines inputlen pos nums           -- numbers on the line starting with pos
        parseRow()
        // valid lines inputlen pos [numbers] nums
        dup()
        // valid lines inputlen pos [numbers] nums nums 0
        push(0)
        swap2()
        // valid lines inputlen pos [numbers] nums 0          nums
        // valid lines inputlen pos [numbers] nums inversions first_i

        doWhileNonZero { // over first_i
            // valid lines inputlen pos [numbers] nums inversions first_i+1
            dec()
            // valid lines inputlen pos [numbers] nums inversions first_i
            dup()
            // valid lines inputlen pos [numbers] nums inversions first_i first_i
            // valid lines inputlen pos [numbers] nums inversions first_i second_i

            ifZero { // this is such a lovely `while` loop, maybe I should finally make one
            } otherwise {
                doWhileNonZero { // over second_i
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i+1
                    dec()
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i
                    dup(); add(5)
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i second_i+5
                    dupNth()
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i numbers[second_i]
                    dupThird(); add(6)
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i numbers[second_i] first_i+6
                    dupNth()
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i numbers[second_i] numbers[first_i]
                    getXY()
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i is_inversion
                    permute("inversions first_i second_i is_inversion", "first_i second_i is_inversion inversions")
                    // valid lines inputlen pos [numbers] nums first_i second_i is_inversion inversions
                    add()
                    // valid lines inputlen pos [numbers] nums first_i second_i inversions
                    permute("first_i second_i inversions", "inversions first_i second_i")
                    // valid lines inputlen pos [numbers] nums inversions first_i second_i
                    dup()
                }
            }
            // valid lines inputlen pos [numbers] nums inversions <=0
            pop()
            // valid lines inputlen pos [numbers] nums inversions first_i
            CS()
        }
        // valid lines inputlen pos [numbers] nums inversions 0
        pop()
        // valid lines inputlen pos [numbers] nums inversions
        ifZero {
            pop()
            // valid lines inputlen pos [numbers] nums
            dup(); div(2); inc(); inc()
            // valid lines inputlen pos [numbers] nums nums/2+2
            dupNth()
            // valid lines inputlen pos [numbers] nums midNum
            swap2()
            // valid lines inputlen pos [numbers] midNum nums
            doWhileNonZero {
                dec()
                pop3()
                CS()
            }
            pop()
            // valid lines inputlen pos midNum
            roll(5, -1); add();
            // lines inputlen pos midNum+valid
            roll(4, 1);
            // valid lines inputlen pos
        } otherwise {
            pop()
            // valid lines inputlen pos [numbers] nums
            doWhileNonZero {
                dec()
                pop2()
                CS()
            }
            pop()
            // valid lines inputlen pos
        }
        // valid lines inputlen pos
        permute("valid lines inputlen pos", "inputlen pos valid lines")
        // inputlen pos valid lines
        CS()
    }
    pop()
    leaveTop()
}

/** ```pos nums -> newPos [numbers] nums``` */
private fun ComplexBlock.parseRow() = complexFunction {
    // pos nums
    dup()
    // pos nums nums
    dec()
    // pos nums nums-1  -- the last number on the row is terminated with newline
    // pos nums i
    doWhileNonZero { // over i
        // [numbers] pos nums i+1
        dec()
        // [numbers] pos nums i
        permute("pos nums i", "nums i pos")
        // [numbers] nums i pos
        parseNonNegativeNum(','.code)
        // [numbers] nums i num pos
        inc()
        // [numbers] nums i num pos
        permute("nums i num pos", "num pos nums i")
        // [numbers|num] pos nums i
        CS()
    }
    pop()
    // [numbers] newPos nums
    swap2()
    // [numbers] nums newPos
    parseNonNegativeNum('\n'.code)
    // [numbers] nums num newPos
    inc()
    // [numbers] nums num newPos
    permute("nums num newPos", "num newPos nums")
    // [numbers|num] newPos nums
    // [numbers] newPos nums
    dup()
    // [numbers] newPos nums nums
    permute("pos a b", "a pos b")
    // [numbers] nums newPos nums
    inc(); inc()
    // [numbers] nums newPos nums+2 1
    push(1)
    // [numbers] nums newPos 1 nums+2
    swap2()
    lroll()
    // newPos [numbers] nums

}

/** `x y -> s[x,y]` */
private fun ComplexBlock.getXY() = complexFunction {
    push(100)
    // x y width
    mul()
    // x y*width
    add()
    // y*width+x
    yoink()
    // s[y*width+x]
}

/** `n x y -> `; `s[x,y] = n` */
private fun ComplexBlock.setXY() = complexFunction {
    push(100)
    // n x y width
    mul()
    // n x y*width
    add()
    // n y*width+x
    yeet()
}
