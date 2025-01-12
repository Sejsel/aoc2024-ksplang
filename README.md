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

| Day | Instructions                                                                                    | Input mode | Runtime    | Executed instructions |
|-----|-------------------------------------------------------------------------------------------------|------------|------------|-----------------------|
| 1-1 | [6554](/ksplang/1-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day1.kt))  | numeric    | 21.388s    | 1533825452            |
| 1-2 | [4490](/ksplang/1-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day1.kt))  | numeric    | 15.973s    | 1176145375            |
| 2-1 | [14909](/ksplang/2-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day2.kt)) | text       | 1.702s     | 144668637             |
| 2-2 | [20918](/ksplang/2-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day2.kt)) | text       | 521.645s   | 47360521764           |
| 3-1 | [64550](/ksplang/3-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt)) | text       | 0.773s     | 71764648              |
| 3-2 | [82426](/ksplang/3-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt)) | text       | 1.239s     | 119309701             |
| 4-1 | [46162](/ksplang/4-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day4.kt)) | text       | 3.345s     | 290696045             |
| 4-2 | [28003](/ksplang/4-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day4.kt)) | text       | 2.24s      | 176488689             |
| 5-1 | [20021](/ksplang/5-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day5.kt)) | text       | 1.856s     | 132478368             |
| 5-2 | [25165](/ksplang/5-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day5.kt)) | text       | 2.393s     | 172774543             |
| 6-1 | [35216](/ksplang/6-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day6.kt)) | text       | 2.145s     | 192576888             |
| 6-2 | [98210](/ksplang/6-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day6.kt)) | text       | 6184.82s   | 552616919215          |
| 7-1 | [22598](/ksplang/7-1.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day7.kt)) | text       | 88.277s    | 6921562949            |
| 7-1 | [35233](/ksplang/7-2.ksplang) ([generator](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day7.kt)) | text       | 16500.261s | 1453249659806         |

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

### [Day 6](https://adventofcode.com/2024/day/6) (2025-01-07)

So, after seeing that AoC tasks are indeed solvable in ksplang, I had an idea on how to make it easier to solve
the remaining tasks. It took me a while to find the energy to implement it (hello, 2025!), but it has made it
significantly easier to write more complex programs.

The idea was simple: it would be fairly easy to keep track of the top of the stack automatically. I could
call values on top of the stack *variables* and call functions with these variables, which could create new variables
(what a wild concept!). This *auto* stack tracking is the basis of the *auto* ksplang DSL.

Using the lovely Kotlin DSLs, we even get scopes almost for free. We just need to search in parent scopes to find
variables in there and calculate the correct distance of the variable from the stack top.

For now, I ended up implementing the easiest approach, which is to instantly generate all code. The upside is that it is
easy to reason about and implement. The downside is that all intermediate variables live until the end of the scope,
as there is no way to know when is the last time a variable is used. In the future, I could save all of the function calls,
automatically track the lifetime of a variable and when it is used for the last time, move it to the top to be consumed
instead of duplicating it. That would make all generated programs quite a bit faster.

This *auto* approach is not a full reimplementation, it is just a layer on top of the current DSL.
This means that we can both temporarily opt-in to this auto stack tracking and temporarily opt-out
(as long as we don't break the stack layout, for example by forgetting an extra variable on top of the stack).
It's just an extra tool in the arsenal, albeit a powerful one.

You can see what this looks like on the [Day 6](/aoc/src/main/kotlin/cz/sejsel/ksplang/aoc/Day6.kt) task solution.
There are a few lambdas in places you might not expect (conditions) and we need to avoid Kotlin keywords, but overall
it works quite well. It has definitely saved a lot of time on implementing the task. And since this is still all build
on top of a Kotlin DSL, there is no need to parse this, it's all just function calls.

There are a few footguns here that need to be kept in mind. We cannot override the assignment operator, so instead of
writing `val x = variable(5); x = 20`, we need to use something like `set(x) to 20`. Additionally, when we are calling
functions on Variables, we do not have a reference to the current scope which further reduces the API options. Unfortunately,
that means we cannot override operators or use custom infix operators. So we couldn't do `x assignTo 20` (not that I
would like that name). Now, tracking the current scope may be somehow possible, but I am pretty sure it would cause
some issues in the future, there is fairly heavy use of lambdas and potential laziness we may want to exploit in the future.
Anyway, as long as `val` is used with variables, all will be fine. Oh, and for the love of god, do not assign the result
of `set(x)` (a `VarSetter` that exposes the `to` infix function) to a variable and attempt to use it later, in a different scope.

Might be time to add some custom lints, using something like [detekt](https://detekt.dev).

#### Part 2

Took the most straightforward approach, which is very slow, especially considering how poorly optimized
my bitwise operations are, and I used them to mark visited places.

Writing more complex data structures would take way more time than just letting the program run in the background.

### [Day 7](https://adventofcode.com/2024/day/7) (2025-01-11)

Really easy to implement with the `auto` stack tracking. 

#### Part 2

I like it when the task can be adapted just by swapping out one function.

I thought this would run a bit slower than part 1, around an hour, did not implement the obvious optimization
(early stopping) and went to sleep. Woke up to a new runtime record of 4.5 hours. Neat!
Would be fairly easy to make significantly faster. Not having a simple `break` for loops makes it more likely
that something like this will happen again, though.
