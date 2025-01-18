package cz.sejsel

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

fun funkcia(a: Long, b: Long): Long {
    if (a == b || (a < 2 && b < 2)) {
        return 0L
    }

    val aFactors = factorize(a)
    val bFactors = factorize(b)

    var result = 1L
    fun applyFactors(factors: Map<Long, Int>, otherFactors: Map<Long, Int>) {
        factors.forEach { factor, count ->
            if (factor in otherFactors) {
                return@forEach
            }

            repeat(count) {
                result = (result * (factor % FUNKCIA_MOD)) % FUNKCIA_MOD
            }
        }
    }

    applyFactors(aFactors, bFactors)
    applyFactors(bFactors, aFactors)

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