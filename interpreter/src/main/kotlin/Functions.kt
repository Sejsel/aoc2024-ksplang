package cz.sejsel

import com.google.common.math.LongMath
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Takes two numbers from the stack and decomposes both into their prime factors.
 * Then, it removes all prime factors that divide both numbers from the decompositions.
 * The result is the product of all the remaining prime factors, including their exponents, mod 1_000_000_007.
 * If the set of remaining prime factors is empty, the result is zero.
 *
 * For example, for the numbers 100 = 2^2 * 5^2 and 54 = 3^3 * 2 the prime factor 2 is removed because it appears
 * in the decompositions of both numbers. This leaves 5^2*3^3, which equals 675.
 * Mod 1_000_000_007 does not change the result for such small numbers.
 */
const val FUNKCIA_MOD: Long = 1_000_000_007

fun digitSum(n: Long): Long {
    if (n == Long.MIN_VALUE) {
        return 89
    }

    var num = abs(n)
    var result = 0L
    while (num != 0L) {
        result += num % 10
        num /= 10
    }
    return result
}

fun funkcia(a: Long, b: Long): Long {
    if (a == b || (a < 2 && b < 2)) {
        return 0L
    }

    // We only need to factorize one of the numbers. We hope the smaller one is the easier one.
    val (smaller, bigger) = if (a < b) {
        a to b
    } else {
        b to a
    }

    var result = bigger

    val factors = factorize(smaller)

    val remainingFactors = factors.filter { (factor, count) ->
        var occursInBigger = false
        while (result % factor == 0L) {
            result /= factor
            occursInBigger = true
        }

        !occursInBigger
    }

    remainingFactors.forEach { (factor, count) ->
        repeat(count) {
            result = (result * (factor % FUNKCIA_MOD)) % FUNKCIA_MOD
        }
    }
    result %= FUNKCIA_MOD

    if (result == 1L) {
        return 0L
    }
    return result
}

fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val temp = y
        y = x % y
        x = temp
    }
    return x
}

fun lensum(a: Long, b: Long): Long {
    fun len(num: Long): Long {
        if (num == Long.MIN_VALUE) {
            // We cannot use abs
            return 19L
        }

        if (num == 0L) {
            return 0L
        }

        return LongMath.log10(abs(num), RoundingMode.DOWN) + 1L
    }

    return len(a) + len(b)
}

fun cursedDiv(upper: Long, lower: Long): Long {
    return if (upper % lower == 0L) {
        upper / lower
    } else {
        upper % lower
    }
}

private fun factorize(a: Long): Map<Long, Int> {
    var num = a
    val factors = mutableMapOf<Long, Int>()
    var factor = 2L

    val maxFactor = ceil(sqrt(a.toDouble())).toLong()

    while (num > 1 && factor <= maxFactor) {
        if (num % factor == 0L) {
            factors[factor] = factors.getOrDefault(factor, 0) + 1
            num /= factor
        } else {
            factor++
        }
    }

    if (num > 1) {
        factors[num] = factors.getOrDefault(num, 0) + 1
    }

    return factors
}