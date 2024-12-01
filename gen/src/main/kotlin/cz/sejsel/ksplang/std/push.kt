package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.SimpleFunction

fun SimpleFunction.push(constant: Long) {
    if (constant == 0L) {
        // Requires a non-empty stack.
        // CS CS lensum will take any value down to 0-5
        // CS duplicates it
        // funkcia turns two duplicates into 0
        // result = [CS, CS, lensum, CS, funkcia]
        function {
            CS()
            CS()
            lensum()
            CS()
            funkcia()
        }
    } else {
        TODO()
    }
}