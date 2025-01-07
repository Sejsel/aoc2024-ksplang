package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.dsl.auto.runFun2
import cz.sejsel.ksplang.dsl.auto.set
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

val UP = const(0)
val RIGHT = const(1)
val DOWN = const(2)
val LEFT = const(3)


fun day6Part1() = buildComplexFunction("day6part1") {
    // [input]
    stacklen()
    // [input] stacklen
    auto("stacklen") { stacklen ->
        val newline = const('\n'.code)
        val input = Slice(const(0), stacklen)

        val width = findUnsafe(const(0), newline)
        val height = countOccurrences(newline, input)

        val initialPos = findUnsafe(const(0), const('^'.code))

        val (x, y) = toXY(initialPos, width, height)

        val direction = variable(UP)

        doWhileNonZero({ isValid(x, y, width, height) }) {
            yeet(toPos(x, y, width), const('X'.code))

            val oldX = copy(x)
            val oldY = copy(y)

            ifBool({ eq(direction, UP) }) {
                set(y) to add(y, -1)
            }
            ifBool({ eq(direction, RIGHT) }) {
                set(x) to add(x, 1)
            }
            ifBool({ eq(direction, DOWN) }) {
                set(y) to add(y, 1)
            }
            ifBool({ eq(direction, LEFT) }) {
                set(x) to add(x, -1)
            }

            val blockAhead = variable(0)
            ifBool({ isValid(x, y, width, height) }) {
                val nextPos = toPos(x, y, width)
                ifBool({ eq(yoink(nextPos), '#'.code) }) {
                    set(blockAhead) to 1
                }
            }

            ifBool({ blockAhead }) {
                set(direction) to turnRight(direction)
                set(x) to oldX
                set(y) to oldY
            }
        }

        val visited = countOccurrences(const('X'.code), input)
        keepOnly(visited)
    }
    // At this point, you can look at the text output and see the painted path
    leaveTop()
}

fun Scope.turnRight(direction: Parameter) = runFun1(direction) {
    // pos
    inc()
    push(4)
    swap2()
    // 4 pos+1
    modulo()
    // (pos+1)%4
}

fun Scope.toPos(x: Parameter, y: Parameter, width: Parameter) = runFun1(x, y, width) {
    auto("x", "y", "width") { x, y, width ->
        // There is a newline at the end of each line
        set(width) to add(width, 1)

        // pos = y * width + x
        val pos = add(mul(y, width), x)

        keepOnly(pos)
    }
}

fun Scope.toXY(pos: Parameter, width: Parameter, height: Parameter) = runFun2(width, height, pos) {
    // width height pos
    auto("width", "height", "pos") { width, height, pos ->
        // There is a newline at the end of each line
        set(width) to add(width, 1)

        // x = pos % width
        val x = mod(pos, width)
        // y = pos / width
        val y = div(pos, width)

        keepOnly(x, y)
    }
}

fun Scope.isValid(x: Parameter, y: Parameter, width: Parameter, height: Parameter) = runFun1(width, height, x, y) {
    // width height pos
    auto("width", "height", "x", "y") { width, height, x, y ->
        // x >= 0 && x < width && y >= 0 && y < height
        val xValid = isInRange(x, const(0), add(width, -1))
        val yValid = isInRange(y, const(0), add(height, -1))
        val valid = and(xValid, yValid)

        keepOnly(valid)
    }
}
