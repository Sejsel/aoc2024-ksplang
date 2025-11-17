package cz.sejsel.ksplang.benchmarks.rust

import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

data class Result(
    val outputStack: List<Long>,
    val executionTime: Duration,
    val instructionsExecuted: Long
)

class RustKsplangRunner(
    val pathToInterpreter: String,
    /** Maximum stack size (amount of int64 elements on the program stack) */
    val maxStackSize: Long = 2097152,
    /** Maximum operation limit (amount of operations that can be executed) */
    val defaultOpLimit: Long = Long.MAX_VALUE,
    val optimize: Boolean = false,
) {
    fun run(
        program: String,
        input: List<Long>,
    ): Result {
        var programFile: Path? = null
        try {
            // Store the program as a temporary file
            programFile = createTempFile(
                prefix = "ksplang_",
            )
            programFile.writeText(program)

            // Run the program
            val process = ProcessBuilder(buildList {
                add(pathToInterpreter)
                add("--stats")
                add("--max-stack-size"); add(maxStackSize.toString())
                add("--op-limit"); add(defaultOpLimit.toString())
                if (optimize) add("--optimize")
                add(programFile.toString())
            }).start()

            // Write the input to the stdin
            process.outputStream.buffered().use { it.write(input.joinToString(" ").toByteArray()) }

            // Read the entire output from the stdout
            val output = process.inputStream.bufferedReader().readText()
            // Read the entire stderr output
            val error = process.errorStream.bufferedReader().readText()

            if (process.waitFor() != 0) {
                throw RuntimeException("Program exited with non-zero exit code, stderr: $error")
            }

            // Parse the output as a list of longs

            val outputStack =if (optimize) {
                // Hack to work around current debugging output of exyi's --optimize
                val finalStateLine = output.lines().indexOfFirst { it.contains("final state") }
                // horribly inefficient, but eh, good enough
                output.lines().drop(finalStateLine + 1).joinToString(" ").trim().split("\\s+".toRegex()).map { it.toLong() }
            } else {
                output.trim().split("\\s+".toRegex()).map { it.toLong() }
            }

            val stats = parseStats(error)
            return Result(
                outputStack = outputStack,
                executionTime = stats.first,
                instructionsExecuted = stats.second
            )
        } finally {
            programFile?.toFile()?.delete()
        }
    }

    private fun parseStats(str: String): Pair<Duration, Long> {
        // Example output:
        // Execution time: 26.610462169s
        // Instructions executed: 2460870296 (92.5M/s)

        val lines = str.lines()
        val seconds = lines[0].removePrefix("Execution time: ")
        val executionTime = if (seconds.contains("ms")) {
            seconds.trim().removeSuffix("ms").toDouble().let { Duration.ofMillis(it.toLong()) }
        } else if (seconds.contains("ns")) {
            seconds.trim().removeSuffix("ns").toDouble().let { Duration.ofNanos(it.toLong()) }
        } else if (seconds.contains("µs")) {
            seconds.trim().removeSuffix("µs").toDouble().let { Duration.ofNanos(it.toLong() * 1000) }
        } else {
            Duration.parse("PT${seconds.replace('s', 'S')}")
        }
        val instructionsExecuted = lines[1].removePrefix("Instructions executed: ").split(" ")[0].toLong()
        return Pair(executionTime, instructionsExecuted)
    }
}
