package cz.sejsel.ksplang.interpreter

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

    companion object {
        fun byId(id: Int): Op? {
            return when (id) {
                0 -> Praise
                1 -> Pop
                2 -> Pop2
                3 -> Max
                4 -> LSwap
                5 -> Roll
                6 -> FF
                7 -> Swap
                8 -> KPi
                9 -> Increment
                10 -> Universal
                11 -> Remainder
                12 -> Modulo
                13 -> TetrationNumIters
                14 -> TetrationItersNum
                15 -> Median
                16 -> DigitSum
                17 -> LenSum
                18 -> Bitshift
                19 -> And
                20 -> Sum
                21 -> Gcd2
                22 -> GcdN
                23 -> Qeq
                24 -> Funkcia
                25 -> BulkXor
                26 -> BranchIfZero
                27 -> Call
                28 -> Goto
                29 -> Jump
                30 -> Rev
                31 -> Sleep
                32 -> Deez
                else -> null
            }
        }
    }
}