package cz.sejsel

fun parseWord(word: String): Op {
    assert(!word.any { it.isWhitespace() })
    return when (word.lowercase()) {
        // Note that while Op.Nop exists, it is not a part of the language.
        "praise" -> Op.Praise
        "pop" -> Op.Pop
        // The original definition of the instruction was "¬"
        // but that character is not supported by KSP opendata.
        "¬" -> Op.Pop2
        "pop2" -> Op.Pop2
        "max" -> Op.Max
        "l-swap" -> Op.LSwap
        "lroll" -> Op.Roll
        "-ff" -> Op.FF
        "swap" -> Op.Swap
        "kpi" -> Op.KPi
        "++" -> Op.Increment
        "u" -> Op.Universal
        "rem" -> Op.Remainder
        "%" -> Op.Modulo
        "tetr" -> Op.TetrationNumIters
        "^^" -> Op.TetrationItersNum
        "m" -> Op.Median
        "cs" -> Op.DigitSum
        "lensum" -> Op.LenSum
        "bitshift" -> Op.Bitshift
        "and" -> Op.And
        // The original definition of the instruction was Σ
        // but that character is not supported by KSP opendata.
        // "Σ".to_lowercase() -- "σ".
        "σ" -> Op.Sum
        "sum" -> Op.Sum
        "d" -> Op.GcdN
        "gcd" -> Op.Gcd2
        "qeq" -> Op.Qeq
        "funkcia" -> Op.Funkcia
        "bulkxor" -> Op.BulkXor
        "brz" -> Op.BranchIfZero
        "call" -> Op.Call
        "goto" -> Op.Goto
        "j" -> Op.Jump
        "rev" -> Op.Rev
        "spanek" -> Op.Sleep
        "deez" -> Op.Deez
        else -> throw IllegalArgumentException("Unknown word: $word")
    }
}

/**
 * Parses a program from a string.
 *
 * @throws IllegalArgumentException if an instruction is not recognized.
 */
fun parseProgram(program: String): List<Op> {
    return program.split("\\s".toRegex()).map(::parseWord)
}
