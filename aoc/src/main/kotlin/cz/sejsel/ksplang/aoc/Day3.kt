package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.IfZero
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.std.*
import java.io.File

// Day 3
// https://adventofcode.com/2024/day/3
fun main() {
    val builder = KsplangBuilder()
    val program = builder.build(day3Part1())
    val instructionCount = program.trim().split("\\s+".toRegex()).count()
    File("ksplang/3-1.ksplang").writeText(program)
    println("Generated program for day 3 part 1, $instructionCount instructions")
    val program2 = builder.build(day3Part2())
    val instructionCount2 = program2.trim().split("\\s+".toRegex()).count()
    File("ksplang/3-2.ksplang").writeText(program2)
    println("Generated program for day 3 part 2, $instructionCount2 instructions")
}

// We are using text input for this for pretty obvious reasons.
// Outer shell copy-pasted from day3Part1, but not really worth trying to share more code.
fun day3Part2() = buildComplexFunction {
    // [input]
    push(-1)
    // [input] -1
    //         ^ makes it easy to stop a checkMul without any special casing for end of file input.
    stacklen(); dec()
    // [input] -1 inputlen
    push(0)
    // [input] -1 inputlen 0
    // [input] -1 inputlen sum
    swap2()
    // [input] -1 sum inputlen
    push(1)
    push(0)
    // [input] -1 sum inputlen 1       0
    // [input] -1 sum inputlen enabled start
    doWhileNonZero { // over start
        // [input] -1 sum inputlen enabled i
        dup()
        // [input] -1 sum inputlen enabled i i
        checkDo()
        // [input] -1 sum inputlen enabled i 0/1
        ifZero {
            pop()
        } otherwise {
            // [input] -1 sum inputlen enabled i 1
            permute("enabled i 1", "1 i enabled")
            // [input] -1 sum inputlen 1 i enabled
            pop()
            // [input] -1 sum inputlen enabled=1 i
        }

        // [input] -1 sum inputlen enabled i
        dup()
        // [input] -1 sum inputlen enabled i i
        checkDont()
        // [input] -1 sum inputlen enabled i 0/1
        ifZero {
            pop()
        } otherwise {
            // [input] -1 sum inputlen enabled i 1
            pop()
            push(0)
            // [input] -1 sum inputlen enabled i 0
            permute("enabled i 0", "0 i enabled")
            // [input] -1 sum inputlen 0 i enabled
            pop()
            // [input] -1 sum inputlen enabled=0 i
        }

        // [input] -1 sum inputlen enabled i
        dupSecond()
        // [input] -1 sum inputlen enabled i enabled
        ifZero {
            pop()
            // [input] -1 sum inputlen enabled i
            push(0)
            // [input] -1 sum inputlen enabled 0
        } otherwise {
            pop()
            // [input] -1 sum inputlen enabled i
            dup()
            // [input] -1 sum inputlen enabled i i
            checkMul()
            // [input] -1 sum inputlen enabled i 0/1
        }

        ifZero { // not a valid mul
            pop()
            // [input] -1 sum inputlen enabled i
        } otherwise { // valid mul
            // [input] -1 sum inputlen enabled i 0/1
            pop()
            // [input] -1 sum inputlen enabled i
            dup()
            // [input] -1 sum inputlen enabled i i
            add(4) // skip mul(
            parseNonNegativeNum(','.code)
            // [input] -1 sum inputlen enabled i num1 indexOf(,)
            inc()
            parseNonNegativeNum(')'.code)
            // [input] -1 sum inputlen enabled i num1 num2 indexOf())
            pop()
            // [input] -1 sum inputlen enabled i num1 num2
            mul()
            // [input] -1 sum inputlen enabled i num1*num2
            permute("sum inputlen enabled i val", "i inputlen enabled val sum")
            // [input] -1 i inputlen enabled num1*num2 sum
            add()
            // [input] -1 i inputlen enabled sum
            permute("i inputlen enabled sum", "sum inputlen enabled i")
            // [input] -1 sum inputlen enabled i
        }
        // [input] -1 sum inputlen enabled i
        inc()
        // [input] -1 sum inputlen enabled i+1
        dupThird()
        // [input] -1 sum inputlen enabled i+1 inputlen
        dupSecond()
        // [input] -1 sum inputlen enabled i+1 inputlen i+1
        negate()
        add()
        // [input] -1 sum inputlen enabled i+1 inputlen-(i+1)
    }
    // [input] -1 sum inputlen enabled 0
    pop(); pop(); pop()
    leaveTop()
}

