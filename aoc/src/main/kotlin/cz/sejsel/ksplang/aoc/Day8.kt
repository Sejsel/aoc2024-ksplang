package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.runFun1
import cz.sejsel.ksplang.dsl.auto.runFun2
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 8
// https://adventofcode.com/2024/day/8
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day8Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/8-1.ksplang").writeText(program)
    println("Generated program for day 8 part 1, $instructionCount instructions")
    //val program2 = builder.build(day8Part2())
    //val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    //File("ksplang/8-2.ksplang").writeText(program2)
    //println("Generated program for day 8 part 2, $instructionCount2 instructions")
}

private val NEWLINE = const('\n'.code)
private val SPACE = const(' '.code)

fun day8Part1() = day8()
//fun day8Part2() = day8()

fun day8() = buildComplexFunction {
    // [input]
    stacklen()
    // [input] stacklen
    auto("stacklen") { stacklen ->
        val input = Slice(const(0), stacklen)

        val height = countOccurrences(NEWLINE, input)
        val width = div(stacklen, add(height, 1))

        val totalAntinodes = variable(0)

        doNTimes(stacklen) { pos ->
            val (x, y) = toXY(pos, width, height)

            // This may be a newline char
            ifBool(isValid(x, y, width, height)) {
                val isAntinode = variable(false)
                doNTimes(stacklen) { pos2 ->
                    val (antennaX, antennaY) = toXY(pos2, width, height)

                    // This may be a newline char
                    ifBool(isValid(antennaX, antennaY, width, height)) {
                        val xDistance = subabs(antennaX, x)
                        val yDistance = subabs(antennaY, y)

                        val notSame = or(xDistance, yDistance)
                        val frequency = yoink(toPos(antennaX, antennaY, width))
                        val hasFrequency = not(eq(frequency, '.'.code))
                        ifBool(and(notSame, hasFrequency)) {
                            ifBool(isAntenna(add(antennaX, xDistance), add(antennaY, yDistance), width, height, frequency)) {
                                set(isAntinode) to true
                            }
                        }
                    }
                }

                ifBool(isAntinode) {
                    set(totalAntinodes) to add(totalAntinodes, 1)
                }
            }
        }

        keepOnly(totalAntinodes)
    }
    leaveTop()
}

private fun Scope.isAntenna(x: Parameter, y: Parameter, width: Parameter, height: Parameter, frequency: Parameter): Variable {
    val result = variable(false)
    ifBool(isValid(x, y, width, height)) {
        val freq = yoink(toPos(x, y, width))
        ifBool(eq(frequency, freq)) {
            set(result) to true
        }
    }
    return result
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

private fun Scope.isValid(x: Parameter, y: Parameter, width: Parameter, height: Parameter) =
    runFun1(width, height, x, y) {
        auto("width", "height", "x", "y") { width, height, x, y ->
            // x >= 0 && x < width && y >= 0 && y < height
            val xValid = isInRange(x, const(0), add(width, -1))
            val yValid = isInRange(y, const(0), add(height, -1))
            val valid = and(xValid, yValid)

            keepOnly(valid)
        }
    }
