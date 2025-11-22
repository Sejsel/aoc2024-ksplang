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
        RustBenchmark(Programs.wasmksplangpush1, runs = 2),
        RustBenchmark(Programs.wasmi32factorial10000, runs = 5),
        RustBenchmark(Programs.wasmi64factorial10000, runs = 5),
    )

    fun runBenchmark(benchmark: RustBenchmark): List<Duration> {
        return List(benchmark.runs) {
            val result = runner.run(benchmark.program.program, benchmark.program.inputStack)
            if (result.outputStack != benchmark.program.expectedResult) {
                error("Unexpected result for benchmark ${benchmark.name}: expected ${benchmark.program.expectedResult}, got ${result.outputStack}")
            }
            result.executionTime
        }
    }
}
