package cz.sejsel

import cz.sejsel.pushes.VerificationSolutionChecker
import java.io.File
import java.time.Duration
import java.time.Instant.now
import kotlin.math.abs
import kotlin.math.min

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

enum class PraiseCombinatorType {
    ADD,
    ABSSUB,
    MUL,
    CURSEDDIV,
    FUNKCIA,
    REM,
    GCD,
    BITAND,
    BITSHIFT,
}

data class PraiseCombinator(
    val type: PraiseCombinatorType,
    val preIncrement: Boolean,
)

data class TrackImprovementsResult(
    val foundImprovements: Int,
    val duration: Duration,
) {
    fun print(name: String) {
        println("[${duration}] $name done, found $foundImprovements improvements")
    }
}

class FunkciaCache {
    private val cache = mutableMapOf<Pair<Long, Long>, Long>()

    fun get(a: Long, b: Long): Long = cache.getOrPut(a to b) { funkcia(a, b) }
}

fun main() {
    val funkciaCache = FunkciaCache()

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
    //val potentialAddresses = 0..1_048_576L
    val potentialAddresses = 0..10000L // TODO: Adjust to bigger range
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

    fun trackImprovements(name: String, block: () -> Unit): TrackImprovementsResult {
        val startImprovements = foundImprovements
        val startTime = now()
        block()
        val endTime = now()
        val duration = Duration.between(startTime, endTime)

        return TrackImprovementsResult(
            foundImprovements = foundImprovements - startImprovements,
            duration = duration
        ).also {
            it.print(name)
        }
    }


    var loopIndex = 0
    do {
        println("Starting loop $loopIndex")
        foundImprovements = 0

        if (loopIndex == 0) {
            // Praise the almighty hippo gods.
            listOf(
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80, 77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80)
                    to (solutions[2]!! + listOf(Op.Praise)),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80, 77, 225, 109, 32, 114, 225, 100, 32)
                    to (solutions[2]!! + listOf(Op.Praise, Op.Qeq)),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80, 77, 225, 109, 32, 114)
                    to (solutions[2]!! + listOf(Op.Praise, Op.Qeq, Op.Qeq)),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80, 77, 225, 109, 1)
                    to (solutions[2]!! + listOf(Op.Praise, Op.DigitSum, Op.GcdN)),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 1)
                    to (solutions[2]!! + listOf(Op.Praise, Op.Pop, Op.DigitSum, Op.GcdN)),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80, 77, 225)
                    to solutions[2]!! + listOf(Op.Praise, Op.Qeq, Op.Qeq, Op.Qeq),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 75, 83, 80)
                    to solutions[1]!! + listOf(Op.Praise),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32, 1)
                    to solutions[2]!! + listOf(Op.Praise, Op.Pop, Op.Pop, Op.DigitSum, Op.GcdN),
                listOf<Long>(77, 225, 109, 32, 114, 225, 100, 32)
                    to solutions[1]!! + listOf(Op.Praise, Op.Qeq),
                listOf<Long>(77, 225, 109, 32, 114)
                    to solutions[1]!! + listOf(Op.Praise, Op.Qeq, Op.Qeq),
                listOf<Long>(77, 225, 109, 1)
                    to solutions[1]!! + listOf(Op.Praise, Op.DigitSum, Op.GcdN),
                listOf<Long>(77, 225)
                    to solutions[1]!! + listOf(Op.Praise, Op.Qeq, Op.Qeq, Op.Qeq),
            ).forEach { (praiseOutput, praiseInit) ->
                trackImprovements("[total] praise output $praiseOutput") {
                    trackImprovements("praise keep one number") {
                        praiseOutput.forEachIndexed { i, n ->
                            val pops = praiseOutput.size - i - 1
                            val pop2s = praiseOutput.size - pops - 1
                            val properPraise = praiseInit + List(pops) { Op.Pop } + List(pop2s) { Op.Pop2 }
                            trySolution(n.toLong(), properPraise)
                        }
                    }

                    // TODO: Use sequences instead of lists to avoid copying
                    (2..min(4, praiseOutput.size - 1)).forEach { opCount ->
                        trackImprovements("$opCount op combinations") {

                            val options = PraiseCombinatorType.entries.flatMap { type ->
                                listOf(PraiseCombinator(type, false), PraiseCombinator(type, true))
                            }

                            // TODO: Could make version that just returns the indices to avoid copying
                            combinations((0..<praiseOutput.size).toList(), opCount) { praiseIndices ->
                                val pops = praiseOutput.size - praiseIndices.last() - 1
                                val praiseStart = praiseInit + List(pops / 3) { Op.Qeq } + List(pops % 3) { Op.Pop }
                                val gaps = mutableListOf<List<Op>>()
                                praiseIndices.asReversed().zip(praiseIndices.asReversed().drop(1)).forEach { (j, i) ->
                                    gaps.add(List(j - i - 1) { Op.Pop2 })
                                }
                                val finalCleanup = List(praiseIndices.first()) { Op.Pop2 }

                                combinationsWithReplacement(options, opCount - 1) { sequence ->
                                    var resultValue = praiseOutput[praiseIndices.last()]
                                    sequence.zip(praiseIndices.asReversed().drop(1)).forEach { (op, praiseIndex) ->
                                        val value = praiseOutput[praiseIndex]

                                        if (op.preIncrement) {
                                            if (resultValue == Long.MAX_VALUE) {
                                                // Overflow
                                                return@combinationsWithReplacement
                                            }
                                            resultValue += 1
                                        }

                                        when (op.type) {
                                            // TODO: Try using Math.addExact and multiplyExact
                                            //  with try catch and compare performance
                                            PraiseCombinatorType.ADD -> {
                                                // Inspired by Math.addExact
                                                val r = resultValue + value
                                                if (((resultValue xor r) and (value xor r)) < 0) {
                                                    // Overflow
                                                    return@combinationsWithReplacement
                                                }
                                                resultValue = r
                                            }

                                            PraiseCombinatorType.MUL -> {
                                                // Inspired by Math.multiplyExact
                                                val r = resultValue * value
                                                val ax = abs(resultValue)
                                                val ay = abs(value)
                                                if (((ax or ay) ushr 31 != 0L)) {
                                                    if (((value != 0L) && (r / value != resultValue)) ||
                                                        (resultValue == Long.MIN_VALUE && value == -1L)
                                                    ) {
                                                        // Overflow
                                                        return@combinationsWithReplacement
                                                    }
                                                }
                                                resultValue = r
                                            }

                                            PraiseCombinatorType.FUNKCIA -> {
                                                resultValue = funkciaCache.get(resultValue, value)
                                            }

                                            PraiseCombinatorType.ABSSUB -> {
                                                val r = resultValue - value
                                                if (((resultValue xor r) and (value xor r)) < 0) {
                                                    // Overflow
                                                    return@combinationsWithReplacement
                                                }
                                                if (r == Long.MIN_VALUE) {
                                                    // Overflow
                                                    return@combinationsWithReplacement
                                                }

                                                resultValue = abs(r)
                                            }

                                            PraiseCombinatorType.CURSEDDIV -> {
                                                if (value == 0L) {
                                                    // Division by zero
                                                    return@combinationsWithReplacement
                                                }

                                                if (resultValue == Long.MIN_VALUE && value == -1L) {
                                                    // Overflow
                                                    return@combinationsWithReplacement
                                                }

                                                resultValue = cursedDiv(resultValue, value)
                                            }

                                            PraiseCombinatorType.REM -> {
                                                if (value == 0L) {
                                                    // Division by zero
                                                    return@combinationsWithReplacement
                                                }

                                                resultValue %= value
                                            }

                                            PraiseCombinatorType.GCD -> {
                                                if (value == 0L && resultValue == Long.MIN_VALUE) {
                                                    // Overflow
                                                    return@combinationsWithReplacement
                                                }
                                                if (resultValue == 0L && value == Long.MIN_VALUE) {
                                                    // Overflow
                                                    return@combinationsWithReplacement
                                                }

                                                resultValue = abs(gcd(resultValue, value))
                                            }

                                            PraiseCombinatorType.BITAND -> {
                                                resultValue = resultValue and value
                                            }

                                            PraiseCombinatorType.BITSHIFT -> {
                                                if (resultValue < 0) {
                                                    // Invalid bitshift param
                                                    return@combinationsWithReplacement
                                                }
                                                if (resultValue > 63) {
                                                    resultValue = 0
                                                }

                                                resultValue = value shl resultValue.toInt()
                                            }
                                        }
                                    }

                                    if (resultValue !in targetValues) {
                                        return@combinationsWithReplacement
                                    }

                                    // We need to be able to stop early, for that we need to know the result already
                                    // Then we also want to optimize program sequences depending on current values,
                                    // so we have to redo the calculation of results - at least we can avoid all the
                                    // checks for whether it fits into a i64.
                                    val bestKnownSolutionSize = solutions[resultValue]?.size ?: Int.MAX_VALUE
                                    resultValue = praiseOutput[praiseIndices.last()]
                                    val program = praiseStart.toMutableList()
                                    sequence.zip(praiseIndices.asReversed().drop(1).mapIndexed { a, b -> a to b })
                                        .forEach { (op, indices) ->
                                            val (gap, praiseIndex) = indices
                                            val value = praiseOutput[praiseIndex]
                                            program.addAll(gaps[gap])
                                            if (program.size > bestKnownSolutionSize) {
                                                return@combinationsWithReplacement
                                            }

                                            if (op.preIncrement) {
                                                resultValue += 1
                                                program.add(Op.Increment)
                                                if (program.size > bestKnownSolutionSize) {
                                                    return@combinationsWithReplacement
                                                }
                                            }

                                            when (op.type) {
                                                PraiseCombinatorType.ADD -> {
                                                    if (digitSum(resultValue) == digitSum(digitSum(resultValue))) {
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Modulo)
                                                        program.add(Op.Universal)
                                                    } else {
                                                        program.addAll(solutions[0]!!)
                                                        program.add(Op.Universal)
                                                    }
                                                    resultValue += value
                                                }

                                                PraiseCombinatorType.ABSSUB -> {
                                                    if (digitSum(resultValue) == 1L) {
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Universal)
                                                    } else if (digitSum(resultValue) == digitSum(digitSum(resultValue))) {
                                                        check(solutions[1]!!.size > 4)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Modulo)
                                                        program.add(Op.Increment)
                                                        program.add(Op.Universal)
                                                    } else {
                                                        program.addAll(solutions[1]!!)
                                                        program.add(Op.Universal)
                                                    }
                                                    resultValue = abs(resultValue - value)
                                                }

                                                PraiseCombinatorType.MUL -> {
                                                    if (digitSum(resultValue) == 2L) {
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Universal)
                                                    } else if (digitSum(resultValue) == digitSum(digitSum(resultValue))) {
                                                        check(solutions[2]!!.size > 5)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Modulo)
                                                        program.add(Op.Increment)
                                                        program.add(Op.Increment)
                                                        program.add(Op.Universal)
                                                    } else {
                                                        program.addAll(solutions[2]!!)
                                                        program.add(Op.Universal)
                                                    }
                                                    resultValue *= value
                                                }

                                                PraiseCombinatorType.CURSEDDIV -> {
                                                    if (digitSum(resultValue) == 3L) {
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Universal)
                                                    } else if (digitSum(resultValue) == digitSum(digitSum(resultValue))) {
                                                        check(solutions[3]!!.size > 6)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.DigitSum)
                                                        program.add(Op.Modulo)
                                                        program.add(Op.Increment)
                                                        program.add(Op.Increment)
                                                        program.add(Op.Increment)
                                                        program.add(Op.Universal)
                                                    } else {
                                                        program.addAll(solutions[3]!!)
                                                        program.add(Op.Universal)
                                                    }
                                                    resultValue = cursedDiv(resultValue, value)
                                                }

                                                PraiseCombinatorType.FUNKCIA -> {
                                                    program.add(Op.Funkcia)
                                                    resultValue = funkciaCache.get(resultValue, value)
                                                }

                                                PraiseCombinatorType.REM -> {
                                                    program.add(Op.Remainder)
                                                    resultValue %= value
                                                }

                                                PraiseCombinatorType.GCD -> {
                                                    program.add(Op.Gcd2)
                                                    resultValue = abs(gcd(resultValue, value))
                                                }

                                                PraiseCombinatorType.BITAND -> {
                                                    program.add(Op.And)
                                                    resultValue = resultValue and value
                                                }

                                                PraiseCombinatorType.BITSHIFT -> {
                                                    program.add(Op.Bitshift)
                                                    if (resultValue > 63) {
                                                        resultValue = 0
                                                    }

                                                    resultValue = value shl resultValue.toInt()
                                                }
                                            }

                                            if (program.size > bestKnownSolutionSize) {
                                                return@combinationsWithReplacement
                                            }
                                        }

                                    program.addAll(finalCleanup)
                                    trySolution(resultValue, program)
                                }

                            }
                        }
                    }

                    // TODO: Implement the other thingy
                }
            }
        }

        // TODO: Try negate

        // try previous number and ++
        trackImprovements("increments") {
            targetValues.forEach { n ->
                solutions[n - 1]?.let { solution ->
                    trySolution(n, solution + listOf(Op.Increment))
                }
            }
        }

        // bitshift
        trackImprovements("bitshifts") {
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
        }

        // Number, CS, some amount of ++, funkcia
        trackImprovements("cs [++] funkcia") {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                val copy = digitSum(n)
                (0..7).forEach { increments ->
                    val result = funkciaCache.get(n, copy + increments)

                    if (result in targetValues) {
                        val incOps = List(increments) { Op.Increment }
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
        }

        // Number, CS, some amount of ++, bitshift
        trackImprovements("cs [++] bitshift") {
            targetValues.forEach { n ->
                val nSolution = solutions[n] ?: return@forEach

                val copy = digitSum(n).toInt() // guaranteed to be a small number
                (copy..<63).forEach { k ->
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
        }

        // CS, CS, bitshift, bitshift (needed for the optimal 512)
        trackImprovements("cs cs bitshift bitshift") {
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
        }

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
    print("Writing to file...")
    File("lastoptimization.txt").writeText(input)
    println("done")

    val checker = VerificationSolutionChecker()
    checker.checkSolutions(solutions)
}
