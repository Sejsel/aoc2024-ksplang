package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.doWhileZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise


/**
 * Rotate the top `length` elements of the stack by `distance` right (towards stack top).
 */
fun Block.roll(length: Long, distance: Long): SimpleFunction = function("roll($length, $distance)") {
    // TODO: Optimize -1 case, use it when it's shorter and vice versa
    push(distance)
    push(length)
    lroll()
}

/**
 * Swap the top two elements of the stack.
 */
fun Block.swap2() = roll(2, 1)

/**
 * Finds the length of the stack.
 *
 * Signature: `<stack> -> <stack> len(stack)`
 */
fun ComplexBlock.stacklen(): ComplexFunction = complexFunction("stacklen") {
    push(0)
    stacklenWithMin()
}

/**
 * Finds the length of the stack, if it is known that it is at least `minStackLen`.
 *
 * - `minStackLen` must be non-negative.
 * - `minStackLen` must not count itself (it has to be at most the size of the stack without it).
 *
 * Signature: `<stack> minStackLen -> <stack> len(stack)`
 */
fun ComplexBlock.stacklenWithMin(): ComplexFunction = complexFunction("stacklenWithMin") {
    // minStackLen
    // i
    doWhileZero {
        // i
        push(0)
        // i 0
        swap2()
        // 0 i
        dup()
        dup()
        // 0 i i i
        CS()
        // 0 i i i X
        swap2()
        // 0 i i X i
        swap()
        // 0/X i i s[i]
        // if dup_fourth is not zero, we found the end.
        // we can do a "cs_fourth" instead even though it replaces the 0/X with its CS
        // - this replaces the 0? - zero/X with its CS, and we will be checking the zero/X
        // - this only works because we know the stack is not empty (CS of correct i is not zero)
        roll(4, 3)
        // i i s[i] 0/X
        CS()
        // i i s[i] 0/X CS(0/X)
        roll(5, 1)
        // CS(0/X) i i s[i] 0/X
        roll(3, 1)
        // CS(0/X) i 0/X i s[i]
        swap2()
        // CS(0/X) i 0/X s[i] i
        swap()
        // 0 i 0/X X/CS(X)
        pop()
        // 0 i 0/X
        roll(3, 1)
        // 0/X 0 i
        inc()
        // 0/X 0 i+1
        pop2()
        // 0/X i+1
        swap2()
        // i+1 0?
    }
    decPositive()
}

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Does not destroy the value.
 *
 * Fairly expensive, if the value can be destroyed, use [yoinkDestructive] instead
 *
 * Signature: ```n -> s[n]; s[n] stays s[n]```
 */
fun ComplexBlock.yoink(): ComplexFunction = complexFunction("yoink") {
    // n
    dup()
    // We need to push something there temporarily,
    // so we just use CS as that is efficient.
    CS()
    // n n CS(n)
    roll(2, 1)
    // n CS(n) n
    swap()
    // n s[n]
    dup()
    // n s[n] s[n]
    roll(3, 2)
    // s[n] s[n] n
    swap()
    // s[n] CS(n)
    pop()
    // s[n]
}

/**
 * Yoinks a value from the specified index in the stack (inverse of [yeet]). Destroys the value
 * (leaves a random value in its place - the digit sum of the index).
 *
 * Cheaper than [yoink] if the value can be destroyed.
 *
 * Signature: ```n -> s[n]; s[n] is removed```
 */
fun ComplexBlock.yoinkDestructive(): ComplexFunction = complexFunction("yoinkDestructive") {
    // n
    CS()
    // n X
    swap2()
    // X n
    swap()
    // s[n] = CS(n)
    // s[n]
}

/**
 * Yeets a value from the top of the stack to a specific index of the stack (inverse of [yoink]).
 *
 * Signature: ```x i -> <nothing>; stack[i] = x```
 */
fun Block.yeet(): SimpleFunction = function("yeet") {
    swap()
    pop()
}

