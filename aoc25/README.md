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

#### ksplang
| Program        |                                                                                                Instructions |      ksplang |  KsplangJIT | KsplangJIT old |
|----------------|------------------------------------------------------------------------------------------------------------:|-------------:|------------:|---------------:|
| Day 1 - part 1 |  [4032](/aoc25/ksplang/1-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/pure/Day1.kt)) |    281.67 ms |    70.67 ms |       72.67 ms |
| Day 1 - part 2 |  [4894](/aoc25/ksplang/1-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/pure/Day1.kt)) |    285.67 ms |    95.00 ms |       92.00 ms |
| Day 2 - part 1 |  [7759](/aoc25/ksplang/2-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/pure/Day2.kt)) |  13395.00 ms |  1905.00 ms |      630.00 ms |
| Day 2 - part 2 | [11823](/aoc25/ksplang/2-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/pure/Day2.kt)) | 190209.00 ms | 10065.00 ms |    25360.00 ms |
| Day 3 - part 1 |  [4538](/aoc25/ksplang/3-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/pure/Day3.kt)) |    403.00 ms |    70.50 ms |       65.25 ms |
#### Translated from WASM

These are programs made by writing a Rust program, compiling it to WASM, then translating it to ksplang with [wasm2ksplang](/wasm2ksplang).

| Program             |                                                                                                                                               Instructions |   ksplang | KsplangJIT | KsplangJIT old |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------:|----------:|-----------:|---------------:|
| WASM Day 1 - part 1 | [10965](/aoc25/ksplang/wasm/1-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day1.kt), [rust](/aoc25/rust/aoc25-1-1/src/lib.rs)) | 240.33 ms |   96.67 ms |       85.00 ms |
| WASM Day 1 - part 2 | [14098](/aoc25/ksplang/wasm/1-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day1.kt), [rust](/aoc25/rust/aoc25-1-2/src/lib.rs)) | 279.00 ms |  124.33 ms |      125.33 ms |

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

### [Day 3](https://adventofcode.com/2025/day/3) (2025-12-03)

This one was not tooo bad, ksplang has a perfect instruction for it - `lensum`. However, it's getting quite complex.

Seeing part 2, which is not *that* horrible all things considered, with the algorithm being still quite fast,
but just a lot of *effort*. I think it might be time to switch approaches.

### [Day 1](https://adventofcode.com/2025/day/1) - again (2025-12-10)

What, you may ask. Why would you go back to day 1. Well, somehow a week went past without anything getting solved.
I still want to get it all done before Christmas for once (and it doesn't matter that there are only 12 puzzles).
So, yeah, time to do it again, and more efficiently.

Time to use [wasm2ksplang](/wasm2ksplang), the tool I made which can take a WASM program, translate it to ksplang,
creates a runtime layout on the ksplang stack, and allows you to call WASM functions from a ksplang program.
It allows WASM programs to import functions like `read_input(index)` and `input_size`. It doesn't support floats *yet*,
but *surely* that's fine for now.

So, yeah. Time to write some programs in Rust and compile them into WASM. No wasm-pack, mind you, that is very much
aimed at JavaScript interop. Just this magical incantation:

```sh
RUSTFLAGS=-Ctarget-cpu=mvp cargo +nightly build --release -Zbuild-std=panic_abort,std --target wasm32-unknown-unknown
```

Anyway, there are still some optimizations to do with that. One of the slowest
parts of wasm2ksplang programs is the memory - we need to initialize 16 pages (1 million zeroes) in every Rust WASM
program, for example, even if it's not used at all! Well, if we don't use a memory, we can patch the WASM to not
define a memory. Or, eventually, detect that case in wasm2ksplang and not emit a memory if it won't be used,
with no need to modify WASM files.

On that note, modifying WASM files is actually quite easy. Use wasm2wat to translate it to a text representation,
make your changes, and use wat2wasm. Super nice thing to have for a "language" like that.

Anyway, with a simple dirty script which does that, we now have a ksplang program generated from WASM generated from
Rust which is comparable to the "pure" ksplang solution I had.
It has double the instructions or so, but somehow actually runs the same speed or even faster as the
*organically sourced free-range* ksplang.