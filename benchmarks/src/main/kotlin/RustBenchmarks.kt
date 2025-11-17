package cz.sejsel.benchmarks

import cz.sejsel.benchmarks.rust.RustKsplangRunner
import java.time.Duration

data class RustBenchmark(
    val program: BenchmarkProgram,
    val runs: Int
) {
    val name: String = program.name
}

class RustBenchmarks(val runner: RustKsplangRunner) {
    val ALL_BENCHMARKS = listOf(
        RustBenchmark(Programs.stacklen, runs = 20),
    )

    fun runBenchmark(benchmark: RustBenchmark): List<Duration> {
        val runs = 20
        return List(runs) {
            val result = runner.run(benchmark.program.program, benchmark.program.inputStack)
            result.executionTime
        }
    }
}
