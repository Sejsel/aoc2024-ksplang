# Benchmarks

This is a meta-benchmark runner measuring how long it takes to run generated programs.

This benchmarks both the efficiency of the [KsplangBuilder](/gen/, but also 

It compares multiple implementations:

- Kotlin interpreter (see [interpreter](/interpreter/))
- Any amount of Rust interpreters
  - original v1.0 interpreter
  - interpreter with optimized funkcia
  - JIT optimizing interpreter ([github](https://github.com/exyi/ksplang/tree/optimizer))

## Running

Run the benchmarks:
```
./gradlew :benchmarks:run --args "benchmark"
```

Also benchmark the kotlin interpreter:
```
./gradlew :benchmarks:run --args "benchmark --enable-kotlin"
```

## Dumping benchmark programs
You can dump all the benchmark programs to a file. For inputs, see [Programs.kt](/benchmarks/src/main/kotlin/cz/sejsel/ksplang/benchmarks/Programs.kt).

```
./gradlew :benchmarks:run --args "dump-programs"
```
