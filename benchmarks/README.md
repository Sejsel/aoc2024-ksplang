# Benchmarks

This is a meta-benchmark runner measuring how long it takes to run generated programs.

This benchmarks both the efficiency of the [KsplangBuilder](/gen/, but also 

It compares multiple implementations:

- Kotlin interpreter (see [interpreter](/interpreter/))
- Any amount of Rust interpreters
  - original v1.0 interpreter
  - interpreter with optimized funkcia
  - JIT optimizing interpreter ([github](https://github.com/exyi/ksplang/tree/optimizer))