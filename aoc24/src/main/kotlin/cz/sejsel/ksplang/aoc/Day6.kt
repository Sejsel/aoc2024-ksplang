package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
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
    val program2 = builder.build(day6Part2())
    val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    File("ksplang/6-2.ksplang").writeText(program2)
    println("Generated program for day 6 part 2, $instructionCount2 instructions")
}

private val UP = const(0)
private val RIGHT = const(1)
private val DOWN = const(2)
private val LEFT = const(3)

private val RIGHT_BIT = const(0b0001_00000000)
private val UP_BIT = const(0b0010_00000000)
private val LEFT_BIT = const(0b0100_00000000)
private val DOWN_BIT = const(0b1000_00000000)

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

fun day6Part2() = buildComplexFunction("day6part2") {
    // [input]
    stacklen()
    // [input] stacklen
    push(0)
    swap2()
    // [input] 0 stacklen
    yoinkSlice()
    // [input] [input] stacklen
    push(0)
    swap2()
    // [input] [input] 0 stacklen
    yoinkSlice()
    // [input] [input] [input] stacklen
    auto("stacklen") { stacklen ->
        val newline = const('\n'.code)
        val input = Slice(const(0), stacklen)
        val loopMap = Slice(stacklen, stacklen)
        val loopResultMap = Slice(add(stacklen, stacklen), stacklen)

        val width = findUnsafe(const(0), newline)
        val height = countOccurrences(newline, input)

        val initialPos = findUnsafe(const(0), const('^'.code))
        val (startX, startY) = toXY(initialPos, width, height)

        val x = copy(startX)
        val y = copy(startY)

        val initialDirection = UP
        val direction = variable(initialDirection)

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

            val validAhead = isValid(x, y, width, height)
            val blockAhead = variable(false)
            ifBool({ validAhead }) {
                val nextPos = toPos(x, y, width)
                ifBool({ eq(yoink(nextPos), '#'.code) }) {
                    set(blockAhead) to true
                }
            }

            // Try to place a block in front and see if this would create a loop
            ifBool({ and(validAhead, not(blockAhead)) }) {
                copySlice(input, loopMap.from)

                // Set a new block in front in the copy
                val posOffset = toPos(x, y, width)
                yeet(add(loopMap.from, posOffset), const('#'.code))

                ifBool({ isLoop(startX, startY, initialDirection, width, height, loopMap) }) {
                    yeet(add(loopResultMap.from, posOffset), const('O'.code))
                }
            }

            ifBool({ blockAhead }) {
                set(direction) to turnRight(direction)
                set(x) to oldX
                set(y) to oldY
            }
        }

        val loops = countOccurrences(const('O'.code), loopResultMap)
        keepOnly(loops)
    }
    // At this point, you can look at the text output and see the painted path
    leaveTop()
}

fun Scope.isLoop(
    x: Parameter,
    y: Parameter,
    direction: Parameter,
    width: Parameter,
    height: Parameter,
    simInput: Slice
) =
    runFun1(x, y, direction, width, height, simInput.from, simInput.len) {
        auto(
            "x",
            "y",
            "direction",
            "width",
            "height",
            "mapFrom",
            "mapLen"
        ) { x, y, direction, width, height, mapFrom, mapLen ->
            val map = Slice(mapFrom, mapLen)

            val looped = variable(false)
            val stayedInPlace = variable(false)

            doWhileNonZero({ and(not(looped), isValid(x, y, width, height)) }) {
                // We use a different symbol for the path because in this copy, the "#" marked path exists too.
                // We also need to use a different symbol for each direction because we stay in the same location upon turning
                val oldPos = add(map.from, toPos(x, y, width))
                val oldSymbol = yoink(oldPos)
                val oldX = copy(x)
                val oldY = copy(y)

                ifBool({ and(eq(oldSymbol, 'L'.code), not(stayedInPlace)) }) {
                    set(looped) to true
                }

                val status = yoink(oldPos)
                ifBool({ eq(direction, UP) }) {
                    ifBool({ bitand(status, UP_BIT) }) {
                        set(looped) to true
                    }
                    yeet(oldPos, bitor(status, UP_BIT))

                    set(y) to add(y, -1)
                }
                ifBool({ eq(direction, RIGHT) }) {
                    ifBool({ bitand(status, RIGHT_BIT) }) {
                        set(looped) to true
                    }
                    yeet(oldPos, bitor(status, RIGHT_BIT))

                    set(x) to add(x, 1)
                }
                ifBool({ eq(direction, DOWN) }) {
                    ifBool({ bitand(status, DOWN_BIT) }) {
                        set(looped) to true
                    }
                    yeet(oldPos, bitor(status, DOWN_BIT))

                    set(y) to add(y, 1)
                }
                ifBool({ eq(direction, LEFT) }) {
                    ifBool({ bitand(status, LEFT_BIT) }) {
                        set(looped) to true
                    }
                    yeet(oldPos, bitor(status, LEFT_BIT))

                    set(x) to add(x, -1)
                }

                val blockAhead = variable(0)
                ifBool({ isValid(x, y, width, height) }) {
                    val nextPos = add(map.from, toPos(x, y, width))
                    ifBool({ eq(yoink(nextPos), '#'.code) }) {
                        set(blockAhead) to true
                    }
                }
                set(stayedInPlace) to false

                ifBool({ blockAhead }) {
                    set(direction) to turnRight(direction)
                    set(x) to oldX
                    set(y) to oldY
                    set(stayedInPlace) to true
                }
            }

            keepOnly(looped)
        }
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

private fun Scope.toPos(x: Parameter, y: Parameter, width: Parameter) = runFun1(x, y, width) {
    auto("x", "y", "width") { x, y, width ->
        // There is a newline at the end of each line
        set(width) to add(width, 1)

        // pos = y * width + x
        val pos = add(mul(y, width), x)

        keepOnly(pos)
    }
}

private fun Scope.toXY(pos: Parameter, width: Parameter, height: Parameter) = runFun2(width, height, pos) {
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

private fun Scope.isValid(x: Parameter, y: Parameter, width: Parameter, height: Parameter) = runFun1(width, height, x, y) {
    auto("width", "height", "x", "y") { width, height, x, y ->
        // x >= 0 && x < width && y >= 0 && y < height
        val xValid = isInRange(x, const(0), add(width, -1))
        val yValid = isInRange(y, const(0), add(height, -1))
        val valid = and(xValid, yValid)

        keepOnly(valid)
    }
}
