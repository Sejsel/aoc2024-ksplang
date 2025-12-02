# Advent of Code 2025 in ksplang

Solutions to [Advent of Code 2025](https://adventofcode.com/2025/) written in
the [ksplang](https://github.com/ksp/ksplang) programming language.

## Rules
The rules of the game:

- create a ksplang program without access to the input (no solving during ksplang gen)
    - the program should support any valid inputs of any size
- run a ksplang interpreter with this program, optionally in text mode
- output is submitted directly with no post-processing

Other than that, anything goes. Code can be generated, it can be translated from other languages, anything really.

## Progress
We have multiple interpreters:
- [reference interpreter](https://github.com/ksp/ksplang) - the original implementation (in Rust)
- [KsplangJIT](https://github.com/exyi/ksplang/) - an interpreter with an experimental tracing JIT (also in Rust, it's a fork)

### Progress table

| Benchmark                                                                   |                       Instructions |   ksplang | KsplangJIT | KsplangJIT old |
|-----------------------------------------------------------------------------|-----------------------------------:|----------:|-----------:|---------------:|
| [Day 1 - part 1](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day1.kt) | [4032](/aoc25/ksplang/1-1.ksplang) | 288.33 ms |   75.33 ms |       70.33 ms |

*Note that the build time measurement may be significantly off because of JVM warmup, it is not a good measurement, it's more*

## Journal

After not finishing all tasks last year, this year, the plan is to have all of them solved before Christmas.
There are only 12 days of tasks this year, so hopefully I will have enough time this tmie.

### [Day 1](https://adventofcode.com/2025/day/1) (2025-12-02)

While I have some new [tooling](/wasm2ksplang) since last year, this task is simple enough that I just want to do this
in the most simple way, just using my "stdlib" and maybe the automatic stack tracking.

