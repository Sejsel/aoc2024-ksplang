package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 1
// https://adventofcode.com/2024/day/1
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day1())
    println(program)
    File("ksplang/day1/1.ksplang").writeText(program)
}

fun ComplexBlock.extractColumn(columnIndex: Long) = complexFunction {
    // [stack] inputlen
    dup()
    // [stack] inputlen inputlen
    div(2); dec()
    // [stack] inputlen inputlen/2-1
    doWhileNonZero {
        // [stack] inputlen i
        dup()
        // [stack] inputlen i i
        mul(2); add(columnIndex)
        // [stack] inputlen i i*2+$c
        yoink()
        // [stack] inputlen i s[i*2+$c]
        roll(3, 1)
        // [stack] s[i*2+$c] inputlen i
        // We need to also do i=0, so we CS before decrementing, which will only stop if new i would be -1
        CS()
        // [stack] s[i*2+$c] inputlen i CS(i)
        swap2(); dec(); swap2()
        // [stack] s[i*2+$c] inputlen i-1 CS(i)
    }
    pop()
    // [stack] [column] inputlen
}


// We are using numeric input for this
// Unfortunately the two columns are interleaved
fun day1() = buildComplexFunction("day1") {
    // [stack]
    stacklen()
    // [stack] inputlen
    extractColumn(0)
    // [stack] [column1] inputlen
    extractColumn(1)
    // [stack] [column1] [column2] inputlen
    dup(); dup(); dec()
    // [stack] [column1] [column2] inputlen inputlen inputlen-1
    yeet()
    // [stack|inputlen] [column1] [column2] inputlen
    dup(); mul(2)
    // [stack|inputlen] [column1] [column2] inputlen inputlen*2
    lroll()
    // [column1] [column2] [stack|inputlen]
    dup(); div(2)
    // [column1] [column2] [stack|inputlen] inputlen/2
    sort()
    // [sorted(column1)] [column2] [stack|inputlen]
    dec()
    doWhileNonZero {
        // [pre] [stack] i
        pop2()
        dec()
        CS()
    }
    pop()
    // [sorted(column1)] [column2]
    stacklen()
    // [sorted(column1)] [column2] stacklen
    dup(); div(2); swap2()
    // [sorted(column1)] [column2] stacklen/2 stacklen
    lroll()
    // [column2] [sorted(column1)]
    stacklen()
    // [column2] [sorted(column1)] stacklen
    div(2)
    sort()
    // [sorted(column2)] [sorted(column1)]
    stacklen()
    div(2)
    // [sorted(column2)] [sorted(column1)] columnlen
    dup()
    // [sorted(column2)] [sorted(column1)] columnlen columnlen
    push(0)
    // [sorted(column2)] [sorted(column1)] columnlen columnlen 0
    swap2()
    // [sorted(column2)] [sorted(column1)] columnlen 0   columnlen
    // [sorted(column2)] [sorted(column1)] columnlen sum i
    doWhileNonZero {
        // columnlen sum i
        dec()
        // columnlen sum i-1
        dup()
        // columnlen sum i-1 i-1
        yoinkDestructive()
        // columnlen sum i-1 s[i-1]
        dupFourth()
        // columnlen sum i-1 s[i-1] columnlen
        dupThird()
        // columnlen sum i-1 s[i-1] columnlen i-1
        add()
        // columnlen sum i-1 s[i-1] columnlen+i-1
        yoink()
        // columnlen sum i-1 s[i-1] s[columnlen+i-1]
        subabs()
        // columnlen sum i-1 |s[i-1]-s[columnlen+i-1]|
        // columnlen sum i-1 dist
        roll(3, -1)
        // columnlen i-1 dist sum
        add()
        // columnlen i-1 sum+dist
        swap2()
        // columnlen sum+dist i-1
        CS()
    }
    pop()
    // Top of the stack is the result
    leaveTop()
}