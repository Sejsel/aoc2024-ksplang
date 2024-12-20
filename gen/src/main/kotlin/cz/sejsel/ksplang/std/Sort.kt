package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero

/**
 * Sorts k values on the bottom of the stack.
 *
 * Uses a state-of-the-art sorting algorithm ICan'tBelieveItCanSort, for further reference see paper: https://arxiv.org/abs/2110.01111
 */
fun ComplexBlock.sort(): ComplexFunction = complexFunction("sort") {
    // k
    dup()
    // k k
    doWhileNonZero {
        // k i+1
        decPositive()
        dupSecond()
        // k i k
        doWhileNonZero {
            // k i j+1
            decPositive()
            // k i j
            dupAb()
            dupAb()
            // k i j i j i j
            yoink()
            // k i j i j i s[j]
            swap2()
            // k i j i j s[j] i
            yoinkDestructive()
            // k i j i j s[j] s[i]
            dupAb()
            min2()
            // k i j i j s[j] s[i] min(s[j], s[i])
            roll(3, 1)
            // k i j i j min(s[j], s[i]) s[j] s[i]
            max2()
            // k i j i j min(s[j], s[i]) max(s[j],s[i])
            swap2()
            // k i j i j greater lesser
            roll(4, 1)
            // k i j lesser i j greater
            swap2()
            // k i j lesser i greater j
            yeet()
            // s[j] = greater
            // k i j lesser i
            yeet()
            // s[i] = lesser
            // k i j
            CS()
            // k i j CS(j)
        }
        // k i 0
        pop()
        // k i
        CS()
        // k i CS(i)
    }
    // k 0
    pop()
    pop()
}
