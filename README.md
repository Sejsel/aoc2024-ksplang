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

| Day | Instructions                 | Input mode | Runtime | Executed instructions |
|-----|------------------------------|------------|---------|-----------------------|
| 1-1 | [8150](/ksplang/1-1.ksplang) | numeric    | 23.66s  | 1741886242            |
| 1-2 | [5254](/ksplang/1-2.ksplang) | numeric    | 16.983s | 1331944030            |


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
