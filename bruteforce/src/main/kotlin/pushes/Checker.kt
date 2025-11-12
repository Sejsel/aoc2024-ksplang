package cz.sejsel.pushes

import cz.sejsel.KsplangRunner
import cz.sejsel.VALUES_PER_DIGIT_SUM
import cz.sejsel.ksplang.interpreter.Op
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.atomic.AtomicLong

interface SolutionChecker {
    fun checkSolutions(solutions: Map<Long, List<Op>>): Boolean
}

class VerificationSolutionChecker(val verificationBinaryPath: String = "verification/target/release/verification") : SolutionChecker {
    override fun checkSolutions(solutions: Map<Long, List<Op>>): Boolean {
        // please drink a verification can
        val input = solutions.map { (result, solution) ->
            val program = solution.joinToString(" ")
            "$result $program"
        }.joinToString("\n")

        val process = ProcessBuilder(listOf(verificationBinaryPath)).start()

        // Write the input to the stdin
        process.outputStream.buffered().use { it.write(input.toByteArray(Charsets.UTF_8)) }

        // Read the entire output from the stdout
        val output = process.inputStream.bufferedReader().readText()
        // Read the entire stderr output
        val error = process.errorStream.bufferedReader().readText()

        if (process.waitFor() != 0) {
            println("Verification program exited with non-zero exit code, dumping output and stderr")
            println("Output: $output")
            println("Error: $error")
            return false
        }

        return true
    }
}

/** Slow. **/
class RunKsplangPerProgramSolutionChecker(val runner: KsplangRunner, val maxThreadCount: Int = 12): SolutionChecker {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun checkSolutions(solutions: Map<Long, List<Op>>): Boolean {
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

