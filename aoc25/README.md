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

| Program        |                                                                                          Instructions |     ksplang | KsplangJIT | KsplangJIT old |
|----------------|------------------------------------------------------------------------------------------------------:|------------:|-----------:|---------------:|
| Day 1 - part 1 | [4032](/aoc25/ksplang/1-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day1.kt)) |   279.67 ms |   78.00 ms |       72.67 ms |
| Day 1 - part 2 | [4894](/aoc25/ksplang/1-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day1.kt)) |   295.00 ms |   92.67 ms |       94.00 ms |
| Day 2 - part 1 | [9960](/aoc25/ksplang/2-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day2.kt)) | 52245.00 ms |      ERROR |     1087.00 ms |

## Journal

After not finishing all tasks last year, this year, the plan is to have all of them solved before Christmas.
There are only 12 days of tasks this year, so hopefully I will have enough time this tmie.

### [Day 1](https://adventofcode.com/2025/day/1) (2025-12-02)

While I have some new [tooling](/wasm2ksplang) since last year, this task is simple enough that I just want to do this
in the most simple way, just using my "stdlib" and maybe the automatic stack tracking.

In the end, it was pretty straightforward, it runs fast, no need for anything fancy. A nice first task.
And the second part did not require massive rewrites either, so that's neat.

### [Day 2](https://adventofcode.com/2025/day/2) (2025-12-02)

#### Part 1
This one looked super scary at first, but ksplang is actually equipped with the most important instruction for this.
Neat. I can see quite a few optimizations I could make, a 52 second runtime on the reference implementation is not great.
But 1 second runtime with KsplangJIT is pretty great, considering. Might come back to this to make it faster.
Might need to, because of part 2, anyway.

#### Part 2
Well, at least I guessed what the second part is going to be exactly before even seeing it.
Unfortunately, it's going to be quite painful.