/** Clears the entire stack, leaving only the top value. */
fun ComplexBlock.leaveTop() = complexFunction("leaveTop") {
    // [stack]
    stacklen()
    // [stack] stacklen
    push(1); swap2()
    // [stack|top] 1 stacklen
    lroll()
    // [top|stack]
    stacklen()
    // [top|stack] stacklen
    dec()
    doWhileNonZero {
        dec()
        // [top|stack] i-1
        pop2()
        // [top|stack] i
        CS()
    }
    pop()
    // [stack]
}

/**
 * With n on top of the stack, pops n elements from the stack (not counting n), n is also consumed.
 *
 * e.g. `1 2 3 4 2 -> 1 2` (*n* was 2)
 */
fun ComplexBlock.popMany() = complexFunction("popMany") {
    ifZero {
        pop()
    } otherwise {
        doWhileNonZero {
            pop2()
            dec()
            CS()
        }
        pop()
    }
}

/**
 * Pops the *n*-th element from the top of the stack where *n* is the topmost value on the stack (1-indexed, not counting *n*).
 * See also [popKth] for a version with a constant *n*.
 *
 * Requires *n* to be 1 or bigger.
 *
 * Example: `1 2 3 4 2 -> 1 2 4` (*n* was 2)
 *
 * Signature: `n -> ` and *n*-th element from top is removed (not counting *n*)
 */
fun Block.popNth() = function("popNth") {
    // n
    push(-1)
    // n -1
    swap2()
    // -1 n
    lroll()
    // nth
    pop()
}

/**
 * Pops the k-th element from the top of the stack (1-indexed).
 *
 * See also [popNth] for a version which gets the *k* from the stack.
 */
fun Block.popKth(n: Long) = function("popKth($n)") {
    require(n >= 1) { "n must be at least 1" }
    if (n == 1L) {
        pop()
    } else if (n == 2L) {
        pop2()
    } else {
        roll(n, -1)
        pop()
    }
}

fun Block.pop3() = popKth(3)
fun Block.pop4() = popKth(4)
fun Block.pop5() = popKth(5)
fun Block.pop6() = popKth(6)


/**
 * Pops the *n*-th element from the top of the stack and moves it to the top of the stack (basically [dupNth] + [popNth]).
 * 1-indexed, not counting *n*. **Requires *n* to be 1 or bigger.**
 *
 * Example: `1 2 3 4 2 -> 1 2 4 3` (*n* was 2)
 *
 * Signature: `n -> s[stacklen-1-n]` and *n*-th element from top is removed
 */
fun Block.moveNthToTop() = function("moveNthToTop") {
    // n
    dup()
    // n n
    inc()
    // n n+1
    dupNth()
    // n nth
    swap2()
    // nth n
    inc()
    // nth n+1
    popNth()
    // nth
}

/**
 * Sets the *n*-th element from the top of the stack to a new value.
 * 1-indexed, not counting *n*. **Requires *n* to be 1 or bigger.**
 *
 * Example: `1 2 3 4 2 42 -> 1 2 42 4` (*n* was 2)
 *
 * Signature: `n x -> ` and `s[stacklen-1-n] = x`
 */
fun Block.setNth() = function("setNth") {
    // n x
    swap2()
    // x n
    dup()
    // x n n
    push(-1)
    // x n n -1
    swap2()
    // x n -1 n
    inc(); inc()
    // x n -1 n+2
    lroll()
    // x n replaced
    pop()
    // x n
    push(1)
    // x n 1
    swap2()
    // x 1 n
    lroll()
    //
}

/**
 * Sets the *k*-th element from the top of the stack to a new value.
 * 1-indexed, not counting the top value. **Requires *x* to be 1 or bigger.**
 *
 * Example: `1 2 3 4 42 -> 1 2 42 4` (*k* was 2)
 *
 * Signature: `x -> ` and `s[stacklen-1-n] = x`
 */
fun Block.setKth(k: Int) = setKth(k.toLong())

