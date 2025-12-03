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

| Program        |                                                                                           Instructions |      ksplang | KsplangJIT | KsplangJIT old |
|----------------|-------------------------------------------------------------------------------------------------------:|-------------:|-----------:|---------------:|
| Day 1 - part 1 |  [4032](/aoc25/ksplang/1-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day1.kt)) |    276.00 ms |   73.67 ms |       68.67 ms |
| Day 1 - part 2 |  [4894](/aoc25/ksplang/1-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day1.kt)) |    297.00 ms |   90.33 ms |       89.00 ms |
| Day 2 - part 1 |  [9940](/aoc25/ksplang/2-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day2.kt)) |  13425.00 ms | 1862.00 ms |      609.00 ms |
| Day 2 - part 2 | [14036](/aoc25/ksplang/2-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day2.kt)) | 201020.00 ms | 9549.00 ms |    27257.00 ms |
| Day 3 - part 1 |  [4538](/aoc25/ksplang/3-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/Day3.kt)) |    397.50 ms |   70.50 ms |       65.75 ms |

## Journal

After not finishing all tasks last year, this year, the plan is to have all of them solved before Christmas.
There are only 12 days of tasks this year, so hopefully I will have enough time this time.

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

Future note: yep, I ended up making it faster, by preparing a lookup table for powers of 10 at the start of the stack,
it went from 52 seconds down to 13 seconds. Neat!

#### Part 2
Well, at least I guessed what the second part is going to be exactly before even seeing it.
Unfortunately, it's going to be quite painful.

In the end, I went for `auto` - my DSL which tracks the stack for me on its own and allows
some more comfortable variable reuse. It's not quite as effective because it eagerly generates code and cannot
tell when an intermediate variable is used for the latest time, so it stays there on the stack until the end of the scope, slowing everything down a bit.
Not that *that* really matters, because I am also missing a support for breaks or continues in loops and it really, really hurts in this task.

So, super slow solution for now, might come back later if I improve tooling.