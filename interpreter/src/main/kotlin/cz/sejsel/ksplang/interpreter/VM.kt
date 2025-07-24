package cz.sejsel.ksplang.interpreter

import arrow.core.Either
import arrow.core.raise.either


sealed interface OperationError {
    val message: String

    data object PopFailed : OperationError {
        override val message: String = "Removing from an empty stack"
    }
    data object PushFailed : OperationError {
        override val message: String = "Adding to a full stack"
    }
    data class PeekFailed(val index: Long) : OperationError {
        override val message: String = "Reading from a position out of bounds (index $index)"
    }
    data object IntegerOverflow : OperationError {
        override val message: String = "Integer overflow"
    }
    data object DivisionByZero : OperationError {
        override val message: String = "Division by zero"
    }
    data class InvalidArgumentForUniversal(val argument: Long) : OperationError {
        override val message: String = "Invalid argument for u: $argument"
    }
    data class NegativeLength(val value: Long) : OperationError {
        override val message: String = "Negative value used as a length: $value"
    }
    data class NotEnoughElements(val stackLen: Int, val required: Long) : OperationError {
        override val message: String = "Not enough elements on the stack: $stackLen elements, $required required"
    }
    data class IndexOutOfRange(val stackLen: Int, val index: Long) : OperationError {
        override val message: String = "Index out of range: index $index, stack length $stackLen"
    }
    data class PiOutOfRange(val digitsAvailable: Int, val index: Int) : OperationError {
        override val message: String = "Sorry, not enough pi digits available: available $digitsAvailable, index $index"
    }
    data class NegativeIterations(val iterations: Long) : OperationError {
        override val message: String = "Negative iteration count: $iterations"
    }
    data class NegativeBitCount(val bits: Long) : OperationError {
        override val message: String = "Negative bit count: $bits"
    }
    data class NonpositiveLength(val value: Long) : OperationError {
        override val message: String = "Nonpositive value used as a length: $value"
    }
    data class NegativeInstructionIndex(val index: Long) : OperationError {
        override val message: String = "Negative value used as instruction index: $index"
    }
    data class InstructionOutOfRange(val index: Long) : OperationError {
        override val message: String = "Value used as instruction index is too large: $index"
    }
    data class RevReturnInstructionOutOfRange(val index: Long) : OperationError {
        override val message: String = "Return instruction index of a `rev` instruction is too large: $index"
    }
    data class InvalidInstructionId(val id: Long) : OperationError {
        override val message: String = "Value is not a valid instruction id: $id"
    }
    data class NegativePraiseCount(val praises: Long) : OperationError {
        override val message: String = "Negative praise count: $praises. You should praise KSP more!"
    }
    data object QeqZeroEqualsZero : OperationError {
        override val message: String = "Qeq tried to solve 0 = 0. Too many solutions, giving up."
    }
    data class ArgumentAMustBeNonNegative(val value: Long) : OperationError {
        override val message: String = "Argument `a` must be non-negative, it was $value"
    }
    data class ArgumentBMustBeNonNegative(val value: Long) : OperationError {
        override val message: String = "Argument `b` must be non-negative, it was $value"
    }
    data class ArgumentCMustBeNonNegative(val value: Long) : OperationError {
        override val message: String = "Argument `c` must be non-negative, it was $value"
    }
    data class SizeIsTooLargeForJavaIntegersAndImplementingAWorkaroundWouldBeTooPerformanceCostly(val size: Long) : OperationError {
        override val message: String = "Size is too large for Java integers: $size"
    }
}

sealed interface Effect {
    data object None : Effect
    data class SetInstructionPointer(val instructionIndex: Long) : Effect
    data class SaveAndSetInstructionPointer(val instructionIndex: Long) : Effect
    data class AddInstructionPointer(val offset: Long) : Effect
    data object Timeout : Effect
    data class RunSubprogramAndAppendResult(val ops: List<Op>) : Effect
    data class TemporaryReverse(val offset: Long) : Effect
}