/**
 * Sets the *k*-th element from the top of the stack to a new value.
 * 1-indexed, not counting the top value. **Requires *x* to be 1 or bigger.**
 *
 * Example: `1 2 3 4 42 -> 1 2 42 4` (*k* was 2)
 *
 * Signature: `x -> ` and `s[stacklen-1-n] = x`
 */
fun Block.setKth(k: Long) = function("setKth($k)") {
    require(k >= 1) { "k must be at least 1" }
    roll(k + 1, -1)
    pop()
    roll(k, 1)
}

// Adaptation of the following code:
// https://stackoverflow.com/questions/53749357/idiomatic-way-to-create-n-ary-cartesian-product-combinations-of-several-sets-of/53763936#53763936
private fun cartesianProduct(sets: List<Set<*>>): Set<List<*>> {
    return sets.fold(listOf(listOf<Any?>())) { acc, set ->
        acc.flatMap { list -> set.map { element -> list + element } }
    }.toSet()
}

/**
 * Permutes the top elements according to the given permutation.
 *
 * @param before Values in the original order separated by spaces, e.g. "a b c" or "sum i j"
 * @param after Values in the wanted order separated by spaces, e.g. "b c a" or "i j sum"
 *
 */
fun Block.permute(before: String, after: String): SimpleFunction {
    val beforeValues = before.split(" ")
    val afterValues = after.split(" ")

    check(beforeValues.size == afterValues.size) { "Before and after must have the same number of values" }
    check(beforeValues.toSet() == afterValues.toSet()) { "Before and after must have the same values" }
    check(beforeValues.size == beforeValues.toSet().size) { "Before must not contain duplicates" }
    check(afterValues.size == afterValues.toSet().size) { "After must not contain duplicates" }

    val permutation = afterValues.map { beforeValues.indexOf(it) }

    val possibleRolls = buildList {
        for (length in 1..permutation.size) {
            for (distance in 1..<length) {
                add(length.toLong() to distance.toLong())
            }
        }
    }

    fun checkSequence(rollSequence: List<Pair<Long, Long>>): Boolean {
        var objects = (0..<permutation.size).toList()

        rollSequence.forEach { (length, distance) ->
            val prefix = objects.take(objects.size - length.toInt())
            val moving = objects.takeLast(length.toInt())
            val end = moving.takeLast(distance.toInt()) + moving.take(moving.size - distance.toInt())
            objects = prefix + end
        }

        val isCorrect = objects == permutation
        return isCorrect
    }

    for (rolls in 0..Int.MAX_VALUE) {
        val sets = buildList {
            repeat(rolls) {
                add(possibleRolls.toSet())
            }
        }

        if(checkSequence(emptyList())) {
            return function("permute(\"$before\", \"$after\")") {}
        }

        cartesianProduct(sets).forEach {
            @Suppress("UNCHECKED_CAST")
            val rollSequence = it as? List<Pair<Long, Long>> ?: error("still better than Python")
            val isCorrect = checkSequence(rollSequence)
            if (isCorrect) {
                return function("permute(\"$before\", \"$after\")") {
                    rollSequence.forEach { (length, distance) ->
                        roll(length, distance)
                    }
                }
            }
        }
    }

    error("Permutation not possible. Also probably beyond the heat death of the universe.")
}

/**
 * Finds the first instance of a value from a given index (inclusive).
 * If the value does not exist, this will behave unpredictably (crash or wrong results).
 * If i is outside of the stack, this will crash.
 *
 * Signature: ```i a -> first(s[i] == a)```
 */
fun ComplexBlock.findUnsafe() = complexFunction("findUnsafe") {
    // i a
    swap2()
    // a i
    doWhileZero {
        // a i
        dup()
        // a i i
        yoink()
        // a i s[i]
        dupThird()
        // a i s[i] a
        cmp()
        // a i CMP(s[i]==a)
        zeroNot()
        // a i s[i]==a?1:0
        swap2()
        inc()
        swap2()
        // a i+1 s[i]==a?1:0
    }
    // a i+1
    decPositive()
    // a i
    pop2()
    // i
}
