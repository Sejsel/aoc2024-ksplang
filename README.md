# Advent of Code 2024 in ksplang

Solutions to [Advent of Code 2024](https://adventofcode.com/2024/) written in
the [ksplang](https://github.com/ksp/ksplang) programming language. Or rather, the programs used to generate
ksplang programs. And programs used to generate programs used to generate ksplang programs.


> [!NOTE]
> Figuring out how to use ksplang is actually an exceptionally nice puzzle and I would recommend you read none
> of the code in this repository before you try to solve [first tasks](https://github.com/ksp/ksplang/blob/master/ksplang_en.md#tasks)
> such as adding a zero to the stack yourself. It's a fun challenge.
> 
> It can then be expanded into building better and better tooling for yourself
> to write more complex programs, like I have done here.


## Progress

| Day | Instructions                                                                                     | Input mode | Runtime  | Executed instructions |
|-----|--------------------------------------------------------------------------------------------------|------------|----------|-----------------------|
| 1-1 | [6554](/ksplang/1-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day1.kt))   | numeric    | 21.388s  | 1533825452            |
| 1-2 | [4490](/ksplang/1-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day1.kt))   | numeric    | 15.973s  | 1176145375            |
| 2-1 | [14909](/ksplang/2-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day2.kt))  | text       | 1.702s   | 144668637             |
| 2-2 | [20918](/ksplang/2-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day2.kt))  | text       | 521.645s | 47360521764           |
| 3-1 | [64550](/ksplang/3-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt))  | text       | 0.773s   | 71764648              |
| 3-2 | [82426](/ksplang/3-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt))  | text       | 1.239s   | 119309701             |
| 4-1 | [46162](/ksplang/4-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day4.kt))  | text       | 3.345s   | 290696045             |
| 4-2 | [28003](/ksplang/4-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day4.kt))  | text       | 2.24s    | 176488689             |
| 5-1 | [20021](/ksplang/5-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day5.kt))  | text       | 1.856s   | 132478368             |
| 5-2 | [25165](/ksplang/5-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day5.kt))  | text       | 2.393s   | 172774543             |

*Note: Some instruction count optimizations are not enabled. You can place some functions at the start of the file and use
the `call` instruction to use them; for example each `dup` goes down from 38 to 11 instructions needed to trigger the `call`.
Keeping the functions inlined is faster, at least for the standard interpreter. Optimizing for performance here is more
interesting than optimizing for the shortest program.*

## Journal

Might as well keep some notes here, maybe some archaeologists will find them in the future.
And despair over the time ~~wasted~~ spent on this. Entries are numbered by the days of each task, not real days.

### [Day 1](https://adventofcode.com/2024/day/1) (2024-12-03)
Took a bit of time to port my ksplang tooling from Python to Kotlin, mainly for better organization.
Python is unsuitable because of its inability to do circular imports, and its performance is
also painful at times. Turns out it's actually day 3 by now. Surely I can catch up.

The first task is quite nice, and it can be done without text mode as the input is just numbers.
Writing the solution to the first part didn't take too long.

#### Day 1 Part 2 (2024-12-04)
Made a little summary generator to generate that table above. Somehow it's too enjoyable
to try to get the instruction count down.

I also reintroduced my catalogue of [short ways to push a constant](gen/src/main/resources/short_pushes.txt).
There are still a few more optimizations that I need to reintroduce from my Python tooling, but this one
was somehow the most fun back then and it makes a decent difference (~16% speedup in day 1-1).

The second part of the task was fine, my lovely quadratic algorithm ended up faster than
the (quadratic) [sort](https://arxiv.org/abs/2110.01111) I used in part 1. Perhaps I will replace the sort
with something faster later on.

### [Day 2](https://adventofcode.com/2024/day/2) (2024-12-06)

I checked the example input in the description, looked easy enough with numeric mode. Checked the actual input,
and it turns out that we need to count how many numbers are on each line, so text mode it is. Yay.

The text mode of the ksplang interpreter takes text input and converts each unicode codepoint to a number.
This is equivalent to the ASCII encoding for characters that are in the ASCII range. We can also use the same
conversion to produce text, but we don't need that for now, text input and numeric (raw stack value) output is fine.

Part 1 wasn't too bad except for the need to parse numbers. It all ended up as one big program without
any functions, so surely it won't be hard to adapt for part 2.

#### Day 2 Part 2 (2024-12-08)

This was surprisingly annoying, thankfully was able to reuse the part 1 validity check quite nicely.

First correct version took 517 seconds to finish. There is an "easy" opportunity to speed it up a lot,
down to ~70 seconds or so, there is a stack length calculation in the hot section and it could be moved outside of the
loop.
I tried it multiple times and yet, every time, there was some kind of off-by-one error I just could not fix.
At this point I would rather play with other tasks, so this is staying horribly slow, at least for now.

### [Day 3](https://adventofcode.com/2024/day/3) (2024-12-08)

Finally a nice task. This one was actually quite easy, even though it's text mode. I may have created one of the
[ugliest if chains](https://github.com/Sejsel/aoc2024-ksplang/blob/master/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt#L272-L395) I have ever seen, but it was pretty easy to write.
Being able to separate the part before and after the comma helped a lot to reduce the complexity a lot.

My favorite so far. Part 2 was also easy to adapt to.

### [Day 4](https://adventofcode.com/2024/day/4) (2024-12-09)

Straightforward. At least as long as you don't mind a few nested if statements. And some mild copy-pasting for part 2,
the overall approach I chose made the second part very easy to adapt to.

### [Day 5](https://adventofcode.com/2024/day/5) (2024-12-12)

I have an idea for how to revolutionize the industry! Or rather, revolutionize the way we can write ksplang programs.
Stay tuned. I will definitely find time to implement it soon.

Also, this task was quite a pain due to an ugly off-by-one error in loop bounds. Maybe it is time to build some other
loop than a "do while".
