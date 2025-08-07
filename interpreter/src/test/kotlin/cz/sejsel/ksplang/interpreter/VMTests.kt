package cz.sejsel.ksplang.interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import arrow.core.Either

class VMTests : FunSpec({
    val PI_TEST_VALUES = listOf(
        3L, 1L, 4L, 1L, 5L, 9L, 2L, 6L, 5L, 3L, 5L, 8L, 9L, 7L, 9L, 3L, 2L, 3L, 8L, 4L, 6L, 2L, 6L, 4L, 3L, 3L, 8L, 3L, 2L, 7L, 9L, 5L,
        0L, 2L, 8L, 8L, 4L, 1L, 9L, 7L, 1L, 6L
    )

    fun run(initialStack: List<Long>, ops: List<Op>, maxStackSize: Int = Int.MAX_VALUE, piDigits: List<Long> = PI_TEST_VALUES): Either<RunError, RunResult> {
        return run(ops, VMOptions(
            maxStackSize = maxStackSize,
            piDigits = piDigits,
            initialStack = initialStack,
            maxOpCount = 1000000L,
        ))
    }

    fun runIsOk(initialStack: List<Long>, ops: List<Op>, maxStackSize: Int = Int.MAX_VALUE, piDigits: List<Long> = PI_TEST_VALUES): Boolean {
        return run(initialStack, ops, maxStackSize, piDigits).isRight()
    }

    fun runOp(initialStack: List<Long>, op: Op, maxStackSize: Int = Int.MAX_VALUE, piDigits: List<Long> = PI_TEST_VALUES): List<Long> {
        val result = run(initialStack, listOf(op), maxStackSize, piDigits)
        return result.fold(
            {
                throw IllegalStateException("Expected run to succeed, but got error: $it")
            },
            {
                return it.stack
            }
        )
    }

    fun runOps(initialStack: List<Long>, ops: List<Op>, maxStackSize: Int = Int.MAX_VALUE, piDigits: List<Long> = PI_TEST_VALUES): List<Long> {
        val result = run(initialStack, ops, maxStackSize, piDigits)
        return result.fold(
            {
                throw IllegalStateException("Expected run to succeed, but got error: $it")
            },
            {
                return it.stack
            }
        )
    }

    fun runOpIsOk(initialStack: List<Long>, op: Op, maxStackSize: Int = Int.MAX_VALUE, piDigits: List<Long> = PI_TEST_VALUES): Boolean {
        return runIsOk(initialStack, listOf(op), maxStackSize, piDigits)
    }

    context("empty") {
        test("empty program and stack") {
            val result = run(emptyList(), emptyList())
            result.isRight() shouldBe true
            result.fold(
                {
                    throw IllegalStateException("Expected run to succeed, but got error: $it")
                },
                {
                    it.stack shouldBe emptyList()
                }
            )
        }
    }

    context("praise") {
        test("not enough parameters") {
            runOpIsOk(emptyList(), Op.Praise) shouldBe false
        }
        test("negative parameter invalid") {
            runOpIsOk(listOf(-1L), Op.Praise) shouldBe false
        }
        test("correct output for i in 0..10") {
            val iLikeKsp = "Mám rád KSP".map { it.code.toLong() }
            for (i in 0..10) {
                runOp(listOf(i.toLong()), Op.Praise) shouldBe List(i) { iLikeKsp }.flatten()
            }
        }
        test("correct output with numbers") {
            runOp(listOf(1.toLong()), Op.Praise) shouldBe listOf(77L, 225L, 109L, 32L, 114L, 225L, 100L, 32L, 75L, 83L, 80L)
        }
        test("stack size limits") {
            fun runPraiseWithStackSize(initialStack: List<Long>, stackSize: Long): Either<OperationError, List<Long>> {
                val state = State(stackSize, PI_TEST_VALUES)
                state.stack.addAll(initialStack)
                val result = state.apply(Op.Praise)
                return when (result) {
                    is Either.Left -> Either.Left(result.value)
                    is Either.Right -> Either.Right(state.stack.toList())
                }
            }
            val iLikeKsp = "Mám rád KSP".map { it.code.toLong() }
            fun <T> List<T>.repeat(n: Long): List<T> = List(n.toInt()) { this }.flatten()
            runPraiseWithStackSize(listOf(1L), 11) shouldBe Either.Right(iLikeKsp)
            runPraiseWithStackSize(listOf(1L), 10).swap().getOrNull() shouldBe OperationError.PushFailed
            runPraiseWithStackSize(listOf(9091L), 100001) shouldBe Either.Right(iLikeKsp.repeat(9091L))
            runPraiseWithStackSize(listOf(9091L), 100000).swap().getOrNull() shouldBe OperationError.PushFailed
            runPraiseWithStackSize(listOf(Long.MAX_VALUE), 10).swap().getOrNull() shouldBe OperationError.PushFailed
        }
    }

    context("nop") {
        test("nop does nothing") {
            runOp(emptyList(), Op.Nop) shouldBe emptyList()
            runOp(listOf(1L, 2L, 3L), Op.Nop) shouldBe listOf(1L, 2L, 3L)
        }
    }

    context("pop") {
        test("pop fails on empty stack") {
            runOpIsOk(emptyList(), Op.Pop) shouldBe false
        }
        test("pop removes top element") {
            runOp(listOf(1L, 2L, 3L), Op.Pop) shouldBe listOf(1L, 2L)
        }
    }

    context("pop2") {
        test("pop2 fails on empty or one element stack") {
            runOpIsOk(emptyList(), Op.Pop2) shouldBe false
            runOpIsOk(listOf(1L), Op.Pop2) shouldBe false
        }
        test("pop2 on two elements") {
            runOp(listOf(1L, 2L), Op.Pop2) shouldBe listOf(2L)
        }
        test("pop2 on four elements") {
            runOp(listOf(1L, 2L, 3L, 4L), Op.Pop2) shouldBe listOf(1L, 2L, 4L)
        }
    }

    context("lswap") {
        test("lswap on empty and one element stack") {
            runOp(emptyList(), Op.LSwap) shouldBe emptyList()
            runOp(listOf(1L), Op.LSwap) shouldBe listOf(1L)
        }
        test("lswap swaps first and last") {
            runOp(listOf(1L, 2L, 3L, 4L), Op.LSwap) shouldBe listOf(4L, 2L, 3L, 1L)
        }
    }

    context("roll") {
        test("not enough parameters") {
            runOpIsOk(emptyList(), Op.Roll) shouldBe false
            runOpIsOk(listOf(0L), Op.Roll) shouldBe false
        }
        test("not enough elements") {
            runOpIsOk(listOf(1L, 1L), Op.Roll) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 1L, 4L), Op.Roll) shouldBe false
        }
        test("roll edge cases") {
            runOp(listOf(0L, 0L), Op.Roll) shouldBe emptyList()
            runOp(listOf(1L, 0L), Op.Roll) shouldBe emptyList()
        }
        test("roll normal cases") {
            runOp(listOf(1L, 2L, 3L, 4L, 1L, 4L), Op.Roll) shouldBe listOf(4L, 1L, 2L, 3L)
            runOp(listOf(1L, 2L, 3L, 4L, -1L, 4L), Op.Roll) shouldBe listOf(2L, 3L, 4L, 1L)
            runOp(listOf(0L, 1L, 2L, 3L, 4L, 2L, 4L), Op.Roll) shouldBe listOf(0L, 3L, 4L, 1L, 2L)
            runOp(listOf(1L, 2L, 3L, 4L, Long.MAX_VALUE, 4L), Op.Roll) shouldBe listOf(2L, 3L, 4L, 1L)
            runOp(listOf(1L, 2L, 3L, 4L, Long.MIN_VALUE, 4L), Op.Roll) shouldBe listOf(1L, 2L, 3L, 4L)
        }
    }

    context("ff") {
        test("not enough parameters") {
            runOpIsOk(emptyList(), Op.FF) shouldBe false
            runOpIsOk(listOf(1L), Op.FF) shouldBe false
        }
        test("ff fills with min") {
            fun runFF(initialStack: List<Long>, stackSize: Int): List<Long> {
                val state = State(stackSize.toLong(), PI_TEST_VALUES)
                state.stack.addAll(initialStack)
                state.apply(Op.FF)
                return state.stack
            }
            runFF(listOf(1L, 2L), 8) shouldBe List(8) { Long.MIN_VALUE }
            runFF(List(8) { 0L }, 8) shouldBe List(8) { Long.MIN_VALUE }
            runFF(List(8) { Long.MIN_VALUE }, 8) shouldBe List(8) { Long.MIN_VALUE }
            runFF(List(8) { Long.MAX_VALUE }, 8) shouldBe List(8) { Long.MIN_VALUE }
            runFF(listOf(1L, 2L, 3L, 4L, 5L), 8) shouldBe List(8) { Long.MIN_VALUE }
            runFF(listOf(4L, 2L), 8) shouldBe listOf(4L, 2L)
            runFF(listOf(1L, 2L, 3L, 4L, 2L), 8) shouldBe listOf(1L, 2L, 3L, 4L, 2L)
        }
    }

    context("swap") {
        test("swap fails on not enough elements or invalid index") {
            runOpIsOk(emptyList(), Op.Swap) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, -1L), Op.Swap) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, 4L), Op.Swap) shouldBe false
        }
        test("swap works for valid indices") {
            runOp(listOf(1L, 2L, 3L, 4L, 0L), Op.Swap) shouldBe listOf(4L, 2L, 3L, 1L)
            runOp(listOf(1L, 2L, 3L, 4L, 1L), Op.Swap) shouldBe listOf(1L, 4L, 3L, 2L)
            runOp(listOf(1L, 2L, 3L, 4L, 2L), Op.Swap) shouldBe listOf(1L, 2L, 4L, 3L)
            runOp(listOf(1L, 2L, 3L, 4L, 3L), Op.Swap) shouldBe listOf(1L, 2L, 3L, 4L)
            runOp(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 3L), Op.Swap) shouldBe listOf(1L, 2L, 3L, 8L, 5L, 6L, 7L, 4L)
        }
    }

    context("kpi") {
        test("kpi edge cases") {
            runOp(emptyList(), Op.KPi) shouldBe emptyList()
            runOp(listOf(0L), Op.KPi) shouldBe listOf(3L)
            runOp(listOf(1L, 2L, 3L, 4L, 5L), Op.KPi) shouldBe listOf(3L, 1L, 4L, 1L, 5L)
            runOp(listOf(2L, 2L, 2L, 2L, 2L), Op.KPi) shouldBe listOf(2L, 2L, 4L, 2L, 2L)
            runOp(listOf(0L, 1L, 2L, 3L, 4L), Op.KPi) shouldBe listOf(0L, 1L, 2L, 3L, 5L)
        }
    }

    context("increment") {
        test("increment fails on empty or max") {
            runOpIsOk(emptyList(), Op.Increment) shouldBe false
            runOpIsOk(listOf(Long.MAX_VALUE), Op.Increment) shouldBe false
        }
        test("increment works for -10..10") {
            for (i in -10L..10L) {
                runOp(listOf(i), Op.Increment) shouldBe listOf(i + 1)
                runOp(listOf(1L, 2L, 3L, 4L, i), Op.Increment) shouldBe listOf(1L, 2L, 3L, 4L, i + 1)
            }
        }
    }

    context("universal") {
        test("universal fails on not enough parameters and invalid args") {
            runOpIsOk(emptyList(), Op.Universal) shouldBe false
            runOpIsOk(listOf(0L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 0L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MAX_VALUE, 1L, 0L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, -1L, 0L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 1L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, 0L, 1L), Op.Universal) shouldBe false
            runOpIsOk(listOf(2L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 2L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, -1L, 2L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MAX_VALUE, 2L, 2L), Op.Universal) shouldBe false
            runOpIsOk(listOf(3L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 3L), Op.Universal) shouldBe false
            runOpIsOk(listOf(0L, 1L, 3L), Op.Universal) shouldBe false
            runOpIsOk(listOf(-1L, Long.MIN_VALUE, 3L), Op.Universal) shouldBe false
            runOpIsOk(listOf(4L), Op.Universal) shouldBe false
            runOpIsOk(listOf(21L, 4L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MAX_VALUE, 4L), Op.Universal) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, 4L), Op.Universal) shouldBe false
            runOpIsOk(listOf(5L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, 6L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, 7L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, -1L), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, Long.MIN_VALUE), Op.Universal) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 4L, Long.MAX_VALUE), Op.Universal) shouldBe false
        }
        test("universal addition") {
            runOp(listOf(1L, 2L, 3L, 8L, 4L, 0L), Op.Universal) shouldBe listOf(1L, 2L, 3L, 12L)
            runOp(listOf(8L, 4L, 0L), Op.Universal) shouldBe listOf(12L)
            runOp(listOf(8L, -9L, 0L), Op.Universal) shouldBe listOf(-1L)
        }
        test("universal subtraction abs") {
            runOp(listOf(1L, 2L, 3L, 8L, 4L, 1L), Op.Universal) shouldBe listOf(1L, 2L, 3L, 4L)
            runOp(listOf(8L, 4L, 1L), Op.Universal) shouldBe listOf(4L)
            runOp(listOf(4L, 8L, 1L), Op.Universal) shouldBe listOf(4L)
            runOp(listOf(-4L, -8L, 1L), Op.Universal) shouldBe listOf(4L)
            runOp(listOf(0L, 0L, 1L), Op.Universal) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, Long.MAX_VALUE, 1L), Op.Universal) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, 0L, 1L), Op.Universal) shouldBe listOf(Long.MAX_VALUE)
        }
        test("universal multiplication") {
            runOp(listOf(1L, 2L, 3L, 8L, 4L, 2L), Op.Universal) shouldBe listOf(1L, 2L, 3L, 32L)
            runOp(listOf(8L, 4L, 2L), Op.Universal) shouldBe listOf(32L)
            runOp(listOf(8L, -4L, 2L), Op.Universal) shouldBe listOf(-32L)
            runOp(listOf(-8L, 4L, 2L), Op.Universal) shouldBe listOf(-32L)
            runOp(listOf(-8L, -4L, 2L), Op.Universal) shouldBe listOf(32L)
            runOp(listOf(0L, 8L, 2L), Op.Universal) shouldBe listOf(0L)
            runOp(listOf(8L, 0L, 2L), Op.Universal) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, 0L, 2L), Op.Universal) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, -1L, 2L), Op.Universal) shouldBe listOf(-Long.MAX_VALUE)
            runOp(listOf(Long.MIN_VALUE, 0L, 2L), Op.Universal) shouldBe listOf(0L)
        }
        test("universal division/remainder") {
            runOp(listOf(1L, 2L, 3L, 4L, 8L, 3L), Op.Universal) shouldBe listOf(1L, 2L, 3L, 2L)
            runOp(listOf(4L, 8L, 3L), Op.Universal) shouldBe listOf(2L)
            runOp(listOf(8L, 4L, 3L), Op.Universal) shouldBe listOf(4L)
            runOp(listOf(3L, 8L, 3L), Op.Universal) shouldBe listOf(2L)
            runOp(listOf(3L, -8L, 3L), Op.Universal) shouldBe listOf(-2L)
            runOp(listOf(-3L, 8L, 3L), Op.Universal) shouldBe listOf(2L)
        }
        test("universal factorial") {
            runOp(listOf(1L, 2L, 3L, 6L, 4L), Op.Universal) shouldBe listOf(1L, 2L, 3L, 720L)
            runOp(listOf(0L, 4L), Op.Universal) shouldBe listOf(1L)
            runOp(listOf(1L, 4L), Op.Universal) shouldBe listOf(1L)
            runOp(listOf(-1L, 4L), Op.Universal) shouldBe listOf(1L)
            runOp(listOf(6L, 4L), Op.Universal) shouldBe listOf(720L)
            runOp(listOf(-6L, 4L), Op.Universal) shouldBe listOf(720L)
            runOp(listOf(20L, 4L), Op.Universal) shouldBe listOf(2432902008176640000L)
            runOp(listOf(-20L, 4L), Op.Universal) shouldBe listOf(2432902008176640000L)
        }
        test("universal sign") {
            runOp(listOf(1L, 2L, 3L, 1L, 5L), Op.Universal) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(0L, 5L), Op.Universal) shouldBe listOf(0L)
            runOp(listOf(1L, 5L), Op.Universal) shouldBe listOf(1L)
            runOp(listOf(-1L, 5L), Op.Universal) shouldBe listOf(-1L)
            runOp(listOf(-1234L, 5L), Op.Universal) shouldBe listOf(-1L)
            runOp(listOf(1234L, 5L), Op.Universal) shouldBe listOf(1L)
            runOp(listOf(Long.MAX_VALUE, 5L), Op.Universal) shouldBe listOf(1L)
            runOp(listOf(Long.MIN_VALUE, 5L), Op.Universal) shouldBe listOf(-1L)
        }
    }

    context("remainder") {
        test("remainder fails on not enough parameters or division by zero/overflow") {
            runOpIsOk(emptyList(), Op.Remainder) shouldBe false
            runOpIsOk(listOf(1L), Op.Remainder) shouldBe false
            runOpIsOk(listOf(0L, 1L), Op.Remainder) shouldBe false
            // TODO: This should not be an error, there is no overflow,
            //  it's just an implementation detail of Rust that their rem is bad.
            //  Should be changed in the Rust interpreter.
            //runOpIsOk(listOf(-1L, Long.MIN_VALUE), Op.Remainder) shouldBe false
        }
        test("remainder normal cases") {
            runOp(listOf(1L, 2L, 3L, 3L, 1L), Op.Remainder) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(1L, Long.MIN_VALUE), Op.Remainder) shouldBe listOf(0L)
            runOp(listOf(3L, 1L), Op.Remainder) shouldBe listOf(1L)
            runOp(listOf(-3L, 1L), Op.Remainder) shouldBe listOf(1L)
            runOp(listOf(3L, -1L), Op.Remainder) shouldBe listOf(-1L)
            runOp(listOf(-3L, -1L), Op.Remainder) shouldBe listOf(-1L)
        }
    }

    context("modulo") {
        test("modulo fails on not enough parameters or division by zero/overflow") {
            runOpIsOk(emptyList(), Op.Modulo) shouldBe false
            runOpIsOk(listOf(1L), Op.Modulo) shouldBe false
            runOpIsOk(listOf(0L, 1L), Op.Modulo) shouldBe false
            // TODO: This should not be an error, there is no overflow,
            //  it's just an implementation detail of Rust that their checked euclidean modulo is bad.
            //  Should be changed in the Rust interpreter.
            //runOpIsOk(listOf(-1L, Long.MIN_VALUE), Op.Modulo) shouldBe false
        }
        test("modulo normal cases") {
            runOp(listOf(1L, 2L, 3L, 3L, 1L), Op.Modulo) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(1L, Long.MIN_VALUE), Op.Modulo) shouldBe listOf(0L)
            runOp(listOf(3L, 1L), Op.Modulo) shouldBe listOf(1L)
            runOp(listOf(-3L, 1L), Op.Modulo) shouldBe listOf(1L)
            runOp(listOf(3L, -1L), Op.Modulo) shouldBe listOf(2L)
            runOp(listOf(-3L, -1L), Op.Modulo) shouldBe listOf(2L)
        }
    }

    context("tetrationNumIters") {
        test("tetrationNumIters fails on not enough parameters or negative/overflow") {
            runOpIsOk(emptyList(), Op.TetrationNumIters) shouldBe false
            runOpIsOk(listOf(1L), Op.TetrationNumIters) shouldBe false
            runOpIsOk(listOf(-1L, 1L), Op.TetrationNumIters) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, 1L), Op.TetrationNumIters) shouldBe false
            runOpIsOk(listOf(4L, 3L), Op.TetrationNumIters) shouldBe false
            runOpIsOk(listOf(Long.MAX_VALUE, Long.MAX_VALUE), Op.TetrationNumIters) shouldBe false
        }
        test("tetrationNumIters normal cases") {
            runOp(listOf(1L, 2L, 3L, 0L, 1L), Op.TetrationNumIters) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(0L, 1L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(1L, 0L), Op.TetrationNumIters) shouldBe listOf(0L)
            runOp(listOf(2L, 0L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(1000L, 0L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(1L, 1L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(2L, 1L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(3L, 1L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(1000L, 1L), Op.TetrationNumIters) shouldBe listOf(1L)
            runOp(listOf(2L, 2L), Op.TetrationNumIters) shouldBe listOf(4L)
            runOp(listOf(2L, 3L), Op.TetrationNumIters) shouldBe listOf(27L)
            runOp(listOf(2L, 4L), Op.TetrationNumIters) shouldBe listOf(256L)
            runOp(listOf(2L, 5L), Op.TetrationNumIters) shouldBe listOf(3125L)
            runOp(listOf(2L, 6L), Op.TetrationNumIters) shouldBe listOf(46656L)
            runOp(listOf(3L, 2L), Op.TetrationNumIters) shouldBe listOf(16L)
            runOp(listOf(3L, 3L), Op.TetrationNumIters) shouldBe listOf(7625597484987L)
            runOp(listOf(4L, 2L), Op.TetrationNumIters) shouldBe listOf(65536L)
        }
    }

    context("tetrationItersNum") {
        test("tetrationItersNum fails on not enough parameters or negative/overflow") {
            runOpIsOk(emptyList(), Op.TetrationItersNum) shouldBe false
            runOpIsOk(listOf(1L), Op.TetrationItersNum) shouldBe false
            runOpIsOk(listOf(1L, -1L), Op.TetrationItersNum) shouldBe false
            runOpIsOk(listOf(1L, Long.MIN_VALUE), Op.TetrationItersNum) shouldBe false
            runOpIsOk(listOf(3L, 4L), Op.TetrationItersNum) shouldBe false
            runOpIsOk(listOf(Long.MAX_VALUE, Long.MAX_VALUE), Op.TetrationItersNum) shouldBe false
        }
        test("tetrationItersNum normal cases") {
            runOp(listOf(1L, 2L, 3L, 1L, 0L), Op.TetrationItersNum) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(1L, 0L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(0L, 1L), Op.TetrationItersNum) shouldBe listOf(0L)
            runOp(listOf(0L, 2L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(0L, 1000L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(1L, 1L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(1L, 2L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(1L, 3L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(1L, 1000L), Op.TetrationItersNum) shouldBe listOf(1L)
            runOp(listOf(2L, 2L), Op.TetrationItersNum) shouldBe listOf(4L)
            runOp(listOf(3L, 2L), Op.TetrationItersNum) shouldBe listOf(27L)
            runOp(listOf(4L, 2L), Op.TetrationItersNum) shouldBe listOf(256L)
            runOp(listOf(5L, 2L), Op.TetrationItersNum) shouldBe listOf(3125L)
            runOp(listOf(6L, 2L), Op.TetrationItersNum) shouldBe listOf(46656L)
            runOp(listOf(2L, 3L), Op.TetrationItersNum) shouldBe listOf(16L)
            runOp(listOf(3L, 3L), Op.TetrationItersNum) shouldBe listOf(7625597484987L)
            runOp(listOf(2L, 4L), Op.TetrationItersNum) shouldBe listOf(65536L)
        }
    }

    context("median") {
        test("median fails on not enough parameters or not enough elements") {
            runOpIsOk(emptyList(), Op.Median) shouldBe false
            runOpIsOk(listOf(0L), Op.Median) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 5L), Op.Median) shouldBe false
        }
        test("median normal cases") {
            runOp(listOf(1L, 2L, 3L, 2L, 2L), Op.Median) shouldBe listOf(1L, 2L, 3L, 2L, 2L, 2L)
            runOp(listOf(1L), Op.Median) shouldBe listOf(1L, 1L)
            runOp(listOf(3L, 1L), Op.Median) shouldBe listOf(3L, 1L, 1L)
            runOp(listOf(2L, 2L), Op.Median) shouldBe listOf(2L, 2L, 2L)
            runOp(listOf(3L, 2L), Op.Median) shouldBe listOf(3L, 2L, 2L)
            runOp(listOf(4L, 2L), Op.Median) shouldBe listOf(4L, 2L, 3L)
            runOp(listOf(1L, 2L, 3L, 4L, 5L), Op.Median) shouldBe listOf(1L, 2L, 3L, 4L, 5L, 3L)
            runOp(listOf(4L, 3L, 2L, 1L, 5L), Op.Median) shouldBe listOf(4L, 3L, 2L, 1L, 5L, 3L)
            runOp(listOf(Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE, 4L), Op.Median) shouldBe listOf(Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE, 4L, Long.MAX_VALUE - 5)
        }
    }

    context("digitSum") {
        test("digitSum fails on not enough parameters") {
            runOpIsOk(emptyList(), Op.DigitSum) shouldBe false
        }
        test("digitSum normal cases") {
            runOp(listOf(0L), Op.DigitSum) shouldBe listOf(0L, 0L)
            runOp(listOf(1L), Op.DigitSum) shouldBe listOf(1L, 1L)
            runOp(listOf(-1L), Op.DigitSum) shouldBe listOf(-1L, 1L)
            runOp(listOf(10L), Op.DigitSum) shouldBe listOf(10L, 1L)
            runOp(listOf(-10L), Op.DigitSum) shouldBe listOf(-10L, 1L)
            runOp(listOf(333L), Op.DigitSum) shouldBe listOf(333L, 9L)
            runOp(listOf(-333L), Op.DigitSum) shouldBe listOf(-333L, 9L)
            runOp(listOf(Long.MIN_VALUE), Op.DigitSum) shouldBe listOf(Long.MIN_VALUE, 89L)
            runOp(listOf(Long.MAX_VALUE), Op.DigitSum) shouldBe listOf(Long.MAX_VALUE, 88L)
        }
    }

    context("lenSum") {
        test("lenSum fails on not enough parameters") {
            runOpIsOk(emptyList(), Op.LenSum) shouldBe false
            runOpIsOk(listOf(1L), Op.LenSum) shouldBe false
        }
        test("lenSum normal cases") {
            runOp(listOf(1L, 2L, 3L, 0L, 0L), Op.LenSum) shouldBe listOf(1L, 2L, 3L, 0L)
            runOp(listOf(0L, 0L), Op.LenSum) shouldBe listOf(0L)
            runOp(listOf(0L, 1L), Op.LenSum) shouldBe listOf(1L)
            runOp(listOf(9L, 9L), Op.LenSum) shouldBe listOf(2L)
            runOp(listOf(10L, 9L), Op.LenSum) shouldBe listOf(3L)
            runOp(listOf(10L, 10L), Op.LenSum) shouldBe listOf(4L)
            runOp(listOf(Long.MAX_VALUE, Long.MAX_VALUE), Op.LenSum) shouldBe listOf(19L + 19L)
            runOp(listOf(-1L, -1L), Op.LenSum) shouldBe listOf(2L)
            runOp(listOf(Long.MIN_VALUE, Long.MIN_VALUE), Op.LenSum) shouldBe listOf(19L + 19L)
        }
    }

    context("bitshift") {
        test("bitshift fails on not enough parameters or negative shift") {
            runOpIsOk(emptyList(), Op.Bitshift) shouldBe false
            runOpIsOk(listOf(1L), Op.Bitshift) shouldBe false
            runOpIsOk(listOf(1L, -1L), Op.Bitshift) shouldBe false
            runOpIsOk(listOf(64L, -1L), Op.Bitshift) shouldBe false
            runOpIsOk(listOf(64L, Long.MIN_VALUE), Op.Bitshift) shouldBe false
        }
        test("bitshift normal cases") {
            runOp(listOf(1L, 2L, 3L, 1L, 1L), Op.Bitshift) shouldBe listOf(1L, 2L, 3L, 2L)
            runOp(listOf(0L, 0L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(1L, 0L), Op.Bitshift) shouldBe listOf(1L)
            runOp(listOf(2L, 0L), Op.Bitshift) shouldBe listOf(2L)
            runOp(listOf(0L, 1L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(1L, 1L), Op.Bitshift) shouldBe listOf(2L)
            runOp(listOf(2L, 1L), Op.Bitshift) shouldBe listOf(4L)
            runOp(listOf(3L, 1L), Op.Bitshift) shouldBe listOf(6L)
            runOp(listOf(Long.MAX_VALUE, 1L), Op.Bitshift) shouldBe listOf(-2L)
            runOp(listOf(Long.MIN_VALUE, 1L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(-1L, 1L), Op.Bitshift) shouldBe listOf(-2L)
            runOp(listOf(0L, 2L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(1L, 2L), Op.Bitshift) shouldBe listOf(4L)
            runOp(listOf(Long.MIN_VALUE, 2L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(0L, 64L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(-1L, 64L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(1L, 64L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(Long.MIN_VALUE, 64L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, 64L), Op.Bitshift) shouldBe listOf(0L)
            runOp(listOf(1L, Long.MAX_VALUE), Op.Bitshift) shouldBe listOf(0L)
        }
    }

    context("and") {
        test("and fails on not enough parameters") {
            runOpIsOk(emptyList(), Op.And) shouldBe false
            runOpIsOk(listOf(1L), Op.And) shouldBe false
        }
        test("and normal cases") {
            runOp(listOf(1L, 2L, 3L, 5L, 3L), Op.And) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(0L, 0L), Op.And) shouldBe listOf(0L)
            runOp(listOf(0L, 1L), Op.And) shouldBe listOf(0L)
            runOp(listOf(1L, 1L), Op.And) shouldBe listOf(1L)
            runOp(listOf(5L, 3L), Op.And) shouldBe listOf(1L)
            runOp(listOf(Long.MAX_VALUE, Long.MAX_VALUE), Op.And) shouldBe listOf(Long.MAX_VALUE)
            runOp(listOf(Long.MIN_VALUE, Long.MIN_VALUE), Op.And) shouldBe listOf(Long.MIN_VALUE)
        }
    }

    context("sum") {
        test("sum normal cases and overflows") {
            runOp(emptyList(), Op.Sum) shouldBe listOf(0L)
            runOp(listOf(0L), Op.Sum) shouldBe listOf(0L)
            runOp(listOf(1L), Op.Sum) shouldBe listOf(1L)
            runOp(listOf(1L, -2L), Op.Sum) shouldBe listOf(-1L)
            runOp(listOf(1L, Long.MIN_VALUE), Op.Sum) shouldBe listOf(Long.MIN_VALUE + 1L)
            runOp(listOf(Long.MAX_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE), Op.Sum) shouldBe listOf(-2L)
            runOpIsOk(listOf(Long.MAX_VALUE, 1L), Op.Sum) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, -1L), Op.Sum) shouldBe false
        }
    }

    context("gcd2") {
        test("gcd2 fails on not enough parameters or overflow") {
            runOpIsOk(emptyList(), Op.Gcd2) shouldBe false
            runOpIsOk(listOf(1L), Op.Gcd2) shouldBe false
            runOpIsOk(listOf(0L, Long.MIN_VALUE), Op.Gcd2) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, Long.MIN_VALUE), Op.Gcd2) shouldBe false
        }
        test("gcd2 normal cases") {
            runOp(listOf(1L, 2L, 3L, 3L, 7L), Op.Gcd2) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(3L, 7L), Op.Gcd2) shouldBe listOf(1L)
            runOp(listOf(3L, 21L), Op.Gcd2) shouldBe listOf(3L)
            runOp(listOf(18L, 21L), Op.Gcd2) shouldBe listOf(3L)
            runOp(listOf(-18L, 21L), Op.Gcd2) shouldBe listOf(3L)
            runOp(listOf(18L, -21L), Op.Gcd2) shouldBe listOf(3L)
            runOp(listOf(-18L, -21L), Op.Gcd2) shouldBe listOf(3L)
            runOp(listOf(0L, 0L), Op.Gcd2) shouldBe listOf(0L)
            runOp(listOf(0L, Long.MAX_VALUE), Op.Gcd2) shouldBe listOf(Long.MAX_VALUE)
            runOp(listOf(Long.MIN_VALUE, 6L), Op.Gcd2) shouldBe listOf(2L)
        }
    }

    context("gcdn") {
        test("gcdn fails on not enough parameters or overflow") {
            runOpIsOk(emptyList(), Op.GcdN) shouldBe false
            runOpIsOk(listOf(0L), Op.GcdN) shouldBe false
            runOpIsOk(listOf(1L), Op.GcdN) shouldBe false
            runOpIsOk(listOf(0L, Long.MIN_VALUE, 2L), Op.GcdN) shouldBe false
            runOpIsOk(listOf(0L, 0L, Long.MIN_VALUE, 3L), Op.GcdN) shouldBe false
            runOpIsOk(listOf(0L, Long.MIN_VALUE, 0L, 3L), Op.GcdN) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, 1L), Op.GcdN) shouldBe false
        }
        test("gcdn normal cases") {
            runOp(listOf(1L, 2L, 3L, 1L, 1L, 2L), Op.GcdN) shouldBe listOf(1L, 2L, 3L, 1L)
            runOp(listOf(0L, 1L), Op.GcdN) shouldBe listOf(0L)
            runOp(listOf(5L, 1L), Op.GcdN) shouldBe listOf(5L)
            runOp(listOf(-5L, 1L), Op.GcdN) shouldBe listOf(5L)
            runOp(listOf(Long.MAX_VALUE, 1L), Op.GcdN) shouldBe listOf(Long.MAX_VALUE)
            runOp(listOf(3L, 7L, 2L), Op.GcdN) shouldBe listOf(1L)
            runOp(listOf(3L, 7L, 1L, 3L), Op.GcdN) shouldBe listOf(1L)
            runOp(listOf(21L, 21L, 21L, 3L), Op.GcdN) shouldBe listOf(21L)
            runOp(listOf(21L, 7L, 14L, 3L), Op.GcdN) shouldBe listOf(7L)
            runOp(listOf(21L, 54L, 6L, 3L), Op.GcdN) shouldBe listOf(3L)
            runOp(listOf(Long.MIN_VALUE, 6L, Long.MIN_VALUE, Long.MIN_VALUE, 4L), Op.GcdN) shouldBe listOf(2L)
        }
    }

    context("qeq") {
        test("qeq fails on not enough parameters or overflow") {
            runOpIsOk(emptyList(), Op.Qeq) shouldBe false
            runOpIsOk(listOf(1L), Op.Qeq) shouldBe false
            runOpIsOk(listOf(1L, 2L), Op.Qeq) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, Long.MIN_VALUE + 1L, 1L), Op.Qeq) shouldBe false
            runOpIsOk(listOf(Long.MIN_VALUE, 1L, 0L), Op.Qeq) shouldBe false
            runOpIsOk(listOf(0L, 0L, 0L), Op.Qeq) shouldBe false
        }
        test("qeq normal cases") {
            runOp(listOf(-40L, -3L, 1L), Op.Qeq) shouldBe listOf(-5L, 8L)
            runOp(listOf(9L, -6L, 1L), Op.Qeq) shouldBe listOf(3L)
            runOp(listOf(-7L, 3L, 1L), Op.Qeq) shouldBe emptyList()
            runOp(listOf(0L, Long.MIN_VALUE + 1L, 1L), Op.Qeq) shouldBe listOf(0L, Long.MAX_VALUE)
            runOp(listOf(0L, Long.MAX_VALUE, 1L), Op.Qeq) shouldBe listOf(Long.MIN_VALUE + 1L, 0L)
            runOp(listOf(4L, 2L, 0L), Op.Qeq) shouldBe listOf(-2L)
            runOp(listOf(3L, 2L, 0L), Op.Qeq) shouldBe emptyList()
            runOp(listOf(4L, 0L, 0L), Op.Qeq) shouldBe emptyList()
            runOp(listOf(Long.MIN_VALUE, Long.MIN_VALUE, 0L), Op.Qeq) shouldBe listOf(-1L)
            runOp(listOf(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE), Op.Qeq) shouldBe emptyList()
            runOp(listOf(Long.MIN_VALUE, 0L, Long.MIN_VALUE), Op.Qeq) shouldBe emptyList()
            val multiplier = Long.MIN_VALUE / -2L
            runOp(listOf(-2L * multiplier, -1L * multiplier, 1L * multiplier), Op.Qeq) shouldBe listOf(-1L, 2L)
            runOp(listOf(Long.MIN_VALUE, -1L, Long.MAX_VALUE), Op.Qeq) shouldBe listOf(-1L)
        }
    }

    context("funkcia") {
        test("funkcia fails on not enough parameters") {
            runOpIsOk(emptyList(), Op.Funkcia) shouldBe false
            runOpIsOk(listOf(1L), Op.Funkcia) shouldBe false
        }
        test("funkcia normal cases") {
            runOp(listOf(1L, 2L, 3L, 100L, 54L), Op.Funkcia) shouldBe listOf(1L, 2L, 3L, 675L)
            runOp(listOf(100L, 54L), Op.Funkcia) shouldBe listOf(675L)
            runOp(listOf(54L, 100L), Op.Funkcia) shouldBe listOf(675L)
            val MOD = 1_000_000_007L
            runOp(listOf(-1L, -1L), Op.Funkcia) shouldBe listOf(0L)
            runOp(listOf(1L, 0L), Op.Funkcia) shouldBe listOf(0L)
            runOp(listOf(1L, 1L), Op.Funkcia) shouldBe listOf(0L)
            runOp(listOf(1L, 2L), Op.Funkcia) shouldBe listOf(2L)
            runOp(listOf(2L, 2L), Op.Funkcia) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, 0L), Op.Funkcia) shouldBe listOf(Long.MAX_VALUE % MOD)
            runOp(listOf(0L, Long.MAX_VALUE), Op.Funkcia) shouldBe listOf(Long.MAX_VALUE % MOD)
            runOp(listOf(0L, MOD), Op.Funkcia) shouldBe listOf(0L)
            runOp(listOf(0L, MOD - 1L), Op.Funkcia) shouldBe listOf(MOD - 1L)
        }
    }

    context("bulkxor") {
        test("bulkxor fails on not enough parameters or not enough values") {
            runOpIsOk(emptyList(), Op.BulkXor) shouldBe false
            runOpIsOk(listOf(1L, 1L), Op.BulkXor) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L, 2L), Op.BulkXor) shouldBe false
        }
        test("bulkxor normal cases") {
            runOp(listOf(1L, 2L, 3L, 1L, 1L, 1L), Op.BulkXor) shouldBe listOf(1L, 2L, 3L, 0L)
            runOp(listOf(1L, 1L, 1L), Op.BulkXor) shouldBe listOf(0L)
            runOp(listOf(0L, 1L, 1L), Op.BulkXor) shouldBe listOf(1L)
            runOp(listOf(1L, 0L, 1L), Op.BulkXor) shouldBe listOf(1L)
            runOp(listOf(0L, 0L, 1L), Op.BulkXor) shouldBe listOf(0L)
            runOp(listOf(-1L, -1L, 1L), Op.BulkXor) shouldBe listOf(0L)
            runOp(listOf(-1L, 0L, 1L), Op.BulkXor) shouldBe listOf(0L)
            runOp(listOf(Long.MIN_VALUE, 0L, 1L), Op.BulkXor) shouldBe listOf(0L)
            runOp(listOf(Long.MAX_VALUE, 0L, 1L), Op.BulkXor) shouldBe listOf(1L)
            runOp(listOf(Long.MAX_VALUE, Long.MIN_VALUE, 1L), Op.BulkXor) shouldBe listOf(1L)
            runOp(listOf(0L, 0L, 1L, 0L, 2L), Op.BulkXor) shouldBe listOf(0L, 1L)
            runOp(listOf(1L, 0L, 1L, 0L, 2L), Op.BulkXor) shouldBe listOf(1L, 1L)
            runOp(listOf(1L, 0L, 0L, 0L, 2L), Op.BulkXor) shouldBe listOf(1L, 0L)
            runOp(listOf(0L, 0L, 0L, 0L, 2L), Op.BulkXor) shouldBe listOf(0L, 0L)
        }
    }

    context("branchifzero") {
        test("branchifzero fails on not enough parameters or invalid index") {
            runOpIsOk(emptyList(), Op.BranchIfZero) shouldBe false
            runOpIsOk(listOf(0L), Op.BranchIfZero) shouldBe false
            runIsOk(listOf(5L, 0L), listOf(Op.BranchIfZero, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
            runIsOk(listOf(-1L, 0L), listOf(Op.BranchIfZero, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
        }
        test("branchifzero normal cases") {
            runOps(listOf(4L, 0L), listOf(Op.BranchIfZero, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe listOf(4L)
            runOps(listOf(3L, 0L), listOf(Op.BranchIfZero, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe emptyList()
            runOps(listOf(1L, 2L, 3L, 4L), listOf(Op.BranchIfZero, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe emptyList()
        }
    }

    context("call") {
        test("call fails on not enough parameters or invalid index") {
            runOpIsOk(emptyList(), Op.Call) shouldBe false
            runIsOk(listOf(5L), listOf(Op.Call, Op.Nop, Op.Nop, Op.Nop, Op.Nop)) shouldBe false
            runIsOk(listOf(-1L), listOf(Op.Call, Op.Nop, Op.Nop, Op.Nop, Op.Nop)) shouldBe false
        }
        test("call normal cases") {
            runOps(listOf(1L, 2L, 3L, 4L), listOf(Op.Call, Op.Nop, Op.Nop, Op.Nop, Op.Nop)) shouldBe listOf(1L, 2L, 3L, 4L, 1L)
            runOps(listOf(1L, 2L, 3L), listOf(Op.Nop, Op.Call, Op.Nop, Op.Nop, Op.Nop)) shouldBe listOf(1L, 2L, 3L, 2L)
            runOps(listOf(1L, 2L, 3L), listOf(Op.Nop, Op.Nop, Op.Nop, Op.Nop, Op.Call, Op.Nop)) shouldBe listOf(1L, 2L, 3L, 5L, 5L)
        }
    }

    context("goto") {
        test("goto fails on not enough parameters or invalid index") {
            runOpIsOk(emptyList(), Op.Goto) shouldBe false
            runIsOk(listOf(5L), listOf(Op.Goto, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
            runIsOk(listOf(-1L), listOf(Op.Goto, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
        }
        test("goto normal cases") {
            runOps(listOf(1L, 2L, 3L, 4L), listOf(Op.Goto, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe listOf(1L, 2L, 3L)
            runOps(listOf(1L, 2L, 3L), listOf(Op.Goto, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe listOf(1L)
        }
    }

    context("jump") {
        test("jump fails on not enough parameters or invalid index") {
            runOpIsOk(emptyList(), Op.Jump) shouldBe false
            runIsOk(listOf(5L), listOf(Op.Jump, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
            runIsOk(listOf(4L), listOf(Op.Jump, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
            runIsOk(listOf(-2L), listOf(Op.Jump, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe false
        }
        test("jump normal cases") {
            runOps(listOf(3L, 0L, -3L, 0L, 0L), listOf(Op.Pop, Op.Pop, Op.Jump, Op.Pop, Op.Pop, Op.Pop, Op.Pop)) shouldBe emptyList()
        }
    }

    context("rev") {
        test("rev fails on not enough parameters or invalid arguments") {
            runOpIsOk(emptyList(), Op.Rev) shouldBe false
            runOpIsOk(listOf(1L), Op.Rev) shouldBe false
            runOpIsOk(listOf(0L, 1L), Op.Rev) shouldBe false
            runIsOk(listOf(-1L, 0L), listOf(Op.Nop, Op.Nop, Op.Rev, Op.Nop, Op.Nop)) shouldBe false
            runOpIsOk(listOf(0L, 0L), Op.Rev) shouldBe false
            runOpIsOk(listOf(1L, 0L), Op.Rev) shouldBe false
        }
        test("rev normal cases") {
            runOps(listOf(1L, 2L, 3L, 4L, 0L, 0L), listOf(Op.Rev, Op.Nop)) shouldBe listOf(1L, 2L, 3L, 4L)
            runOps(listOf(1L, 2L, 3L, 4L, 0L, 2L, 1L), listOf(Op.Rev, Op.Nop)) shouldBe listOf(1L, 2L, 3L, 4L)
            runOps(listOf(1L, 2L, 3L, 4L, 2L, 0L), listOf(Op.Rev, Op.Pop, Op.Pop, Op.Nop)) shouldBe listOf(3L, 4L)
            runOps(listOf(0L, 1L, 2L, 3L, 2L, 0L), listOf(Op.Nop, Op.Rev, Op.Goto, Op.Nop, Op.Nop)) shouldBe listOf(3L, 2L, 1L, 0L)
            runOps(listOf(0L, 1L, 2L, 3L, 1L, 0L), listOf(Op.Nop, Op.Rev, Op.Goto, Op.Nop)) shouldBe listOf(3L, 2L, 1L, 0L)
            runOps(listOf(-1L, 0L, 2L, -1L, 10L, 11L, 12L, -1L, -1L, 5L, 0L), listOf(Op.Rev, Op.Pop, Op.Pop, Op.Pop, Op.Rev, Op.Pop, Op.Nop)) shouldBe listOf(10L, 11L, 12L)
            runOps(listOf(-1L, 0L, 2L, 10L, 11L, 12L, -1L, -1L, 4L, 0L, -1L), listOf(Op.Pop, Op.Rev, Op.Pop, Op.Pop, Op.Rev, Op.Pop, Op.Nop)) shouldBe listOf(10L, 11L, 12L)
        }
    }

    context("sleep") {
        test("sleep fails on not enough parameters") {
            runOpIsOk(emptyList(), Op.Sleep) shouldBe false
            runOpIsOk(listOf(1L, 2L, 3L), Op.Sleep) shouldBe false
        }
    }

    context("deez") {
        test("deez fails on not enough parameters") {
            runOpIsOk(emptyList(), Op.Deez) shouldBe false
        }
        test("deez normal case") {
            runOp(listOf(1L, 2L, 3L, 9L, 20L, 2L), Op.Deez) shouldBe listOf(1L, 2L)
        }
    }
})
