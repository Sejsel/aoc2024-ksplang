package cz.sejsel.ksplang.benchmarks

import arrow.core.Either
import cz.sejsel.ksplang.interpreter.RunError
import cz.sejsel.ksplang.interpreter.RunResult
import cz.sejsel.ksplang.interpreter.run
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
class KotlinInterpreterBenchmarks {
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    fun stacklen10000(): Either<RunError, RunResult> {
        return run(Programs.stacklen10000.ops, Programs.stacklen10000.vmOptions)
    }

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    fun sort100(): Either<RunError, RunResult> {
        return run(Programs.sort100.ops, Programs.sort100.vmOptions)
    }


    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    fun sumloop10000(): Either<RunError, RunResult> {
        return run(Programs.sumloop10000.ops, Programs.sumloop10000.vmOptions)
    }
}
