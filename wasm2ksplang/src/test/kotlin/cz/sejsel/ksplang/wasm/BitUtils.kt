package cz.sejsel.ksplang.wasm

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe

fun UInt.bitsToLong(): Long = toLong() and 0xFF_FF_FF_FFL
fun Int.bitsToLong(): Long = toLong() and 0xFF_FF_FF_FFL

private fun Long.areTop32BitsZero(): Boolean = (this and -4294967296L) == 0L

private fun top32BitsAreZero() = Matcher<Long> { value ->
    MatcherResult(
        value.areTop32BitsZero(),
        { "Expected top 32 bits of $value to be zero" },
        { "Expected top 32 bits of $value to not be zero" },
    )
}


fun Long.top32BitsShouldBeZero(): Long {
    this shouldBe top32BitsAreZero()
    return this
}