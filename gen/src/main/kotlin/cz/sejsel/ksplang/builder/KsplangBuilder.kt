package cz.sejsel.ksplang.builder

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.Instruction
import cz.sejsel.ksplang.dsl.core.SimpleFunction
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.IfZero
import cz.sejsel.ksplang.dsl.core.*
import cz.sejsel.ksplang.std.PaddingFailureException
import cz.sejsel.ksplang.std.roll
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.pushPaddedTo

/*
The original Python version, we are translating this to Kotlin.

@dataclass
class RegisteredFunction:
    function: SimpleFunction
    n_params: int
    n_out: int
    index: int | None

    def name(self) -> str:
        return self.function.name

    def to_callable(self) -> SimpleFunction:
        from ksplang_gen.stdlib.stack import roll

        return SimpleFunction(
            f"callable_{self.name}",
            [
                # [...params] $index IP
                pop2,
                # [...params] IP
                roll(1 + self.n_params, 1),
                # IP [...params]
                self.function,
                # IP [...result]
                roll(1 + self.n_out, self.n_out),
                # [...result] IP
                goto,
            ])

    def to_call(self) -> SimpleFunction:
        from ksplang_gen.stdlib.int import push

        assert self.index is not None
        return SimpleFunction(
            f"call_{self.name}",
            [
                push(self.index),
                call,
                pop,
            ])


@dataclass(eq=False)
class PreparedPush:
    index: int
    setter: Callable[['PreparedPush', int, int], None]
    padding: int
    invalidated: bool = False

    def set(self, n: int):
        if self.invalidated:
            raise ValueError("This push was already set")
        self.setter(self, n, self.padding)
        self.invalidated = True

    def set_for_jump(self, n: int):
        """Convenience method for when this push is used right before a jump."""
        self.set(n - self.index_after() - 1)

    def isspace(self) -> bool:
        return False

    def index_end(self) -> int:
        return self.index + self.padding - 1

    def index_after(self) -> int:
        return self.index + self.padding


class Builder:
    """A class for building ksplang programs. Use the methods to build up a program,
    then call .build() to get the program as a list of instructions."""

    def __init__(self):
        self.blocks = []
        self.registered_functions: list[RegisteredFunction] = []
        self.early_exit = None
        self.enable_push_optimizations = True

    def append(self, block: Union[ProgramBlock, list[ProgramBlock]]):
        """Append a block or a list of blocks to the program."""
        if isinstance(block, list):
            self.blocks.extend(block)
        else:
            self.blocks.append(block)

    def register_function(self, function: SimpleFunction, n_params: int, n_out: int):
        """Register a function for insertion at the start of the program.
        This function will be called using call instead of being inserted.
        Function identification is done by name."""
        assert not any(f.name == function.name for f in self.registered_functions)
        self.registered_functions.append(RegisteredFunction(function, n_params, n_out, None))

    def add_early_exit(self, blocks: list[ProgramBlock]):
        """
        Add an early exit with specified logic to the program. If the program ends normally,
        the blocks will not be executed. If EarlyExit is ran, the program will immediately
        jump to the end and execute blocks before finishing.

        Only one early exit may be set. If no EarlyExit is used in the program, no new instructions
        will be emitted.
        """
        if self.early_exit is not None:
            raise ValueError("Early exit already set")
        self.early_exit = blocks

    def build(self) -> str:
        """Build the program and return it as a list of instructions."""
        from ksplang_gen.stdlib.int import push, push_on, push_on_sgn, push_padded_to
        from ksplang_gen.stdlib.math import sgn, zero_not, zero_not_positive, dec
        from ksplang_gen.stdlib.stack import roll

        push_name_regex = re.compile(r"^push\(([-]?\d+)\)$")

        # We use a global address padding for simplification for now.
        for address_pad in itertools.count(6):
            try:
                index = 0
                program = []

                last_depth = 0
                last_simple_function: SimpleFunction | None = None
                early_exit_pushes: list[PreparedPush] = []

                prepared_pushes = []

                def backup_state():
                    backup_index = copy.deepcopy(index)
                    backup_program = copy.deepcopy(program)
                    backup_last_depth = copy.deepcopy(last_depth)
                    backup_last_simple_function = copy.deepcopy(last_simple_function)
                    backup_early_exit_pushes: list[PreparedPush] = copy.deepcopy(early_exit_pushes)
                    backup_prepared_pushes = copy.deepcopy(prepared_pushes)

                    return backup_index, backup_program, backup_last_depth, backup_last_simple_function, backup_early_exit_pushes, backup_prepared_pushes

                def restore_state(backup):
                    nonlocal index, program, last_depth, last_simple_function, early_exit_pushes, prepared_pushes
                    index, program, last_depth, last_simple_function, early_exit_pushes, prepared_pushes = backup

                def apply_prepared_push(push: PreparedPush, n: int, padding: int):
                    index = program.index(push)
                    padded_push = push_padded_to(n, padding)
                    assert isinstance(padded_push, SimpleFunction)
                    program[index:index + 1] = " ".join((instr.code for instr in padded_push.expand())) + "\n"

                def prepare_padded_push(padding: int | None = None) -> PreparedPush:
                    nonlocal index
                    if padding is None:
                        padding = address_pad
                    push = PreparedPush(index, apply_prepared_push, padding)
                    program.append(push)
                    index += padding
                    prepared_pushes.append(push)
                    return push

                def expand(block: ProgramBlock, is_last: bool = False, depth: int = 0, use_calls: bool = True):
                    nonlocal last_depth, last_simple_function, index, push_name_regex

                    # A shorthand to expand recursively with correct params
                    def e(block: ProgramBlock):
                        expand(block, is_last, depth=depth + 1, use_calls=use_calls)

                    if depth > last_depth:
                        while len(program) > 0 and program[-1].isspace():
                            program.pop()
                        if len(program) > 0 and program[-1] != "\n":
                            program.append("\n")
                        program.append(" " * depth)
                    last_depth = depth

                    if isinstance(block, Instruction):
                        last_simple_function = None
                        # Individual instructions
                        index += 1
                        program.append(block.code)
                        program.append(" ")
                    elif isinstance(block, SimpleFunction):
                        # Simple functions with a set length

                        # Special optimization for pushes.
                        # We can replace a push with a push_on if the top of the stack is known.
                        optimized = False
                        if self.enable_push_optimizations and last_simple_function is not None:
                            last_match = push_name_regex.match(last_simple_function.name)
                            this_match = push_name_regex.match(block.name)
                            if last_match and this_match:
                                last_n = int(last_match.group(1))
                                this_n = int(this_match.group(1))
                                # We must not optimize again in case push_on falls back onto push(n)
                                # That would result in an infinite loop.
                                last_simple_function = None
                                e(push_on(last_n, this_n))
                                optimized = True
                            elif this_match and last_simple_function == sgn:
                                this_n = int(this_match.group(1))
                                e(push_on_sgn(this_n, is_last))
                                last_simple_function = None
                                optimized = True

                        if not optimized:
                            try:
                                if not use_calls:
                                    raise StopIteration()

                                # A function can be registered, at the start of the program, and callable:
                                registered = next(f for f in self.registered_functions if
                                                  f.name() == block.name and f.index is not None)
                                e(registered.to_call())
                            except StopIteration:
                                # Or it may not be callable, then we just inline it:
                                for instruction in block.blocks:
                                    e(instruction)
                        last_simple_function = block
                    elif isinstance(block, ComplexFunction):
                        try:
                            if not use_calls:
                                raise StopIteration()

                            registered = next(f for f in self.registered_functions if f.name() == block.name)
                            if registered is not None:
                                raise NotImplementedError("Complex functions registration has not been tested.")
                        except StopIteration:
                            # Or it may not be callable, then we just inline it:
                            for child in block.blocks:
                                e(child)
                    elif isinstance(block, DoWhileNonZero):
                        if block.type == CheckedValueType.NEGATIVE:
                            raise NotImplementedError("Specialization not implemented, use Any or implement it")
                        if block.type == CheckedValueType.SGN:
                            raise NotImplementedError("Specialization not implemented, use Any or implement it")
                        e(CS)
                        e(CS)
                        redo_index = index
                        e(pop)
                        e(pop)
                        for b in block.blocks:
                            e(b)
                        match block.type:
                            case CheckedValueType.ANY:
                                e(zero_not)
                            case CheckedValueType.POSITIVE:
                                e(zero_not_positive)
                        e(push(redo_index))
                        e(roll(2, 1))
                        e(brz)
                        e(pop2)
                        e(pop)
                    elif isinstance(block, DoWhileZero):
                        e(CS)
                        e(CS)
                        redo_index = index
                        e(pop)
                        e(pop)
                        for block in block.blocks:
                            e(block)
                        e(push(redo_index))
                        e(roll(2, 1))
                        e(brz)
                        e(pop2)
                        e(pop)
                    elif isinstance(block, IfZero):
                        if block.otherwise is None:
                            # We can specialize
                            raise NotImplementedError()

                        then_push = prepare_padded_push()
                        e(roll(2, 1))
                        e(brz)
                        e(pop2)
                        otherwise_j_push = prepare_padded_push()
                        otherwise_j_index = index
                        e(j)
                        then_push.set(index)
                        e(pop2)
                        for b in block.then:
                            e(b)
                        end_j_push = prepare_padded_push()
                        end_j_index = index
                        e(j)
                        e(pop)
                        otherwise_j_push.set(index - otherwise_j_index - 2)
                        for b in block.otherwise:
                            e(b)
                        e(CS)
                        e(pop)
                        end_j_push.set(index - end_j_index - 2)
                    elif isinstance(block, DoNTimes):
                        e(DoWhileNonZero([
                            *block.blocks,
                            dec,
                            CS,
                        ], type=CheckedValueType.POSITIVE))
                        e(pop)
                    elif isinstance(block, EarlyExit):
                        if self.early_exit is None:
                            raise ValueError("No early exit set with .add_early_exit(), cannot use EarlyExit")
                        early_exit_pushes.append(prepare_padded_push())
                        e(goto)
                    else:
                        assert False

                callables = [(f, f.to_callable()) for f in self.registered_functions]

                # 16 is a really nice number to align to, as it can be created in very few instructions
                # call cost of num is len(short_pushes[num].split())
                # call cost 11: 16 24 27
                # call cost 12: 17 25 28 32 48 64 77 256 512 2048 65536 16777216 7625597484988
                # call cost 13: 15 18 20 26 29 33 49 65 71 78 96 112 128 160 225 257 384 513 1536 2049 3125 4096 4608
                # call cost 14: 14 19 21 30 34 39 42 50 66 72 79 97 109 113 129 154 161 168 175 192 218 226 258 320 385 450 514 768 896 1024 1537 2050 3072 3126 3200 4097 4352 4609
                if len(callables) > 0:
                    for first_fun_start in range(16, 2 ** 32):
                        backup = backup_state()
                        try:
                            # Initial jump past the callable functions

                            # If we can push the total skip in 15 instructions,
                            # we can align first function to index 16, which is really nice
                            # as that can be created in very few instructions
                            after_push = prepare_padded_push(first_fun_start - 1)
                            expand(j)
                            # TODO: We may be able to save a few instructions per call by padding to a good value
                            #       should be possible to math it out, we know how many calls we have as well
                            # TODO: We may also be able to save a bit by using goto instead of j there
                            # Callable functions
                            for f, c in callables:
                                # We use f.index to check if a function is callable within expansion,
                                # so we need to only set it after we are done with expanding this function
                                # or it will expand itself as a call to itself.
                                f_index = index
                                expand(c)
                                f.index = f_index
                            # Landing pop for the initial jump
                            after_push.set_for_jump(index)
                            expand(pop)
                            break
                        except PaddingFailureError:
                            for f, c in callables:
                                f.index = None
                            restore_state(backup)
                            pass

                for i, block in enumerate(self.blocks):
                    is_last = i == len(self.blocks) - 1
                    expand(block, is_last=is_last, depth=0)

                if len(early_exit_pushes) > 0:
                    assert self.early_exit is not None
                    # When exiting normally, we need to skip over this early exit logic
                    skip_push = prepare_padded_push()
                    expand(j)

                    # Landing pop for the early exit
                    for early_exit_push in early_exit_pushes:
                        early_exit_push.set(index)
                    expand(pop)
                    expand(ComplexFunction("_early_exit_logic", self.early_exit))
                    expand(CS)
                    skip_push.set_for_jump(index)
                    expand(pop, is_last=True)

                return "".join(program)
            except PaddingFailureError:
                pass


def evaluate(p: Union[ProgramBlock, list[ProgramBlock]]):
    b = Builder()
    b.append(p)
    print(b.build())


def evaluate_as_ops(p: Union[ProgramBlock, list[ProgramBlock]]) -> list[Instruction]:
    b = Builder()
    b.append(p)
    b.build()
    return [instruction_from_str(word) for word in b.build().split()]


 */

