package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.std.*

/**
 * Keeps track of the position of the allocator.
 *
 * See also [alloc].
 */
data class Allocator(
    val position: Variable
)

/**
 * Allocates a block of zeroes with the [allocator], returning a [Slice] of length [len].
 */
fun Scope.alloc(allocator: Allocator, len: Parameter): Slice {
    runFun0(allocator.position, len) {
        allocNoReturn()
    }
    val slice = Slice(copy(allocator.position), copy(len))
    set(allocator.position) to add(allocator.position, len)
    return slice
}
