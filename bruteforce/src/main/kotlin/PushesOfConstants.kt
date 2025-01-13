package cz.sejsel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
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
        /*
        println("cs [++] funkcia")
        trackImprovements {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                val copy = digitSum(n)
                (0..7).forEach { increments ->
                    val incOps = (0..increments).map { Op.Increment }
                    TODO("we need funkcia")
                }
            }
        }.also {
            println("cs [++] funkcia done, found $it improvements")
        }
         */

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


    //for ((result, solution) in solutions) {
    //    println("$result: ${solution.joinToString(" ")}")
    //}

    val runner = KsplangRunner()
    val checker = SolutionChecker(runner)
    checker.checkSolutions(solutions)
}

class SolutionChecker(val runner: KsplangRunner, val maxThreadCount: Int = 12) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun checkSolutions(solutions: Map<Long, List<Op>>): Boolean {
        println("Checking solutions")
        var totalRanPrograms = AtomicLong(0)
        var wrongResults = AtomicLong(0)

        runBlocking(Dispatchers.Default.limitedParallelism(maxThreadCount)) {
            val jobs = mutableListOf<Job>()
            for ((result, solution) in solutions) {
                val job = launch {
                    val program = solution.joinToString(" ")
                    VALUES_PER_DIGIT_SUM.forEach { digitSum ->
                        val inputStack = listOf<Long>(digitSum)
                        val actualResult = runner.run(program, inputStack.joinToString(" "))
                        totalRanPrograms.incrementAndGet()
                        if (actualResult.outputStack.size != inputStack.size + 1) {
                            println("$result WRONG OUTPUT SIZE, input $inputStack, output ${actualResult.outputStack}")
                            wrongResults.incrementAndGet()
                            return@launch
                        }
                        if (actualResult.outputStack.last() != result) {
                            println("$result WRONG OUTPUT VALUE, input $inputStack, output ${actualResult.outputStack}")
                            wrongResults.incrementAndGet()
                            return@launch
                        }
                        if (actualResult.outputStack.dropLast(1) != inputStack) {
                            println("$result MALFORMED INPUT STACK, input $inputStack, output ${actualResult.outputStack}")
                            wrongResults.incrementAndGet()
                            return@launch
                        }
                    }
                }
                jobs.add(job)
            }

            jobs.forEach { it.join() }
        }

        println("Finished checks for ${solutions.size} solutions. Ran $totalRanPrograms programs, $wrongResults wrong results")
        return wrongResults.get() == 0L
    }
}

/**
 * Provides a value which has a specified digit sum for all possible digit sums.
 *
 * For example, `VALUES_PER_DIGIT_SUM[150]` provides a number which will have a digit sum of 150.
 */
val VALUES_PER_DIGIT_SUM = listOf(
    0L,
    1L,
    2L,
    3L,
    4L,
    5L,
    6L,
    7L,
    8L,
    9L,
    19L,
    29L,
    39L,
    49L,
    59L,
    69L,
    79L,
    89L,
    99L,
    199L,
    299L,
    399L,
    499L,
    599L,
    699L,
    799L,
    899L,
    999L,
    1999L,
    2999L,
    3999L,
    4999L,
    5999L,
    6999L,
    7999L,
    8999L,
    9999L,
    19999L,
    29999L,
    39999L,
    49999L,
    59999L,
    69999L,
    79999L,
    89999L,
    99999L,
    199999L,
    299999L,
    399999L,
    499999L,
    599999L,
    699999L,
    799999L,
    899999L,
    999999L,
    1999999L,
    2999999L,
    3999999L,
    4999999L,
    5999999L,
    6999999L,
    7999999L,
    8999999L,
    9999999L,
    19999999L,
    29999999L,
    39999999L,
    49999999L,
    59999999L,
    69999999L,
    79999999L,
    89999999L,
    99999999L,
    199999999L,
    299999999L,
    399999999L,
    499999999L,
    599999999L,
    699999999L,
    799999999L,
    899999999L,
    999999999L,
    1999999999L,
    2999999999L,
    3999999999L,
    4999999999L,
    5999999999L,
    6999999999L,
    7999999999L,
    8999999999L,
    9999999999L,
    19999999999L,
    29999999999L,
    39999999999L,
    49999999999L,
    59999999999L,
    69999999999L,
    79999999999L,
    89999999999L,
    99999999999L,
    199999999999L,
    299999999999L,
    399999999999L,
    499999999999L,
    599999999999L,
    699999999999L,
    799999999999L,
    899999999999L,
    999999999999L,
    1999999999999L,
    2999999999999L,
    3999999999999L,
    4999999999999L,
    5999999999999L,
    6999999999999L,
    7999999999999L,
    8999999999999L,
    9999999999999L,
    19999999999999L,
    29999999999999L,
    39999999999999L,
    49999999999999L,
    59999999999999L,
    69999999999999L,
    79999999999999L,
    89999999999999L,
    99999999999999L,
    199999999999999L,
    299999999999999L,
    399999999999999L,
    499999999999999L,
    599999999999999L,
    699999999999999L,
    799999999999999L,
    899999999999999L,
    999999999999999L,
    1999999999999999L,
    2999999999999999L,
    3999999999999999L,
    4999999999999999L,
    5999999999999999L,
    6999999999999999L,
    7999999999999999L,
    8999999999999999L,
    9999999999999999L,
    19999999999999999L,
    29999999999999999L,
    39999999999999999L,
    49999999999999999L,
    59999999999999999L,
    69999999999999999L,
    79999999999999999L,
    89999999999999999L,
    99999999999999999L,
    199999999999999999L,
    299999999999999999L,
    399999999999999999L,
    499999999999999999L,
    599999999999999999L,
    699999999999999999L,
    799999999999999999L,
    899999999999999999L,
    999999999999999999L,
    1999999999999999999L,
    2999999999999999999L,
    3999999999999999999L,
    4999999999999999999L,
    5999999999999999999L,
    6999999999999999999L,
    7999999999999999999L,
    8999999999999999999L,
)
