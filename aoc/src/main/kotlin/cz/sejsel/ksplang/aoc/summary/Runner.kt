package cz.sejsel.ksplang.aoc.summary

import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

data class Result(
    val outputStack: List<Long>,
    val executionTime: Duration,
    val instructionsExecuted: Long
)

class KsplangRunner(
) {
    /**
     * Runs a ksplang program using a `ksplang` interpreter in PATH (see https://github.com/ksp/ksplang).
     *
     * @param maxStackSize Maximum stack size (amount of int64 elements on the program stack)
     * */
    fun run(
        program: String,
        input: String,
        maxStackSize: Long = 2097152,
        inputText: Boolean = false,
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
                add("ksplang")
                add("--stats")
                add("--max-stack-size")
                add(maxStackSize.toString())
                if (inputText) {
                    add("--text-input")
                }
                add(programFile.toString())
            }).start()

            // Write the input to the stdin
            process.outputStream.buffered().use { it.write(input.toByteArray(Charsets.UTF_8)) }

            // Read the entire output from the stdout
            val output = process.inputStream.bufferedReader().readText()
            // Read the entire stderr output
            val error = process.errorStream.bufferedReader().readText()

            if (process.waitFor() != 0) {
                throw RuntimeException("Program exited with non-zero exit code, stderr: $error")
            }

            // Parse the output as a list of longs
            val outputStack = output.trim().split("\\s+".toRegex()).map { it.toLong() }

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
        val executionTime = Duration.parse("PT${seconds.replace('s', 'S')}")
        val instructionsExecuted = lines[1].removePrefix("Instructions executed: ").split(" ")[0].toLong()
        return Pair(executionTime, instructionsExecuted)
    }
}
