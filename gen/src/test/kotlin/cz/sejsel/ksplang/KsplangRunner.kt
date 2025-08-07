package cz.sejsel.ksplang

import arrow.core.getOrElse
import cz.sejsel.ksplang.interpreter.VMOptions
import cz.sejsel.ksplang.interpreter.parseProgram
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

interface KsplangRunner {
    fun run(program: String, input: List<Long>): List<Long>
}

typealias DefaultKsplangRunner = KotlinKsplangRunner

class KotlinKsplangRunner(
    /** Maximum stack size (amount of int64 elements on the program stack) */
    val maxStackSize: Long = 2097152,
    /** Maximum operation limit (amount of operations that can be executed) */
    val defaultOpLimit: Long = 100000
) : KsplangRunner {
    override fun run(program: String, input: List<Long>): List<Long> {
        val ops = parseProgram(program)

        val result = cz.sejsel.ksplang.interpreter.run(
            ops = ops,
            options = VMOptions(
                maxStackSize = maxStackSize.toInt(),
                initialStack = input,
                maxOpCount = defaultOpLimit,
            ),
        )

        return result.getOrElse { throw RuntimeException("Program execution failed: $it") }.stack
    }
}

class RustKsplangRunner(
    /** Maximum stack size (amount of int64 elements on the program stack) */
    val maxStackSize: Long = 2097152,
    /** Maximum operation limit (amount of operations that can be executed) */
    val defaultOpLimit: Long = 100000
) : KsplangRunner {
    override fun run(program: String, input: List<Long>): List<Long> {
        var programFile: Path? = null
        try {
            // Store the program as a temporary file
            programFile = createTempFile(
                prefix = "ksplang_",
            )
            programFile.writeText(program)

            // Run the program
            val process = ProcessBuilder(
                "ksplang",
                "--max-stack-size", maxStackSize.toString(),
                "--op-limit", defaultOpLimit.toString(),
                programFile.toString()
            ).start()

            // Write the input to the stdin
            process.outputStream.buffered().use { it.write(input.joinToString(" ").toByteArray()) }

            // Read the entire output from the stdout
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()

            if (process.waitFor() != 0) {
                throw RuntimeException("Program exited with non-zero exit code: $error")
            }

            // Parse the output as a list of longs
            return output.trim().split("\\s+".toRegex()).map { it.toLong() }
        } finally {
            programFile?.toFile()?.delete()
        }
    }
}