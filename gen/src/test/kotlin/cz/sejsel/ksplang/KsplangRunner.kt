package cz.sejsel.ksplang

import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class KsplangRunner(
    /** Maximum stack size (amount of int64 elements on the program stack) */
    val maxStackSize: Long = 2097152,
    /** Maximum operation limit (amount of operations that can be executed) */
    val defaultOpLimit: Long = 100000
) {
    fun run(program: String, input: List<Long>, opLimit: Long = defaultOpLimit): List<Long> {
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
                "--op-limit", opLimit.toString(),
                programFile.toString()
            ).start()

            // Write the input to the stdin
            process.outputStream.buffered().use { it.write(input.joinToString(" ").toByteArray()) }

            // Read the entire output from the stdout
            val output = process.inputStream.bufferedReader().readText()

            // Parse the output as a list of longs
            return output.trim().split("\\s+".toRegex()).map { it.toLong() }
        } finally {
            programFile?.toFile()?.delete()
        }
    }
}