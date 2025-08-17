package cz.sejsel.ksplang.wasm

fun UInt.bitsToLong(): Long = toLong() and 0xFF_FF_FF_FFL
fun Int.bitsToLong(): Long = toLong() and 0xFF_FF_FF_FFL
