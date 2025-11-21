package cz.sejsel.ksplang.benchmarks

import cz.sejsel.ksplang.benchmarks.rust.RustKsplangRunner
import java.time.Duration

data class RustBenchmark(
    val program: BenchmarkProgram,
    val runs: Int
) {
    val name: String = program.name
}

class RustBenchmarks(val runner: RustKsplangRunner) {
    val allBenchmarks = listOf(
        RustBenchmark(Programs.stacklen10000, runs = 20),
        RustBenchmark(Programs.sort100, runs = 20),
        RustBenchmark(Programs.sumloop10000, runs = 20),
        RustBenchmark(Programs.wasmaoc24day2, runs = 2),
        RustBenchmark(Programs.ksplangpush1, runs = 2),
        RustBenchmark(Programs.i64factorial200, runs = 2),
    )

    fun runBenchmark(benchmark: RustBenchmark): List<Duration> {
        return List(benchmark.runs) {
            val result = runner.run(benchmark.program.program, benchmark.program.inputStack)
            result.executionTime
        }
    }
}
