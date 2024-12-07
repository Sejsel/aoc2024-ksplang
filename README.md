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

| Day | Instructions                  | Input mode | Runtime | Executed instructions |
|-----|-------------------------------|------------|---------|-----------------------|
| 1-1 | [6554](/ksplang/1-1.ksplang)  | numeric    | 21.388s | 1533825452            |
| 1-2 | [4490](/ksplang/1-2.ksplang)  | numeric    | 15.973s | 1176145375            |
| 2-1 | [14909](/ksplang/2-1.ksplang) | text       | 1.702s  | 144668637             |
## Journal

Might as well keep some notes here, maybe some archaeologists will find them in the future.
And despair over the time ~~wasted~~ spent on this. Entries are numbered by the days of each task, not real days.

### Day 1 (2024-12-03)
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

### Day 2 (2024-12-06)

Checked the example input in the description, looked easy enough with numeric mode. Checked the actual input,
and it turns out that we need to count how many nubmers are on each line, so text mode it is. Yay.

The text mode of the ksplang interpreter takes text input and converts each unicode codepoint to a number.
This is equivalent to the ASCII encoding for characters that are in the ASCII range.

Part 1 wasn't too bad except for the need to parse numbers. It's somehow already December 6th, though.