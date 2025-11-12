package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.auto
import cz.sejsel.ksplang.dsl.auto.const
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.auto.*
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 9
// https://adventofcode.com/2024/day/9
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day9Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/9-1.ksplang").writeText(program)
    println("Generated program for day 9 part 1, $instructionCount instructions")
    //val program2 = builder.build(day9Part2())
    //val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    //File("ksplang/9-2.ksplang").writeText(program2)
    //println("Generated program for day 9 part 2, $instructionCount2 instructions")
}

fun day9Part2() = buildComplexFunction {
    // [input]
    stacklen()
    // [input] stacklen
    auto("stacklen") { stacklen ->
        val disk = createDisk(stacklen)
        TODO()
    }
}

fun day9Part1() = buildComplexFunction {
    // [input]
    stacklen()
    // [input] stacklen
    auto("stacklen") { stacklen ->
        val disk = createDisk(stacklen)

        // Now, we move blocks to empty spaces. We scan from the front for empty spaces and from
        // the back for blocks, until they meet in the middle
        val front = copy(disk.from) // from the start
        val back = dec(add(disk.from, disk.len)) // from the end

        val stop = variable(false)
        whileNonZero({ not(stop) }) {
            // The following wouldn't work for inputs with only one file, otherwise guaranteed to be safe:
            set(front) to findUnsafe(front, (-1).const)

            // this is a reverse findUnsafe
            val continueReversing = variable(true)
            whileNonZero({ continueReversing }) {
                val value = yoink(back)
                ifBool(eq(value, (-1).const)) {
                    set(back) to dec(back)
                } otherwise {
                    set(continueReversing) to false
                }
            }

            ifBool(geq(front, back)) {
                set(stop) to true
            } otherwise {
                // swap front and back
                yeet(front, yoink(back))
                yeet(back, (-1).const)
            }
        }

        val checksum = variable(0)
        val index = variable(0)
        sliceForEach(disk) {
            ifBool(geq(it, 0.const)) {
                set(checksum) to add(checksum, mul(index, it))
            }
            set(index) to inc(index)
        }

        keepOnly(checksum)
    }
    leaveTop()
}

private fun Scope.createDisk(stacklen: Variable): Slice {
    // input ends with a newline, so we use [0:stacklen-1] as input
    val input = Slice(const(0), dec(stacklen))

    val allocator = Allocator(copy(stacklen))

    val sum = variable(0)
    sliceForEach(input) {
        set(it) to sub(it, 48.const)
        set(sum) to add(sum, it)
    }

    val disk = alloc(allocator, sum)

    val isFile = variable(true)
    val fileId = variable(0)
    val diskPos = variable(0)
    sliceForEach(input) {
        doNTimes(it) {
            ifBool(isFile) {
                set(disk[diskPos]) to fileId
            } otherwise {
                set(disk[diskPos]) to -1
            }

            set(diskPos) to inc(diskPos)
        }

        ifBool(isFile) {
            set(fileId) to add(fileId, 1)
        }

        set(isFile) to not(isFile)
    }
    return disk
}
