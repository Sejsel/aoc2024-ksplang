package cz.sejsel.ksplang.std

import cz.sejsel.ksplang.dsl.core.SimpleFunction

fun SimpleFunction.push(n: Long) {
    // TODO: Port "short pushes"
    if (n == 0L) {
        // Requires a non-empty stack.
        // CS CS lensum will take any value down to 0-5
        // CS duplicates it
        // funkcia turns two duplicates into 0
        // result = [CS, CS, lensum, CS, funkcia]
        CS()
        CS()
        lensum()
        CS()
        funkcia()
    } else if (n in 1..15) {
        // We must handle n == 1 here.
        // This is by no means optimal.
        push(0)
        repeat(n.toInt()) {
            inc()
        }
    } else if (n > 0) {
        // We could just repeat inc() n times, but that is not viable for big numbers.
        // Instead, we build it out of powers of two and sum those up
        /* Original python code:
        result = []

        nums = 0
        for i in range(n.bit_length()):
            if n & (1 << i):
                result.append(push(1))
                if i == 1:
                    # We can duplicate the 1 by using m or CS
                    result.append(m)
                else:
                    result.append(push(i))
                result.append(bitshift)
                nums += 1

        for _ in range(nums - 1):
            result.append(_add)

         */

        TODO()
    } else {
        TODO()
    }
}
