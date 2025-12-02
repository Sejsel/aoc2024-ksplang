package cz.sejsel.ksplang.std.auto

import cz.sejsel.ksplang.dsl.auto.Constant
import cz.sejsel.ksplang.dsl.auto.MutableVariable
import cz.sejsel.ksplang.dsl.auto.Parameter
import cz.sejsel.ksplang.dsl.auto.Scope
import cz.sejsel.ksplang.dsl.auto.Variable
import cz.sejsel.ksplang.dsl.auto.runFun0
import cz.sejsel.ksplang.dsl.auto.set
import cz.sejsel.ksplang.std.*

/**
 * Keeps track of the position of the allocator. Keep in mind that you cannot have multiple allocators,
 * or the one earlier in the stack will break the position of the later one.
 * Additionally, any additions/removal from the stack below the block managed by this allocator
 * will also break the position.
 *
 * See also [alloc].
 */
data class Allocator(
    val position: MutableVariable
)

/**
 * Allocates a block of zeroes with the [allocator], returning a [Slice] of length [len].
 */
fun Scope.alloc(allocator: Allocator, len: Parameter): Slice {
    when (len) {
        is Constant -> {
            runFun0(allocator.position) {
                allocNoReturnConstLen(len.value)
            }
        }
        is Variable -> {
            runFun0(allocator.position, len) {
                allocNoReturn()
            }
        }
    }
    val slice = Slice(copy(allocator.position), copy(len))
    set(allocator.position) to add(allocator.position, len)
    return slice
}
