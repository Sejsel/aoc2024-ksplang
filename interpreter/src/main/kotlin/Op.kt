package cz.sejsel

enum class Op {
    /** Not a real operation, only used internally for testing */
    Nop,
    Praise,
    Pop,
    Pop2,
    Max,
    LSwap,
    Roll,
    FF,
    Swap,
    KPi,
    Increment,
    Universal,
    Remainder,
    Modulo,
    TetrationNumIters,
    TetrationItersNum,
    Median,
    DigitSum,
    LenSum,
    Bitshift,
    And,
    Sum,
    Gcd2,
    GcdN,
    Qeq,
    Funkcia,
    BulkXor,
    BranchIfZero,
    Call,
    Goto,
    Jump,
    Rev,
    Sleep,
    Deez;

    override fun toString(): String =
        when (this) {
            Nop -> "nop"
            Praise -> "praise"
            Pop -> "pop"
            Pop2 -> "pop2"
            Max -> "max"
            LSwap -> "L-swap"
            Roll -> "lroll"
            FF -> "-ff"
            Swap -> "swap"
            KPi -> "kPi"
            Increment -> "++"
            Universal -> "u"
            Remainder -> "REM"
            Modulo -> "%"
            TetrationNumIters -> "tetr"
            TetrationItersNum -> "^^"
            Median -> "m"
            DigitSum -> "CS"
            LenSum -> "lensum"
            Bitshift -> "bitshift"
            And -> "And"
            Sum -> "sum"
            Gcd2 -> "gcd"
            GcdN -> "d"
            Qeq -> "qeq"
            Funkcia -> "funkcia"
            BulkXor -> "bulkxor"
            BranchIfZero -> "BRZ"
            Call -> "call"
            Goto -> "GOTO"
            Jump -> "j"
            Rev -> "rev"
            Sleep -> "SPANEK"
            Deez -> "deez"
        }
}