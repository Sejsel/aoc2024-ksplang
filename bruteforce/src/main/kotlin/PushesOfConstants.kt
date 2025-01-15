package cz.sejsel

import cz.sejsel.pushes.VerificationSolutionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

// TODO: Extract to interpreter
fun digitSum(n: Long): Long {
    if (n == Long.MIN_VALUE) {
        return 89
    }

    var num = abs(n)
    var result = 0L
    while (num != 0L) {
        result += num % 10
        num /= 10
    }
    return result
}

fun main() {
    val solutions: MutableMap<Long, List<Op>> = mutableMapOf()
    // Confirmed optimal solutions (by trying all programs up to 10 instructions):
    solutions[-1] = parseProgram("cs cs lensum ++ cs cs cs % qeq")
    solutions[0] = parseProgram("cs cs lensum cs funkcia")
    solutions[1] = parseProgram("cs cs lensum cs funkcia ++")
    solutions[2] = parseProgram("cs cs lensum ++ cs lensum")
    solutions[3] = parseProgram("cs cs lensum ++ cs lensum ++")
    solutions[4] = parseProgram("cs cs lensum ++ cs lensum ++ ++")
    solutions[5] = parseProgram("cs cs lensum ++ cs lensum ++ ++ ++")
    solutions[6] = parseProgram("cs cs lensum ++ cs lensum cs ++ funkcia")
    solutions[7] = parseProgram("cs cs lensum ++ cs lensum cs ++ funkcia ++")
    solutions[8] = parseProgram("cs cs lensum ++ cs lensum cs bitshift")
    solutions[12] = parseProgram("cs cs lensum ++ cs lensum ++ cs ++ funkcia")
    solutions[16] = parseProgram("cs cs lensum ++ cs lensum cs ++ bitshift")
    solutions[17] = parseProgram("cs cs lensum ++ cs lensum cs ++ bitshift ++")
    solutions[24] = parseProgram("cs cs lensum ++ cs lensum ++ cs bitshift")
    solutions[27] = parseProgram("cs cs lensum ++ cs lensum cs ++ tetr")
    solutions[28] = parseProgram("cs cs lensum ++ cs lensum cs ++ tetr ++")
    solutions[32] = parseProgram("cs cs lensum ++ cs lensum cs ++ ++ bitshift")
    solutions[48] = parseProgram("cs cs lensum ++ cs lensum ++ cs ++ bitshift")
    solutions[64] = parseProgram("cs cs lensum ++ cs lensum ++ ++ cs bitshift")
    solutions[77] = parseProgram("cs cs lensum cs funkcia ++ praise cs d qeq")
    solutions[7625597484987] = parseProgram("cs cs ++ lensum cs lensum ++ cs tetr")
    solutions[7625597484988] = parseProgram("cs cs ++ lensum cs lensum ++ cs tetr ++")

    val powersOfTwo = (0..62).map { 1L shl it }
    val negativePowersOfTwo = (0..63).map { (-1L shl it) }
    val positivePowersOfTwoMinusOne = ((0..62).map { (1L shl it) - 1 } + Long.MAX_VALUE)
    val potentialAddresses = 0..1_048_576L
    val smallNegativeNumbers = -1024..-1L

    val targetValues: Set<Long> =
        (powersOfTwo + negativePowersOfTwo + positivePowersOfTwoMinusOne + potentialAddresses + smallNegativeNumbers).toSet()

    var foundImprovements = 0

    fun trySolution(result: Long, solution: List<Op>) {
        require(result in targetValues)
        val existingSolution = solutions[result]
        if (existingSolution == null || existingSolution.size > solution.size) {
            solutions[result] = solution
            foundImprovements += 1
        }
    }

    fun trackImprovements(block: () -> Unit): Int {
        val startImprovements = foundImprovements
        block()
        return foundImprovements - startImprovements
    }


    var loopIndex = 0
    do {
        println("Starting loop $loopIndex")
        foundImprovements = 0

        // try previous number and ++
        println("increments")
        trackImprovements {
            targetValues.forEach { n ->
                solutions[n - 1]?.let { solution ->
                    trySolution(n, solution + listOf(Op.Increment))
                }
            }
        }.also {
            println("increments done, found $it improvements")
        }

        // bitshift
        println("bitshifts")
        trackImprovements {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                (1..63).forEach { shift ->
                    solutions[shift.toLong()]?.let { shiftSolution ->
                        val result = n shl shift
                        if (result in targetValues) {
                            trySolution(result, nSolution + shiftSolution + listOf(Op.Bitshift))
                        }
                    }
                }
            }
        }.also {
            println("bitshifts done, found $it improvements")
        }

        // Number, CS, some amount of ++, funkcia
        println("cs [++] funkcia")
        trackImprovements {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                val copy = digitSum(n)
                (0..7).forEach { increments ->
                    val result = funkcia(n, copy + increments)

                    if (result in targetValues) {
                        val incOps = (0..increments).map { Op.Increment }
                        val solution = buildList {
                            addAll(nSolution)
                            add(Op.DigitSum)
                            addAll(incOps)
                            add(Op.Funkcia)
                        }
                        trySolution(result, solution)
                    }
                }
            }
        }.also {
            println("cs [++] funkcia done, found $it improvements")
        }

        // Number, CS, some amount of ++, bitshift
        println("cs [++] bitshift")
        trackImprovements {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                val copy = digitSum(n).toInt() // guaranteed to be a small number
                (copy..63).forEach { k ->
                    val result = n shl k
                    if (result in targetValues) {
                        val increments = k - copy
                        val solution = buildList {
                            addAll(nSolution)
                            add(Op.DigitSum)
                            repeat(increments) { add(Op.Increment) }
                            add(Op.Bitshift)
                        }
                        trySolution(result, solution)
                    }
                }
            }
        }.also {
            println("cs [++] bitshift done, found $it improvements")
        }

        // CS, CS, bitshift, bitshift (needed for the optimal 512)
        println("cs cs bitshift bitshift")
        trackImprovements {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                val cs = digitSum(n)
                val cs2 = digitSum(cs).toInt()
                if (cs2 < 64 && cs shl cs2 < 64) {
                    val result = n shl (cs shl cs2).toInt()
                    if (result in targetValues) {
                        val solution = buildList {
                            addAll(nSolution)
                            add(Op.DigitSum)
                            add(Op.DigitSum)
                            add(Op.Bitshift)
                            add(Op.Bitshift)
                        }
                        trySolution(result, solution)
                    }
                }
            }
        }.also {
            println("cs cs bitshift bitshift done, found $it improvements")
        }

        // TODO: Math.multiplyExact

        val percentageDiscovered = solutions.size.toDouble() / targetValues.size * 100
        val totalSize = solutions.values.sumOf { it.size }
        println(
            "Finished loop $loopIndex, found improvements for $foundImprovements nums, reached ${
                String.format(
                    "%.3f",
                    percentageDiscovered
                )
            }% of target values, total size $totalSize"
        )
        loopIndex += 1
    } while (foundImprovements != 0)


    val input = solutions.map { (result, solution) ->
        val program = solution.joinToString(" ")
        "$result $program"
    }.joinToString("\n")
    File("lastoptimization.txt").writeText(input)

    val checker = VerificationSolutionChecker()
    checker.checkSolutions(solutions)
}
