package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 4
// https://adventofcode.com/2024/day/4
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day4Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/4-1.ksplang").writeText(program)
    println("Generated program for day 4 part 1, $instructionCount instructions")
    //val program2 = builder.build(day4Part2())
    //val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    //File("ksplang/4-2.ksplang").writeText(program2)
    //println("Generated program for day 4 part 2, $instructionCount2 instructions")
}


fun day4Part1() = buildComplexFunction {
    // [stack]
    stacklen()
    // [stack] inputlen
    push('\n'.code)
    push(0)
    // [stack] inputlen 10 0
    dupThird()
    // [stack] inputlen 10 0 inputlen
    countOccurrences()
    // [stack] inputlen newlines
    dupAb()
    // [stack] inputlen newlines inputlen newlines
    swap2(); div()
    // [stack] inputlen newlines inputlen/newlines
    dec() // note that the newlines are included in the count, but we exclude it from width
    // [stack] inputlen height width
    swap2()
    // [stack] inputlen width height
    pop3()
    // [stack] width height
    push(0)
    // [stack] width height 0
    // [stack] width height count
    permute("width height count", "count width height")
    // [stack] count width height

    // horizontal
    findHorizontal('X'.code, 'M'.code, 'A'.code, 'S'.code)
    findHorizontal('S'.code, 'A'.code, 'M'.code, 'X'.code)
    // [stack] count width height
}

fun ComplexBlock.findHorizontal(first: Int, second: Int, third: Int, fourth: Int) = complexFunction {
    // count width height
    dup()
    // count width height height
    // count width height y
    doWhileNonZero { // over y
        // count width height y+1
        dec()
        // count width height y
        dupThird()
        // count width height y width
        add(-3) // we cannot be too close to the right edge
        // count width height y width-3
        // count width height y x
        swap2()
        // count width height x y
        doWhileNonZero { // over x
            // count width height x+1 y
            swap2(); dec(); swap2()
            // count width height x y
            dupFourth(); dupFourth(); dupFourth(); dupFourth()
            // count width height x y width height x y
            getXY()
            // count width height x y width height s[x,y]
            ifIs(first) {
                // count width height x y width height s[x,y]
                pop()
                // count width height x y width height
                dupFourth(); dupFourth()
                // count width height x y width height x y
                swap2(); inc(); swap2()
                // count width height x y width height x+1 y
                getXY()
                // count width height x y width height s[x+1,y]
                ifIs(second) {
                    pop()
                    dupFourth(); dupFourth()
                    swap2(); inc(); inc(); swap2()
                    getXY()
                    ifIs(third) {
                        pop()
                        dupFourth(); dupFourth()
                        swap2(); inc(); inc(); inc(); swap2()
                        getXY()
                        ifIs(fourth) {
                            // count width height x y width height s[x+3,y]
                            pop(); pop(); pop()
                            // count width height x y
                            permute("count width height x y", "width height x y count")
                            inc()
                            permute("width height x y count", "count width height x y")
                            // count+1 width height x y
                        } otherwise {
                            pop(); pop(); pop()
                        }
                    } otherwise {
                        pop(); pop(); pop()
                    }
                } otherwise {
                    pop(); pop(); pop()
                }
            } otherwise {
                pop(); pop(); pop()
            }
            // count width height x y
            swap2()
            // count width height y x
            CS()
            // count width height y x CS
            permute("y x CS", "x y CS")
            // count width height x y CS
        }
        // count width height 0 y
        pop2()
        // count width height y
        CS()
    }
    // count width height 0
    pop()
    // count width height
}

/** `width height x y -> width height s[x,y]` */
fun ComplexBlock.getXY() = complexFunction {
    // we need s[y*(width+1)+x], +1 is for the newline

    // width height x y
    dupFourth()
    // width height x y width
    inc()
    // width height x y width+1
    mul()
    // width height x y*(width+1)
    add()
    // width height y*(width+1)+x
    yoink()
    // width height s[y*(width+1)+x]
}
