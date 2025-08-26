package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.std.*

class WasmFunctionScope private constructor(
    val localTypes: List<ValType>,
    val returnTypes: List<ValType>,
) {
    // The locals are on the stack, going from 0 to localTypes.size - 1.
    // Then there may be intermediate values on top of the locals.
    private var intermediateStackValues = 0
    private var localsPopped: Boolean = false

    private fun ComplexFunction.dupLocal(index: Int) {
        check(!localsPopped)
        push(localTypes.size + intermediateStackValues - index)
        dupNth()
        intermediateStackValues++
    }

    private fun ComplexFunction.i32ToSigned() {
        // a
        dup()
        push(2147483648)
        // a a 2^31
        cmp()
        // a cmp -- 1 if a > 2^31
        //       -- 0 if a == 2^31, -1 if a < 2^31
        inc()
        // a cmp++ -- 1 or 2 if a >= 2^31
        //         -- 0 if if a < 2^31
        ifZero(popChecked = true) {
            // a < 2^31
            // a
        } otherwise {
            // a
            push(4294967296)
            sub()
            // a-2^32
        }
    }


    private fun ComplexFunction.instruction(stackSizeChange: Int, block: ComplexFunction.() -> Unit) {
        check(!localsPopped)
        intermediateStackValues += stackSizeChange
        block()
    }


    // TODO: Find last local usage in function, and instead of duplicating, we can consume it.
    fun ComplexFunction.getLocal(index: Int) {
        check(!localsPopped)
        dupLocal(index)
    }

    fun ComplexFunction.i32Add() = instruction(stackSizeChange = -1) {
        add()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32Sub() = instruction(stackSizeChange = -1) {
        sub()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32Mul() = instruction(stackSizeChange = -1) {
        // Unfortunately, we only have signed i64 multiplication, so we can overflow
        // even with multiplication of two i32 values.

        TODO("Not yet implemented due to overflows") // See Hacker's delight TABLE 2–2. OVERFLOW TEST FOR SIGNED MULTIPLICATION onwards, we can have a fast case

        /*
        mul()

        push(I32_MOD)
        swap2()
        modulo()
        */
    }

    fun ComplexFunction.i32DivSigned() = instruction(stackSizeChange = -1) {
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        div()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32DivUnsigned() = instruction(stackSizeChange = -1) {
        swap2()
        div()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32RemSigned() = instruction(stackSizeChange = -1) {
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        REM()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32RemUnsigned() = instruction(stackSizeChange = -1) {
        swap2()
        REM()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32Shl() = instruction(stackSizeChange = -1) {
        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        bitshift()

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32ShrUnsigned() = instruction(stackSizeChange = -1) {
        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        u32Shr()

        push(I32_MOD)
        swap2()
        modulo()
    }

    /**
     * Unsigned shift right. Do not use with by > 63, it would fail with division by zero.
     * Does not do sign extension (hence it is unsigned)
     * a by -> a>>by
     */
    private fun ComplexFunction.u32Shr() {
        push(1)
        swap2()
        // a 1 by
        bitshift()
        // a 2**by
        swap2()
        div()
        // a//(2**by)
        // a>>by
    }

    fun ComplexFunction.i32ShrSigned() = instruction(stackSizeChange = -1) {
        // We are using ((val+2^31)>>by) - (2^31>>by) from Hacker's Delight section 2.7
        // instead of (2^31>>by), we are using 1 << (31-by), which we can safely do because by is at most 31.
        // we need to convert val to signed representation for this to work as expected

        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        // val by    (for simplification, but it is mod 32)
        swap2()
        // by val
        i32ToSigned() // this is very important, we need full sign extension for the final sub to work as expected
        // by val    (we don't denote the sign extension on val either)

        add(2147483648) // cannot overflow if val is 32-bit
        dupSecond()
        // by val+2^31 by
        u32Shr()
        // by (val+2^31)>>by

        push(1)
        // by (val+2^31)>>by 1
        roll(3, 2)
        // (val+2^31)>>by 1 by
        push(31)
        subabs()
        // (val+2^31)>>by 1 31-by
        bitshift()
        // (val+2^31)>>by 1<<31-by
        // (val+2^31)>>by 2^31>>by
        sub()
        // ((val+2^31)>>by)-(2^31>>by)

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.bitAnd() = instruction(stackSizeChange = -1) {
        bitand()
        // No need to MOD as it cannot set any higher bits
    }

    fun ComplexFunction.bitOr() = instruction(stackSizeChange = -1) {
        bitor()
        // No need to MOD as it cannot set any higher bits
    }

    fun ComplexFunction.bitXor() = instruction(stackSizeChange = -1) {
        bitxor()
        // No need to MOD as it cannot set any higher bits
    }

    fun ComplexFunction.popLocals() {
        check(!localsPopped) { "Locals have already been popped in this scope" }
        // There should be only return values + locals on the stack now.
        check(intermediateStackValues == returnTypes.size) {
            "Inconsistent stack size: expected ${returnTypes.size} values on top of locals, got $intermediateStackValues values"
        }

        roll((localTypes.size + returnTypes.size).toLong(), returnTypes.size.toLong())
        repeat(localTypes.size) {
            pop()
        }
        localsPopped = true
    }

    companion object {
        private const val I32_MOD = 4294967296L

        fun ComplexFunction.initialize(
            params: List<ValType>,
            localTypes: List<ValType>,
            returns: List<ValType>
        ): WasmFunctionScope {
            require(returns.all { it in listOf(ValType.I32, ValType.I64, ValType.F32, ValType.F64) }) {
                "Unsupported return type(s) found in $returns"
            }

            val scope = WasmFunctionScope(params + localTypes, returns)
            // We need to push defaults for each local
            localTypes.forEach { type ->
                when (type) {
                    ValType.BOT -> error("Some Chicory internals leaking into its API")
                    ValType.RefBot -> error("Some Chicory internals leaking into its API")
                    ValType.I32 -> push(0)
                    ValType.I64 -> push(0)
                    ValType.F32 -> push(0)
                    ValType.F64 -> push(0)
                    ValType.V128 -> error("v128 (SIMD feature) is not supported right now")
                    ValType.FuncRef -> error("funcref (Reference Types for WebAssembly feature) is not supported")
                    ValType.ExnRef -> error("Exception handling is not supported")
                    ValType.ExternRef -> error("externref (Reference Types for WebAssembly feature) is not supported")
                    else -> error("Unsupported type: $type")
                }
            }
            return scope
        }
    }
}

