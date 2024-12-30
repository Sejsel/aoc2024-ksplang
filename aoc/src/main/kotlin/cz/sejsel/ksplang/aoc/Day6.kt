package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.CallResult1
import cz.sejsel.ksplang.dsl.auto.CallResult2
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.RestrictedAutoBlock
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun
import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 6
// https://adventofcode.com/2024/day/6
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day6Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/6-1.ksplang").writeText(program)
    println("Generated program for day 6 part 1, $instructionCount instructions")
    //val program2 = builder.build(day6Part2())
    //val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    //File("ksplang/6-2.ksplang").writeText(program2)
    //println("Generated program for day 6 part 2, $instructionCount2 instructions")
}

const val UP: Long = 0
const val RIGHT: Long = 1
const val DOWN: Long = 2
const val LEFT: Long = 3


fun day6Part1() = buildComplexFunction("day6part1") {
    // [input]
    stacklen()
    // [input] stacklen
    auto("stacklen") { stacklen ->
        val width = variable("width")
        val height = variable("height")
        findUnsafe(const(0), const('\n'.code)) { setTo(width) }
        countOccurrences(const('\n'.code), Slice(const(0), stacklen)) { setTo(height) }

        val initialPos = variable("pos")
        findUnsafe(const(0), const('^'.code)) { setTo(initialPos) }

        val x = variable("x")
        val y = variable("y")

        toXY(initialPos, width, height) { setTo(x, y) }

        val direction = variable("direction", UP)

        val isDone = variable("isDone", 1)
        doWhileNonZero(isDone) {
        }
    }
}

fun RestrictedAutoBlock.turnRight(direction: Parameter, useResult: CallResult1.() -> Unit) =
    runFun(direction, useResult) {
        // pos
        inc()
        push(4)
        swap2()
        // 4 pos+1
        modulo()
        // (pos+1)%4
    }

fun RestrictedAutoBlock.toXY(pos: Parameter, width: Parameter, height: Parameter, useResult: CallResult2.() -> Unit) =
    runFun(width, height, pos, useResult) {
        // width height pos
        auto("width", "height", "pos") { width, height, pos ->
            // There is a newline at the end of each line
            width.inc()

            // x = pos % width
            var x = variable("x")
            mod(pos, width) { setTo(x) }
            // y = pos / width
            var y = variable("y")
            div(pos, width) { setTo(y) }

            keepOnly(x, y)
        }
    }

fun RestrictedAutoBlock.isValid(x: Parameter, y: Parameter, width: Parameter, height: Parameter, useResult: CallResult1.() -> Unit) =
    runFun(width, height, x, y, useResult) {
        // width height pos
        auto("width", "height", "x", "y") { width, height, x, y ->
            // There is a newline at the end of each line
        }
    }