// We are using text input for this for pretty obvious reasons.
fun day3Part1() = buildComplexFunction {
    // [input]
    push(-1)
    // [input] -1
    //         ^ makes it easy to stop a checkMul without any special casing for end of file input.
    stacklen(); dec()
    // [input] -1 inputlen
    push(0)
    // [input] -1 inputlen 0
    // [input] -1 inputlen sum
    swap2()
    // [input] -1 sum inputlen
    // [input] -1 sum start
    doWhileNonZero { // over start
        // [input] -1 sum i+1
        dec()
        // [input] -1 sum i
        dup()
        // [input] -1 sum i i
        checkMul()
        // [input] -1 sum i 0/1
        ifZero { // not a valid mul
            pop()
            // [input] -1 sum i
        } otherwise { // valid mul
            // [input] -1 sum i 0/1
            pop()
            // [input] -1 sum i
            dup()
            // [input] -1 sum i i
            add(4) // skip mul(
            parseNonNegativeNum(','.code)
            // [input] -1 sum i num1 indexOf(,)
            inc()
            parseNonNegativeNum(')'.code)
            // [input] -1 sum i num1 num2 indexOf())
            pop()
            // [input] -1 sum i num1 num2
            mul()
            // [input] -1 sum i num1*num2
            permute("sum i val", "i val sum")
            // [input] -1 i num1*num2 sum
            add()
            // [input] -1 i sum
            swap2()
            // [input] -1 sum i
        }
        CS()
    }
    // [input] -1 sum 0
    pop()
    leaveTop()
}

// i -> does `do()` start at i
fun ComplexBlock.checkDo() = complexFunction {
    // i
    dup(); yoink()
    // i s[i]
    ifIs('d'.code) {
        pop(); inc(); dup(); yoink()
        // i+1 s[i+1]
        ifIs('o'.code) {
            pop(); inc(); dup(); yoink()
            // i+2 s[i+2]
            ifIs('('.code) {
                pop(); inc(); dup(); yoink()
                // i+3 s[i+3]
                ifIs(')'.code) {
                    pop(); pop(); push(1)
                } otherwise {
                    pop(); pop(); push(0)
                }
            } otherwise {
                pop(); pop(); push(0)
            }
        } otherwise {
            pop(); pop(); push(0)
        }
    } otherwise {
        pop(); pop(); push(0)
    }
}

// i -> does `don't()` start at i
fun ComplexBlock.checkDont() = complexFunction {
    // i
    dup(); yoink()
    // i s[i]
    ifIs('d'.code) {
        pop(); inc(); dup(); yoink()
        // i+1 s[i+1]
        ifIs('o'.code) {
            pop(); inc(); dup(); yoink()
            // i+2 s[i+2]
            ifIs('n'.code) {
                pop(); inc(); dup(); yoink()
                // i+3 s[i+3]
                ifIs('\''.code) {
                    pop(); inc(); dup(); yoink()
                    // i+4 s[i+4]
                    ifIs('t'.code) {
                        pop(); inc(); dup(); yoink()
                        // i+5 s[i+5]
                        ifIs('('.code) {
                            pop(); inc(); dup(); yoink()
                            // i+6 s[i+6]
                            ifIs(')'.code) {
                                pop(); pop(); push(1)
                            } otherwise {
                                pop(); pop(); push(0)
                            }
                        } otherwise {
                            pop(); pop(); push(0)
                        }
                    } otherwise {
                        pop(); pop(); push(0)
                    }
                } otherwise {
                    pop(); pop(); push(0)
                }
            } otherwise {
                pop(); pop(); push(0)
            }
        } otherwise {
            pop(); pop(); push(0)
        }
    } otherwise {
        pop(); pop(); push(0)
    }
}

/**
 * Given i, checks if text matches mul([0-9]{1,3},[0-9]{1,3}) starting from i. Stops on any invalid character.
 *
 * If valid, returns 1
 * If invalid, returns 0
 */
