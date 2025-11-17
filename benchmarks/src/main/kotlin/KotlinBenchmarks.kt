package cz.sejsel.benchmarks

import arrow.core.Either
import cz.sejsel.ksplang.interpreter.RunError
import cz.sejsel.ksplang.interpreter.RunResult
import cz.sejsel.ksplang.interpreter.run
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
class Stacklen {
    @Benchmark
    fun kotlinInterpreter(): Either<RunError, RunResult> {
        return run(Programs.stacklen.ops, Programs.stacklen.vmOptions)
    }
}
