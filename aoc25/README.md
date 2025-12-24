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

| Program             |                                                                                                                                                 Instructions |      ksplang | KsplangJIT | KsplangJIT no tracing |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------:|-------------:|-----------:|----------------------:|
| WASM Day 1 - part 1 | [10655](/aoc25/ksplang/wasm/1-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day1.kt), [rust](/aoc25/rust/aoc25-1-1/src/lib.rs)) | 232.67 ms | 79.67 ms | 76.67 ms |
| WASM Day 1 - part 2 | [13721](/aoc25/ksplang/wasm/1-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day1.kt), [rust](/aoc25/rust/aoc25-1-2/src/lib.rs)) | 268.67 ms | 113.00 ms | 106.33 ms |
| WASM Day 2 - part 1 | [499549](/aoc25/ksplang/wasm/2-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day2.kt), [rust](/aoc25/rust/aoc25-2-1/src/lib.rs)) | 27123.00 ms | 859.00 ms | 1793.00 ms |
| WASM Day 2 - part 2 | [508533](/aoc25/ksplang/wasm/2-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day2.kt), [rust](/aoc25/rust/aoc25-2-2/src/lib.rs)) | 218731.00 ms | 5459.00 ms | 14314.00 ms |
| WASM Day 3 - part 1 | [15137](/aoc25/ksplang/wasm/3-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day3.kt), [rust](/aoc25/rust/aoc25-3-1/src/lib.rs)) | 534.33 ms | 80.33 ms | 77.67 ms |
| WASM Day 3 - part 2 | [7601](/aoc25/ksplang/wasm/3-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day3.kt), [rust](/aoc25/rust/aoc25-3-2/src/lib.rs)) | 1361.00 ms | 127.50 ms | 99.00 ms |
| WASM Day 4 - part 1 | [34863](/aoc25/ksplang/wasm/4-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day4.kt), [rust](/aoc25/rust/aoc25-4-1/src/lib.rs)) | 907.33 ms | 272.67 ms | 254.67 ms |
| WASM Day 4 - part 2 | [41621](/aoc25/ksplang/wasm/4-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day4.kt), [rust](/aoc25/rust/aoc25-4-2/src/lib.rs)) | 16726.00 ms | 1010.00 ms | 1028.00 ms |
| WASM Day 5 - part 1 | [919133](/aoc25/ksplang/wasm/5-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day5.kt), [rust](/aoc25/rust/aoc25-5-1/src/lib.rs)) | 6840.00 ms | 605.00 ms | 459.00 ms |
| WASM Day 5 - part 2 | [2541218](/aoc25/ksplang/wasm/5-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day5.kt), [rust](/aoc25/rust/aoc25-5-2/src/lib.rs)) | 4215.00 ms | 655.00 ms | 641.00 ms |
| WASM Day 6 - part 1 | [55476](/aoc25/ksplang/wasm/6-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day6.kt), [rust](/aoc25/rust/aoc25-6-1/src/lib.rs)) | 428.67 ms | 211.67 ms | 205.00 ms |
| WASM Day 6 - part 2 | [57975](/aoc25/ksplang/wasm/6-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day6.kt), [rust](/aoc25/rust/aoc25-6-2/src/lib.rs)) | 460.33 ms | 210.67 ms | 201.33 ms |
| WASM Day 7 - part 1 | [20701](/aoc25/ksplang/wasm/7-1.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day7.kt), [rust](/aoc25/rust/aoc25-7-1/src/lib.rs)) | 456.67 ms | 102.33 ms | 92.67 ms |
| WASM Day 7 - part 2 | [29495](/aoc25/ksplang/wasm/7-2.ksplang) ([gen](/aoc25/src/main/kotlin/cz/sejsel/ksplang/aoc/days/wasm/Day7.kt), [rust](/aoc25/rust/aoc25-7-2/src/lib.rs)) | 653.33 ms | 121.33 ms | 117.67 ms |



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
program, even if it's not used at all! Well, if we don't use a memory, we can patch the WASM to not
define a memory. Or, eventually, detect that case in wasm2ksplang and not emit a memory if it won't be used,
with no need to modify WASM files.

On that note, modifying WASM files is actually quite easy. Use wasm2wat to translate it to a text representation,
make your changes, and use wat2wasm. Super nice thing to have for a "language" like that.

Anyway, with a simple dirty script which does that, we now have a ksplang program generated from WASM generated from
Rust which is comparable to the "pure" ksplang solution I had.
It has double the instructions or so, but somehow actually runs the same speed or even faster as the
*organically sourced free-range* ksplang.

