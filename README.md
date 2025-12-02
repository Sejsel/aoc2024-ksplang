# ksplang programs

This is a collection of various programs written in [ksplang](https://esolangs.org/wiki/ksplang).
Or rather, the programs used to generate ksplang programs. And programs used to generate programs used to generate ksplang programs.
It's written mainly in Kotlin, it has pretty nice DSLs for this.

> [!NOTE]
> Figuring out how to use ksplang is actually an exceptionally nice puzzle and I would recommend you read none
> of the code in this repository before you try to solve [first tasks](https://github.com/ksp/ksplang/blob/master/ksplang_en.md#tasks)
> such as adding a zero to the stack yourself. It's a fun challenge.
> 
> It can then be expanded into building better and better tooling for yourself
> to write more complex programs, like I have done here.

## WebAssembly (WASM) runtime

Yep, you read that right. There is a lib here that can take a wasm binary, translate its functions to a
ksplang program, and get output from it. [See more](wasm2ksplang/README.md).

Yes, it can be used to run the Rust ksplang interpreter in ksplang. 

## Generator

The core of everything here is the gen module. It contains DSLs to define high-level programs, a builder
which translates these programs into real ksplang, and a standard library of sorts.

The builder and standard library do try to optimize programs to be short when possible, generally fewer
instructions = better (although not necessarily faster in practice).

## Advent of Code 2024
In 2024, I tried to solve Advent of Code tasks using ksplang. Rules are simple: the input is passed to a ksplang
program (possibly translated from text; `--text-input` in the interpreter), other than that anything goes.

Got to day 9, making improvements to the tooling along the way, then kinda ran out of time,
it would have been possible to continue, I might get back to it at some point. [See more](AOC24.md).


| Day | Instructions                                                                                            | Input mode | Runtime    | Executed instructions |
|-----|---------------------------------------------------------------------------------------------------------|------------|------------|-----------------------|
| 1-1 | [6554](/aoc24/ksplang/1-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day1.kt))  | numeric    | 21.388s    | 1533825452            |
| 1-2 | [4490](/aoc24/ksplang/1-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day1.kt))  | numeric    | 15.973s    | 1176145375            |
| 2-1 | [14909](/aoc24/ksplang/2-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day2.kt)) | text       | 1.702s     | 144668637             |
| 2-2 | [20918](/aoc24/ksplang/2-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day2.kt)) | text       | 521.645s   | 47360521764           | 
| 3-1 | [64550](/aoc24/ksplang/3-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt)) | text       | 0.773s     | 71764648              | 
| 3-2 | [82426](/aoc24/ksplang/3-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day3.kt)) | text       | 1.239s     | 119309701             | 
| 4-1 | [46162](/aoc24/ksplang/4-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day4.kt)) | text       | 3.345s     | 290696045             | 
| 4-2 | [28003](/aoc24/ksplang/4-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day4.kt)) | text       | 2.24s      | 176488689             | 
| 5-1 | [20021](/aoc24/ksplang/5-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day5.kt)) | text       | 1.856s     | 132478368             | 
| 5-2 | [25165](/aoc24/ksplang/5-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day5.kt)) | text       | 2.393s     | 172774543             | 
| 6-1 | [35216](/aoc24/ksplang/6-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day6.kt)) | text       | 2.145s     | 192576888             | 
| 6-2 | [98210](/aoc24/ksplang/6-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day6.kt)) | text       | 4206.66s   | 307526200616          | 
| 7-1 | [22598](/aoc24/ksplang/7-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day7.kt)) | text       | 88.277s    | 6921562949            | 
| 7-2 | [35233](/aoc24/ksplang/7-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day7.kt)) | text       | 16500.261s | 1453249659806         | 
| 8-1 | [19867](/aoc24/ksplang/8-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day8.kt)) | text       | 31.51s     | 2236694272            | 
| 8-2 | [19837](/aoc24/ksplang/8-2.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day8.kt)) | text       | 31.75s     | 2246499101            | 
| 9-1 | [14585](/aoc24/ksplang/9-1.ksplang) ([generator](/aoc24/src/main/kotlin/cz/sejsel/ksplang/aoc/Day9.kt)) | text       | 8.526s     | 613203435             | 

## Table of contents
... or rather a bullet list of contents, really.

- [Advent of Code 2024](aoc24/README.md) - solutions to Advent of Code 2024
- [WASM](wasm2ksplang/README.md) - read about the ksplang WASM runtime
- [gen](/gen) - the ksplang preprocessor (Kotlin DSL to generate ksplang code)
- [wasm2ksplang](/wasm2ksplang) - a full-blown WASM -> ksplang translation; can run the Rust ksplang interpreter in ksplang
- [bruteforce](/bruteforce) - tools for finding shorter/optimal ksplang sequences (pushes of constants...)
- [verification](/verification) - a Rust tool for verifying results of bruteforcing
- [interpreter](/interpreter) - a Kotlin interpreter of ksplang (for testing, mainly); use the [reference interpreter](https://github.com/ksp/ksplang) instead
- [annotools](/annotools) - CLI for various debugging/browsing of annotated ksplang programs
- [debugger](/debugger) - Backend (http server) for a ksplang debugger
- [debugger-ui](/debugger-ui) - Frontend (react) for a ksplang debugger
- [README.md](README.md) - readme for... wait, you are reading it right now!