data class PreparedPush(
    val index: Int,
    val setter: (PreparedPush, Long, Int) -> Unit,
    val padding: Int,
    var invalidated: Boolean = false,
) {
    val placeholder: String = "[PREPARED-PUSH-$index]"

    fun set(n: Int) = set(n.toLong())

    fun set(n: Long) {
        if (invalidated) {
            throw IllegalStateException("This push was already set")
        }
        setter(this, n, padding)
        invalidated = true
    }

    fun setForJump(n: Int) {
        set((n - indexAfter() - 1))
    }

    fun indexEnd(): Int {
        return index + padding - 1
    }

    fun indexAfter(): Int {
        return index + padding
    }
}

private class BuilderState {
    var index = 0
    var program = mutableListOf<String>()
    var lastDepth = 0
    var lastSimpleFunction: SimpleFunction? = null
    var earlyExitPushes = mutableListOf<PreparedPush>()
    var preparedPushes = mutableListOf<PreparedPush>()
}

/** Transforms the ksplang DSL tree consisting of [Instruction], [SimpleFunction], and [ComplexFunction]
 * into real ksplang code. */
class KsplangBuilder {
    fun build(programTree: ComplexBlock): String {
        // For simplification, we use a global address padding (all addresses are padded to the same length)
        for (addressPad in 6..Int.MAX_VALUE) {
            try {
                val state = BuilderState()

                fun backupState(): BuilderState {
                    return BuilderState().apply {
                        index = state.index
                        program = state.program.toMutableList()
                        lastDepth = state.lastDepth
                        lastSimpleFunction = state.lastSimpleFunction
                        earlyExitPushes = state.earlyExitPushes.toMutableList()
                        preparedPushes = state.preparedPushes.toMutableList()
                    }
                }

                fun restoreState(backup: BuilderState) {
                    state.index = backup.index
                    state.program = backup.program.toMutableList()
                    state.lastDepth = backup.lastDepth
                    state.lastSimpleFunction = backup.lastSimpleFunction
                    state.earlyExitPushes = backup.earlyExitPushes.toMutableList()
                    state.preparedPushes = backup.preparedPushes.toMutableList()
                }

                fun applyPreparedPush(push: PreparedPush, n: Long, padding: Int) {
                    val index = state.program.indexOf(push.placeholder)
                    val paddedPush = extract { pushPaddedTo(n, padding) }
                    state.program[index] = build(paddedPush.getInstructions())
                }

                fun preparePaddedPush(padding: Int? = null): PreparedPush {
                    val padding = padding ?: addressPad
                    val push = PreparedPush(state.index, ::applyPreparedPush, padding)
                    state.program.add(push.placeholder)
                    state.index += padding
                    state.preparedPushes.add(push)
                    return push
                }

                fun expand(
                    block: Block,
                    isLast: Boolean = false,
                    depth: Int = 0,
                    useCalls: Boolean = true
                ) {
                    // A shorthand to expand recursively with correct params
                    fun e(block: Block) {
                        expand(block, isLast, depth + 1, useCalls)
                    }

                    if (depth > state.lastDepth) {
                        while (state.program.isNotEmpty() && state.program.last().isBlank()) {
                            state.program.removeLast()
                        }
                        if (state.program.isNotEmpty() && state.program.last() != "\n") {
                            state.program.add("\n")
                        }
                        state.program.add(" ".repeat(depth))
                    }
                    state.lastDepth = depth

                    when (block) {
                        is Instruction -> {
                            state.lastSimpleFunction = null
                            state.index++
                            state.program.add(block.text)
                            state.program.add(" ")
                        }

                        is SimpleFunction -> {
                            // TODO: Reintroduce push_on optimization
                            // TODO: Reintroduce callable functions
                            block.getInstructions().forEach { e(it) }
                            state.lastSimpleFunction = block
                        }

                        is ComplexFunction -> {
                            // It may be called complex, but it is so simple, oh so simple:
                            for (child in block.children) {
                                e(child)
                            }
                        }

                        is IfZero -> {
                            if (block.orElse == null) {
                                // We can specialize
                                throw NotImplementedError()
                            }

                            val thenPush = preparePaddedPush()
                            e(extract { roll(2, 1) })
                            e(brz)
                            e(pop2)
                            val otherwiseJPush = preparePaddedPush()
                            val otherwiseJIndex = state.index
                            e(j)
                            thenPush.set(state.index)
                            e(pop2)
                            for (b in block.children) {
                                e(b)
                            }
                            val endJPush = preparePaddedPush()
                            val endJIndex = state.index
                            e(j)
                            e(pop)
                            otherwiseJPush.set(state.index - otherwiseJIndex - 2)
                            for (b in block.orElse!!.children) {
                                e(b)
                            }
                            e(CS)
                            e(pop)
                            endJPush.set(state.index - endJIndex - 2)
                        }

                        is DoWhileZero -> {
                            e(CS)
                            e(CS)
                            val redoIndex = state.index
                            e(pop)
                            e(pop)
                            for (b in block.children) {
                                e(b)
                            }
                            e(extract { push(redoIndex) })
                            e(extract { roll(2, 1) })
                            e(brz)
                            e(pop2)
                            e(pop)
                        }
                    }
                }

                programTree.children.forEachIndexed { i, block ->
                    val isLast = i == programTree.children.size - 1
                    expand(block, isLast, depth = 0)
                }

                if (state.earlyExitPushes.isNotEmpty()) {
                    TODO()
                }

                return state.program.joinToString(" ")
            } catch (e: PaddingFailureException) {
                // Try again with a different address padding
            }
        }

        throw IllegalStateException("Could not find a suitable address padding")
    }

    fun build(instructions: SimpleFunction): String {
        return build(instructions.getInstructions())
    }

    fun build(instructions: List<Instruction>): String {
        return instructions.joinToString(" ") { it.text }
    }
}