package cz.sejsel.ksplang.interpreter

import arrow.core.Either
import arrow.core.raise.either
import com.google.common.math.LongMath
import java.math.BigInteger
import java.util.zip.GZIPInputStream
import kotlin.math.abs


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

    data class JavaOnlyHasIntIndices(val size: Long) :
        OperationError {
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

object PiDigits {
    val digits: List<Long> =
        GZIPInputStream(this::class.java.getResourceAsStream("/pi-10million.txt.gz")).bufferedReader()
            .readText()
            .filter { it.isDigit() }
            .map { it.digitToInt().toLong() }
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

    fun peek_n(n: Int): Either<OperationError, Long> {
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
                    raise(OperationError.JavaOnlyHasIntIndices(n))
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
                stack.subList(len - n.toInt(), len).rotateRight(rotateBy.toInt())
            }

            Op.FF -> {
                val a = pop().bind()
                val b = pop().bind()
                if (a == 2L && b == 4L) {
                    push(b).bind()
                    push(a).bind()
                } else {
                    clear()
                    while (len() < maxStackSize) {
                        push(Long.MIN_VALUE).bind()
                    }
                }
            }

            Op.Swap -> {
                val i = pop().bind()
                val len = len()
                if (i < 0 || i >= len.toLong()) {
                    raise(OperationError.IndexOutOfRange(len, i))
                }
                stack.swap(i.toInt(), len - 1)
            }

            Op.KPi -> {
                val index = stack.withIndex().reversed().find { (i, value) -> value == i.toLong() }?.index
                if (index != null) {
                    val digit = piDigits.getOrNull(index)
                        ?: raise(OperationError.PiOutOfRange(piDigits.size, index))
                    stack[index] = digit
                } else {
                    val len = len()
                    if (len > piDigits.size) {
                        raise(OperationError.PiOutOfRange(piDigits.size, piDigits.size))
                    }
                    clear()
                    assert(len <= maxStackSize)
                    stack.addAll(piDigits.take(len))
                }
            }

            Op.Increment -> {
                val a = pop().bind()
                push(a.plus(1).also {
                    if (a > 0 && it < 0) raise(OperationError.IntegerOverflow)
                }).bind()
            }

            Op.Universal -> {
                val op = pop().bind()
                when (op) {
                    0L -> {
                        val a = pop().bind()
                        val b = pop().bind()
                        val sum = a.checkedAdd(b).bind()
                        push(sum).bind()
                    }

                    1L -> {
                        val a = pop().bind()
                        val b = pop().bind()
                        val diff = a.checkedSubtract(b).bind()
                        val abs = if (diff == Long.MIN_VALUE) raise(OperationError.IntegerOverflow) else abs(diff)
                        push(abs).bind()
                    }

                    2L -> {
                        val a = pop().bind()
                        val b = pop().bind()
                        val product = try {
                            LongMath.checkedMultiply(a, b)
                        } catch (e: ArithmeticException) {
                            raise(OperationError.IntegerOverflow)
                        }
                        push(product).bind()
                    }

                    3L -> {
                        val a = pop().bind()
                        val b = pop().bind()
                        if (b == 0L) raise(OperationError.DivisionByZero)
                        if (a == Long.MIN_VALUE && b == -1L) raise(OperationError.IntegerOverflow)
                        val rem = a % b
                        if (rem == 0L) {
                            push(a / b).bind()
                        } else {
                            push(rem).bind()
                        }
                    }

                    4L -> {
                        val a = pop().bind()
                        val absA = abs(a)
                        val factorial = when (absA) {
                            0L, 1L -> 1L
                            2L -> 2L
                            3L -> 6L
                            4L -> 24L
                            5L -> 120L
                            6L -> 720L
                            7L -> 5040L
                            8L -> 40320L
                            9L -> 362880L
                            10L -> 3628800L
                            11L -> 39916800L
                            12L -> 479001600L
                            13L -> 6227020800L
                            14L -> 87178291200L
                            15L -> 1307674368000L
                            16L -> 20922789888000L
                            17L -> 355687428096000L
                            18L -> 6402373705728000L
                            19L -> 121645100408832000L
                            20L -> 2432902008176640000L
                            else -> raise(OperationError.IntegerOverflow)
                        }
                        push(factorial).bind()
                    }

                    5L -> {
                        val a = pop().bind()
                        val result = when {
                            a < 0L -> -1L
                            a == 0L -> 0L
                            else -> 1L
                        }
                        push(result).bind()
                    }

                    else -> raise(OperationError.InvalidArgumentForUniversal(op))
                }
            }

            Op.DigitSum -> {
                val a = peek().bind()
                push(digitSum(a)).bind()
            }

            Op.Remainder -> {
                // Let's do big integers because java library space is not very good when it comes to checked math
                val a = pop().bind().toBigInteger()
                val b = pop().bind().toBigInteger()
                if (b == BigInteger.ZERO) raise(OperationError.DivisionByZero)

                val rem = a.rem(b.abs())
                push(rem.toCheckedLong().bind()).bind()
            }

            Op.Modulo -> {
                // There is no way to overflow with the result.
                val a = pop().bind()
                val b = pop().bind()
                if (b == 0L) raise(OperationError.DivisionByZero)

                if (b == Long.MIN_VALUE) {
                    if (a == Long.MIN_VALUE) {
                        push(0L).bind()
                    } else {
                        push(a).bind()
                    }
                }

                val mod = a.mod(abs(b))
                push(mod).bind()
            }

            Op.TetrationNumIters -> {
                val num = pop().bind()
                val iters = pop().bind()
                if (iters < 0L) raise(OperationError.NegativeIterations(iters))
                val result = when (iters) {
                    0L -> 1L
                    1L -> num
                    else -> {
                        var res = num
                        if (num == 0L || num == 1L) {
                            res = 1L
                        } else {
                            for (i in 1 until iters) {
                                if (res > Int.MAX_VALUE) raise(OperationError.IntegerOverflow)
                                try {
                                    res = LongMath.checkedPow(num, res.toInt())
                                } catch (e: ArithmeticException) {
                                    raise(OperationError.IntegerOverflow)
                                }
                            }
                        }
                        res
                    }
                }
                push(result).bind()
            }

            Op.TetrationItersNum -> {
                val iters = pop().bind()
                val num = pop().bind()
                if (iters < 0L) raise(OperationError.NegativeIterations(iters))
                val result = when (iters) {
                    0L -> 1L
                    1L -> num
                    else -> {
                        var res = num
                        if (num == 0L || num == 1L) {
                            res = 1L
                        } else {
                            for (i in 1 until iters) {
                                if (res > Int.MAX_VALUE) raise(OperationError.IntegerOverflow)
                                try {
                                    res = LongMath.checkedPow(num, res.toInt())
                                } catch (e: ArithmeticException) {
                                    raise(OperationError.IntegerOverflow)
                                }
                            }
                        }
                        res
                    }
                }
                push(result).bind()
            }

            Op.Median -> {
                val n = peek().bind()
                if (n <= 0L) raise(OperationError.NonpositiveLength(n))
                if (len() < n) raise(OperationError.NotEnoughElements(len(), n))
                val values = (0 until n).map { peek_n(it.toInt()).bind() }.sorted()
                val result = if (values.size % 2 == 0) {
                    val mid1 = values[values.size / 2 - 1]
                    val mid2 = values[values.size / 2]
                    ((mid1.toBigInteger() + mid2.toBigInteger()) / 2.toBigInteger()).toLong()
                } else {
                    values[values.size / 2]
                }
                push(result).bind()
            }

            Op.LenSum -> {
                val a = pop().bind()
                val b = pop().bind()
                push(lensum(a, b)).bind()
            }

            Op.Bitshift -> {
                val bits = pop().bind()
                val num = pop().bind()
                if (bits < 0L) raise(OperationError.NegativeBitCount(bits))
                val result = if (bits < Long.SIZE_BITS) num shl bits.toInt() else 0L
                push(result).bind()
            }

            Op.And -> {
                val a = pop().bind()
                val b = pop().bind()
                push(a and b).bind()
            }

            Op.Sum -> {
                val sum = stack.fold(BigInteger.ZERO) { acc, v -> acc + v.toBigInteger() }.toCheckedLong().bind()
                clear()
                push(sum).bind()
            }

            Op.Gcd2 -> {
                val a = pop().bind()
                val b = pop().bind()

                val result = gcd(a.toBigInteger(), b.toBigInteger()).abs()

                push(result.toCheckedLong().bind()).bind()
            }

            Op.GcdN -> {
                val n = pop().bind()
                if (n <= 0L) raise(OperationError.NonpositiveLength(n))
                if (len() < n) raise(OperationError.NotEnoughElements(len(), n))
                var result = pop().bind().toBigInteger().abs()
                for (i in 1 until n) {
                    val value = pop().bind().toBigInteger().abs()
                    result = gcd(result, value).abs()
                }
                push(result.toCheckedLong().bind()).bind()
            }

            Op.Qeq -> {
                val a = pop().bind()
                val b = pop().bind()
                val c = pop().bind()
                if (a == 0L && b == 0L) {
                    if (c == 0L) raise(OperationError.QeqZeroEqualsZero)
                    return@either Effect.None
                }
                if (a == 0L) {
                    val bBig = b.toBigInteger()
                    val cBig = c.toBigInteger()
                    if (-cBig % bBig == BigInteger.ZERO) {
                        val result = -cBig / bBig
                        push(result.toCheckedLong().bind()).bind()
                    }
                    return@either Effect.None
                }
                when (val res = solveQuadraticEquation(a, b, c)) {
                    is QuadraticEquationResult.None -> {}
                    is QuadraticEquationResult.One -> push(res.value.toCheckedLong().bind()).bind()
                    is QuadraticEquationResult.Two -> {
                        push(res.smaller.toCheckedLong().bind()).bind()
                        push(res.larger.toCheckedLong().bind()).bind()
                    }
                }
            }

            Op.Funkcia -> {
                val a = pop().bind()
                val b = pop().bind()
                push(funkcia(a, b)).bind()
            }

            Op.BulkXor -> {
                val n = pop().bind()
                if (len() < 2 * n) raise(OperationError.NotEnoughElements(len(), 2 * n))
                val xors = mutableListOf<Long>()
                for (i in 0 until n) {
                    val a = if (pop().bind() > 0L) 1L else 0L
                    val b = if (pop().bind() > 0L) 1L else 0L
                    xors.add(a xor b)
                }
                for (xor in xors.asReversed()) {
                    push(xor).bind()
                }
            }

            Op.BranchIfZero -> {
                val c = peek().bind()
                if (c != 0L) return@either Effect.None
                val i = peek_n(1).bind()
                if (i < 0L) raise(OperationError.NegativeInstructionIndex(i))
                return@either Effect.SetInstructionPointer(i)
            }

            Op.Call -> {
                val i = peek().bind()
                if (i < 0L) raise(OperationError.NegativeInstructionIndex(i))
                return@either Effect.SaveAndSetInstructionPointer(i)
            }

            Op.Goto -> {
                val i = peek().bind()
                if (i < 0L) raise(OperationError.NegativeInstructionIndex(i))
                return@either Effect.SetInstructionPointer(i)
            }

            Op.Jump -> {
                val i = peek().bind()
                return@either Effect.AddInstructionPointer(i + 1)
            }

            Op.Rev -> {
                val a = pop().bind()
                if (a < 0L) raise(OperationError.ArgumentAMustBeNonNegative(a))
                val b = pop().bind()
                if (b < 0L) raise(OperationError.ArgumentBMustBeNonNegative(b))
                val offset = if (a == 0L) {
                    b
                } else {
                    val c = pop().bind()
                    if (c < 0L) raise(OperationError.ArgumentCMustBeNonNegative(c))
                    when (val res = solveQuadraticEquation(a, b, c)) {
                        is QuadraticEquationResult.None -> b
                        is QuadraticEquationResult.One -> res.value
                        is QuadraticEquationResult.Two -> res.larger
                    }
                }

                return@either Effect.TemporaryReverse(offset.toLong())
            }

            Op.Sleep -> return@either Effect.Timeout
            Op.Deez -> {
                val n = pop().bind()
                if (n < 0L) raise(OperationError.NegativeLength(n))
                if (len() < n) raise(OperationError.NotEnoughElements(len(), n))
                val ops = mutableListOf<Op>()
                for (i in 0 until n) {
                    val id = pop().bind()
                    if (id < 0L) raise(OperationError.InvalidInstructionId(id))
                    val op = Op.byId(id.toInt()) ?: raise(OperationError.InvalidInstructionId(id))
                    ops.add(op)
                }
                return@either Effect.RunSubprogramAndAppendResult(ops)
            }
        }

        Effect.None
    }
}

