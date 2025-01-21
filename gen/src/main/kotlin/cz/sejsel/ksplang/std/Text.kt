package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.doWhileNonZero

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parse_number(' ') will parse a number that is followed by a space.
 *
 * Signature: i -> number index_of_terminator
 */
fun ComplexBlock.parseNonNegativeNum(terminator: Int, base: Int = 10) = parseNonNegativeNum(terminator.toLong(), base)

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parse_number(' ') will parse a number that is followed by a space.
 *
 * Signature: i -> number index_of_terminator
 */
fun ComplexBlock.parseNonNegativeNum(terminator: Long, base: Int = 10) = complexFunction("parseNum($terminator, $base)") {
    // start
    dup()
    // start start
    push(terminator)
    // start start $terminator
    findUnsafe()
    // start index_of_terminator
    parseNonNegativeNumInRange(base)
    // res index_of_terminator
}

/**
 * Parse a text number starting on index i and ending before terminator. If the terminator is not found,
 * undefined behavior occurs. If there is no valid number, undefined behavior occurs.
 * For example parse_number(' ') will parse a number that is followed by a space.
 *
 * Signature: i terminator -> number index_of_terminator
 */
fun ComplexBlock.parseNonNegativeNum2(base: Int = 10) = complexFunction("parseNum($base)") {
    // start terminator
    dupSecond()
    // start terminator start
    swap2()
    // start start terminator
    findUnsafe()
    // start index_of_terminator
    parseNonNegativeNumInRange(base)
    // res index_of_terminator
}

/**
 * Parse a text number starting on index i and ending before terminator.
 * If there is no valid number, undefined behavior occurs.
 *
 * Signature: from index_of_terminator -> number index_of_terminator
 */
fun ComplexBlock.parseNonNegativeNumInRange(base: Int = 10) = complexFunction("parseNonNegativeNumInRange($base)") {
    require(base >= 2)
    require(base <= 10) { "Base must be at most 10 because only 0-9 are consecutive in ASCII" }

    // start index_of_terminator
    dup()
    // start index_of_terminator i
    push(0)
    push(1)
    // start index_of_terminator i 0 1
    roll(3, 2)
    // start index_of_terminator 0 1 i
    doWhileNonZero {
        // start index_of_terminator res pow i+1
        dec()
        // start _ res pow i
        dup()
        yoink()
        // start _ res pow i s[i]
        // TODO: Handle a '-' sign here and turn this into parseNum
        push(-'0'.code)
        add()
        // start _ res pow i value
        dupThird()
        mul()
        // start _ res pow i value*pow
        permute("res pow i val", "pow i res val")
        add()
        // start _ pow i res
        permute("pow i res", "i res pow")
        push(10); mul()
        // start _ i res pow*10
        permute("i res pow", "res pow i")
        // start _ res pow i
        dup()
        // start _ res pow i i
        dupSixth()
        // start _ res pow i i start
        subabs()
        // start _ res pow i |i-start|
    }
    // start index_of_terminator pow i
    pop()
    pop()
    // start index_of_terminator res
    roll(3, 1)
    // res start index_of_terminator
    pop2()
    // res index_of_terminator
}