class State(
    val maxStackSize: Long,
    val piDigits: List<Long>
) {
    val stack: MutableList<Long> = mutableListOf()

    fun clear() {
        stack.clear()
    }

    fun pop(): Either<OperationError, Long> {
        return if (stack.isEmpty()) {
            Either.Left(OperationError.PopFailed)
        } else {
            Either.Right(stack.removeLast())
        }
    }

    fun push(value: Long): Either<OperationError, Unit> {
        return if (stack.size >= maxStackSize) {
            Either.Left(OperationError.PushFailed)
        } else {
            stack.add(value)
            Either.Right(Unit)
        }
    }

    fun len(): Int = stack.size

    fun peek(): Either<OperationError, Long> {
        return if (stack.isEmpty()) {
            Either.Left(OperationError.PopFailed)
        } else {
            Either.Right(stack.last())
        }
    }

    fun peek_n(n: Int) : Either<OperationError, Long> {
        val index = stack.size - (1 + n)
        if (index < 0 || index >= stack.size) {
            return Either.Left(OperationError.PeekFailed(index.toLong()))
        }
        return Either.Right(stack[index])
    }

    fun apply(op: Op): Either<OperationError, Effect> = either {
        when (op) {
            Op.Nop -> {}
            Op.Praise -> {
                val n = pop().bind()
                if (n < 0) {
                    raise(OperationError.NegativePraiseCount(n))
                }

                if (len() + 11 * n > maxStackSize) {
                    raise(OperationError.PushFailed)
                }
                if (n > Int.MAX_VALUE) {
                    raise(OperationError.SizeIsTooLargeForJavaIntegersAndImplementingAWorkaroundWouldBeTooPerformanceCostly(n))
                }


                repeat(n.toInt()) {
                    push(77).bind()
                    push(225).bind()
                    push(109).bind()
                    push(32).bind()
                    push(114).bind()
                    push(225).bind()
                    push(100).bind()
                    push(32).bind()
                    push(75).bind()
                    push(83).bind()
                    push(80).bind()
                }
            }
            Op.Pop -> {
                pop().bind()
            }
            Op.Pop2 -> {
                val top = pop().bind()
                pop().bind()
                push(top).bind()
            }
            Op.Max -> {
                val a = pop().bind()
                val b = pop().bind()
                push(maxOf(a, b)).bind()
            }
            Op.LSwap -> {
                val len = len()
                if (len > 1) {
                    val bottom = stack[0]
                    stack[0] = stack.last()
                    stack[len - 1] = bottom
                }
            }
            Op.Roll -> {
                val n = pop().bind()
                val x = pop().bind()

                if (n < 0) {
                    raise(OperationError.NegativeLength(n))
                }

                if (n > len()) {
                    raise(OperationError.NotEnoughElements(len(), n))
                }

                if (n == 0L) {
                    return@either Effect.None
                }


                val rotateBy = x.mod(n)
                val len = len()
                stack.subList(len - n.toInt(), len).rotateRight(rotateBy.toInt()) }
            Op.FF -> TODO()
            Op.Swap -> TODO()
            Op.KPi -> TODO()
            Op.Increment -> TODO()
            Op.Universal -> TODO()
            Op.Remainder -> TODO()
            Op.Modulo -> TODO()
            Op.TetrationNumIters -> TODO()
            Op.TetrationItersNum -> TODO()
            Op.Median -> TODO()
            Op.DigitSum -> TODO()
            Op.LenSum -> TODO()
            Op.Bitshift -> TODO()
            Op.And -> TODO()
            Op.Sum -> TODO()
            Op.Gcd2 -> TODO()
            Op.GcdN -> TODO()
            Op.Qeq -> TODO()
            Op.Funkcia -> TODO()
            Op.BulkXor -> TODO()
            Op.BranchIfZero -> TODO()
            Op.Call -> TODO()
            Op.Goto -> TODO()
            Op.Jump -> TODO()
            Op.Rev -> TODO()
            Op.Sleep -> TODO()
            Op.Deez -> TODO()
        }

        Effect.None
    }
}

fun <T> MutableList<T>.rotateRight(n: Int) {
    if (this.isEmpty()) return
    val k = ((n % this.size) + this.size) % this.size
    if (k == 0) return
    this.reverse()
    this.subList(0, k).reverse()
    this.subList(k, this.size).reverse()
}