### [Day 2](https://adventofcode.com/2025/day/2) - again (2026-12-13)

Trying to get the Rust program as close to "pure" ksplang performance as possible. The main thing I did was
expose ksplang instructions as extern functions, so if you want to use ksplang instructions directly from Rust, you can.

In the end, I ended up with a program which is about 3x as slow as my non-WASM solution. Most of the slowness is in
the pow(10, e) function. In the pure ksplang solution, I have a lookup table on the stack, but here it has to be in WASM
memory and it is significantly more costly that way (WASM memory is byte-based, so we need to get 8 stack values instead of 1).

It would be possible to somehow extend the generated WASM with lookup table support, there are custom sections in WASM
and we could then invoke an imported function which would use a lookup table which we defined in the custom section.
However, that is too much work for now, so I won't bother just yet.

Imported ksplang instructions make it possible to work with 64-bit numbers in Rust without incurring massive performance cost of wrapping
arithmetic. If you are fine with overflows crashing the program, you can now just use raw ksplang instructions.
I have a pretty `RawI64` type, and operators on it use the cheaper ksplang instructions.

It is tempting to just have a special mode for wasm2ksplang where i64 instructions would just use
fast variants and crash upon overflows. Unfortunately, you would not have full control over it - it's LLVM who
decides which WASM ops are used, and any optimization might cause potential overflows even when you would not expect it from the Rust code.
And it's signed i64 operation overflows which cause issues, so if you are working with u64, it would also behave
a bit unexpectedly.

### [Day 3](https://adventofcode.com/2025/day/3) - again (2026-12-13)

Now, solving part 1 again in Rust and adding part 2 was so much faster than it ever could have been without wasm2ksplang.
The performance is quite good considering, only 30% slowdown in part 1.

I wonder how the performance is going to be going forward, there are no more "pure" ksplang solutions for comparisons.
But it was good to optimize the current solutions, I now know what to expect - what is expensive (i64) and what is not
worth spending time optimizing (i32).

### [Day 4](https://adventofcode.com/2025/day/4) - 2026-12-14

Part 1 is sweet and simple. Part 2 actually a nice task, will try the most trivial approach first
and then might implement a better way.

Part 2 was actually way faster than I expected even without any smart approach. Might come back to it later,
but this is okay by me for now.

Now I am running into a performance issue where the WASM compiler generates one of two things when comparing an i64
with zero depending on what it likes for control flow. Either it generates the "equals to zero" instruction or it does
a generic "not equals" comparison with a zero constant because WASM does not have an "neqz" isntruction.

First option:
```wat
i64.eqz
```

This is something that has a very efficient translation to ksplang, we can use what I call `zeroNot`:
```kotlin
/*
 * Returns 1 if the top value on the stack is zero, 0 otherwise. A negation of "zeroity".
 *
 * Signature: `a -> a == 0 ? 1 : 0`
 */
fun Block.zeroNot() = function("zeroNot") {
    // x
    sgn()
    // sgn(x)
    abs()
    // |sgn(x)|, i.e. 0 or 1
    CS(); j(); inc()
    // |sgn(x)| 1
    CS()
    // |sgn(x)| 1 1
    bulkxor()
}
```

The alternative is that it generates this sequence:

```wat
i64.const 0
i64.ne
```

This in theory also has an efficient translation (like `zeroNot` twice), but right now we go instruction by instruction,
so we don't know it's comparison with a constant. I might end up adding an intermediate representation when translating
WASM which would allow for optimizations like this. But not right now.

### [Day 5](https://adventofcode.com/2025/day/5) - 2026-12-14

I was afraid part 1 would be super slow because it's the first one in which I am using memory, but it was
actually more than fine.

Now part 2, this is finally a place for WASM translation to shine because I don't have any good sorting
implemented in my ksplang "stdlib".

### [Day 6](https://adventofcode.com/2025/day/6) - 2026-12-19

This one was not too bad at all. Part 2 was super fast to implement, faster than it would be in other languages,
just because I needed to implement a parse function before already, and could modify it super quickly. Neat.

### [Day 7](https://adventofcode.com/2025/day/6) - 2026-12-20

Another one that was kind of straightforward. There is even some potential for optimization which I
haven't even done because it was more than fast enough without it already.

Thankfully, part 2 was very easy, but I can imagine it being super annoying if you don't figure out the simple approach.

I did have to debug on mistake a bit, I might extend `annotools` to not only list function calls, but to also list
what parameters the functions were called with. That would have saved me quite a bit of time.