data class VMOptions(
    val initialStack: List<Long>,
    val maxStackSize: Int = 2097152,
    val piDigits: List<Long>? = null,
    val maxOpCount: Long = Long.MAX_VALUE,
)


private sealed interface IPChange {
    data object Increment : IPChange
    data class Set(val ip: Int) : IPChange
    data class Add(val offset: Int) : IPChange
}

sealed interface RunError {
    data class OperationFailed(val error: OperationError) : RunError
    data object Timeout : RunError
    data object StackOverflow : RunError
    data object RunTooLong : RunError
}

data class RunResult(
    val stack: List<Long>,
    val instructionCounter: Long,
    val instructionPointer: Long,
    val reversed: Boolean,
)

fun run(ops: List<Op>, options: VMOptions): Either<RunError, RunResult> {
    val state = State(
        maxStackSize = options.maxStackSize.toLong(),
        piDigits = options.piDigits ?: PiDigits.digits
    )
    state.stack.clear()
    state.stack.addAll(options.initialStack)

    val ops = ops.toMutableList()

    val reverseUndoStack = mutableListOf<Pair<Int, Int>>()

    var ip = 0
    var opsRun = 0L
    var reversed = false

    while (true) {
        while (reverseUndoStack.isNotEmpty()) {
            val (reverseIp, returnIp) = reverseUndoStack.last()

            if (reverseIp == ip) {
                reversed = !reversed
                ip = returnIp
                state.stack.reverse()
                reverseUndoStack.removeLast()
            } else {
                break
            }
        }

        val op = ops.getOrNull(ip)

        if (op != null) {
            val (ipChange, instructionsRun) = state.apply(op).let {
                when (it) {
                    is Either.Right<Effect> -> {
                        return@let when (it.value) {
                            Effect.None -> IPChange.Increment to 1L
                            is Effect.SetInstructionPointer -> {
                                val index = (it.value as Effect.SetInstructionPointer).instructionIndex
                                check(index >= Int.MIN_VALUE && index <= Int.MAX_VALUE) {
                                    "Index out of puny java bounds"
                                }
                                IPChange.Set(index.toInt()) to 1L
                            }
                            is Effect.AddInstructionPointer -> {
                                val offset = (it.value as Effect.AddInstructionPointer).offset
                                check(offset >= Int.MIN_VALUE && offset <= Int.MAX_VALUE) {
                                    "Offset out of puny java bounds"
                                }
                                IPChange.Add(offset.toInt()) to 1L
                            }
                            is Effect.RunSubprogramAndAppendResult -> {
                                val remainingOps = options.maxOpCount - opsRun
                                val subprogramOps = (it.value as Effect.RunSubprogramAndAppendResult).ops
                                val result = run(
                                    subprogramOps,
                                    VMOptions(
                                        initialStack = emptyList(),
                                        maxStackSize = options.maxStackSize,
                                        piDigits = options.piDigits,
                                        maxOpCount = remainingOps,
                                    )
                                )

                                when (result) {
                                    is Either.Right<RunResult> -> {
                                        result.value.stack.map {
                                            val id = if (it < Int.MAX_VALUE && it > Int.MIN_VALUE) {
                                                it.toInt()
                                            } else {
                                                return Either.Left(RunError.OperationFailed(OperationError.InvalidInstructionId(it)))
                                            }
                                            val op = Op.byId(id)
                                            if (op != null) {
                                                ops.add(op)
                                            } else {
                                                return Either.Left(RunError.OperationFailed(OperationError.InvalidInstructionId(it)))
                                            }
                                        }

                                        IPChange.Increment to (1 + result.value.instructionCounter)
                                    }
                                    is Either.Left<RunError> -> return result
                                }
                            }
                            is Effect.SaveAndSetInstructionPointer -> {
                                val savedIp = ip.toLong() + (if (reversed) -1L else 1L)
                                val result = state.push(savedIp)
                                if (result.isLeft()) {
                                    return Either.Left(RunError.OperationFailed(OperationError.PushFailed))
                                }

                                val newIp = (it.value as Effect.SaveAndSetInstructionPointer).instructionIndex
                                if (newIp > Int.MAX_VALUE || newIp < Int.MIN_VALUE) {
                                    return Either.Left(RunError.OperationFailed(OperationError.JavaOnlyHasIntIndices(newIp)))
                                }
                                IPChange.Set(newIp.toInt()) to 1L
                            }
                            is Effect.TemporaryReverse -> {
                                val offset = (it.value as Effect.TemporaryReverse).offset
                                val returnIp = if (reversed) {
                                    ip - (offset + 1)
                                } else {
                                    ip + (offset + 1)
                                }

                                if (returnIp < 0 || returnIp >= ops.size) {
                                    return Either.Left(RunError.OperationFailed(OperationError.RevReturnInstructionOutOfRange(returnIp.toLong())))
                                }

                                reversed = !reversed
                                reverseUndoStack.add(ip to returnIp.toInt())
                                state.stack.reverse()
                                if (offset > Int.MAX_VALUE || offset < Int.MIN_VALUE) {
                                    return Either.Left(RunError.OperationFailed(OperationError.JavaOnlyHasIntIndices(offset)))
                                }

                                IPChange.Add(-offset.toInt()) to 1
                            }
                            Effect.Timeout -> return Either.Left(RunError.Timeout)
                        }
                    }
                    is Either.Left<OperationError> -> {
                        return Either.Left(RunError.OperationFailed(it.value))
                    }
                }
            }

            when (ipChange) {
                is IPChange.Add -> {
                    val newIp = if (reversed) {
                        ip - ipChange.offset
                    } else {
                        ip + ipChange.offset
                    }

                    if (newIp < 0 || newIp >= ops.size) {
                        return Either.Left(RunError.OperationFailed(OperationError.InstructionOutOfRange(newIp.toLong())))
                    }
                    ip = newIp
                }
                IPChange.Increment -> {
                    if (reversed) {
                        ip -= 1
                    } else {
                        ip += 1
                    }
                }
                is IPChange.Set -> {
                    if (ipChange.ip < 0 || ipChange.ip >= ops.size) {
                        return Either.Left(RunError.OperationFailed(OperationError.InstructionOutOfRange(ipChange.ip.toLong())))
                    }
                    ip = ipChange.ip
                }
            }

            opsRun += instructionsRun.toLong()
            if (opsRun >= options.maxOpCount) {
                return Either.Left(RunError.RunTooLong)
            }


            // Sanity check: should not happen
            if (state.stack.size > options.maxStackSize) {
                return Either.Left(RunError.StackOverflow)
            }
        } else {
            break
        }
    }

    return Either.Right(
        RunResult(
            stack = state.stack.toList(),
            instructionCounter = opsRun,
            instructionPointer = ip.toLong(),
            reversed = reversed
        )
    )
}

fun Long.checkedAdd(other: Long): Either<OperationError.IntegerOverflow, Long> {
    return try {
        Either.Right(LongMath.checkedAdd(this, other))
    } catch (e: ArithmeticException) {
        return Either.Left(OperationError.IntegerOverflow)
    }
}

fun Long.checkedSubtract(other: Long): Either<OperationError.IntegerOverflow, Long> {
    return try {
        Either.Right(LongMath.checkedSubtract(this, other))
    } catch (e: ArithmeticException) {
        return Either.Left(OperationError.IntegerOverflow)
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

fun <T> MutableList<T>.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun BigInteger.toCheckedLong(): Either<OperationError.IntegerOverflow, Long> {
    return if (this < Long.MIN_VALUE.toBigInteger() || this > Long.MAX_VALUE.toBigInteger()) {
        Either.Left(OperationError.IntegerOverflow)
    } else {
        Either.Right(this.toLong())
    }
}