fun ComplexBlock.checkMul() = complexFunction {
    // i
    dup(); yoink()
    // i s[i]
    ifIs('m'.code) {
        pop(); inc(); dup(); yoink()
        // i+1 s[i+1]
        ifIs('u'.code) {
            pop(); inc(); dup(); yoink()
            // i+2 s[i+2]
            ifIs('l'.code) {
                pop(); inc(); dup(); yoink()
                // i+3 s[i+3]
                ifIs('('.code) {
                    pop(); inc(); dup(); yoink()
                    // i+4 s[i+4]
                    push('0'.code.toLong())
                    push('9'.code.toLong())
                    isInRange()
                    // i+4 1/0
                    ifIs(1) {
                        pop(); inc(); dup(); yoink()
                        // i+5 s[i+5]
                        push('0'.code.toLong())
                        push('9'.code.toLong())
                        isInRange()
                        ifIs(1) { // mul(00
                            pop(); inc(); dup(); yoink()
                            push('0'.code.toLong())
                            push('9'.code.toLong())
                            isInRange()
                            ifIs(1) { // mul(000
                                pop(); inc(); dup(); yoink()
                                ifIs(','.code) { //mul(000,
                                    checkMulPastComma()
                                } otherwise {
                                    pop(); pop(); push(0)
                                }
                            } otherwise {
                                pop(); dup(); yoink()
                                ifIs(','.code) { //mul(00,
                                    checkMulPastComma()
                                } otherwise {
                                    pop(); pop(); push(0)
                                }
                            }
                        } otherwise {
                            // i+5 0
                            pop(); dup(); yoink()
                            // i+5 s[i+5]
                            ifIs(','.code) { // mul(0,
                                checkMulPastComma()
                            } otherwise {
                                pop(); pop(); push(0)
                            }
                        }
                    } otherwise {
                        // zero length number, invalid
                        pop(); pop(); push(0)
                    }
                } otherwise {
                    pop(); pop(); push(0)
                }
            } otherwise {
                pop(); pop(); push(0)
            }
        } otherwise {
            pop(); pop(); push(0)
        }
    } otherwise {
        pop(); pop(); push(0)
    }
}

fun ComplexBlock.checkMulPastComma() = complexFunction {
    // i-1 x
    pop(); inc(); dup(); yoink()
    // i s[i]
    push('0'.code.toLong())
    push('9'.code.toLong())
    isInRange()
    ifIs(1) { // ,0
        pop(); inc(); dup(); yoink()
        push('0'.code.toLong())
        push('9'.code.toLong())
        isInRange()
        ifIs(1) { // ,00
            pop(); inc(); dup(); yoink()
            push('0'.code.toLong())
            push('9'.code.toLong())
            isInRange()
            ifIs(1) { // ,000
                pop(); inc(); dup(); yoink()
                ifIs(')'.code) { // ,000)
                    pop(); pop(); push(1)
                } otherwise {
                    pop(); pop(); push(0)
                }
            } otherwise {
                pop(); dup(); yoink()
                ifIs(')'.code) { // ,00)
                    pop(); pop(); push(1)
                } otherwise {
                    pop(); pop(); push(0)
                }
            }
        } otherwise {
            pop(); dup(); yoink()
            ifIs(')'.code) { // ,0)
                pop(); pop(); push(1)
            } otherwise {
                pop(); pop(); push(0)
            }
        }
    } otherwise {
        pop(); pop(); push(0)
    }
}

/**
 * Checks if a number is a constant, using subabs.
 *
 * Do not use with values which may be more than 2^63-1 apart!
 */
fun ComplexBlock.ifIs(n: Long, init: IfZero.() -> Unit): IfZero {
    // x
    dup()
    // x x
    push(n)
    subabs()
    // x |x-n|

    val f = IfZero(popChecked = true)
    f.init()
    children.add(f)
    return f
}

/**
 * Checks if a number is a constant, using subabs.
 *
 * Do not use with values which may be more than 2^63-1 apart!
 */
fun ComplexBlock.ifIs(n: Int, init: IfZero.() -> Unit) = ifIs(n.toLong(), init)
