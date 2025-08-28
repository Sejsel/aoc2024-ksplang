package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.whileNonZero
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
        // Based on Hacker's Delight Chapter 2.6 - Sign Extension

        // a
        add(2147483648)
        // a+2^31
        i32Mod()
        // (a+2^31)&(0xFFFFFFFF)
        add(-2147483648)
        // ((a+2^31)&(0xFFFFFFFF))-2^31
    }

    private fun ComplexFunction.i32Mod() {
        push(4294967295)
        bitand()
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

        i32Mod()
    }

    fun ComplexFunction.i32Sub() = instruction(stackSizeChange = -1) {
        sub()

        i32Mod()
    }

    fun ComplexFunction.i32Mul() = instruction(stackSizeChange = -1) {
        // Unfortunately, we only have signed i64 multiplication, so we can overflow
        // even with multiplication of two i32 values.

        TODO("Not yet implemented due to overflows") // See Hacker's delight TABLE 2–2. OVERFLOW TEST FOR SIGNED MULTIPLICATION onwards, we can have a fast case

        /*
        mul()

        i32Mod()
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

        i32Mod()
    }

    fun ComplexFunction.i32DivUnsigned() = instruction(stackSizeChange = -1) {
        swap2()
        div()

        i32Mod()
    }

    fun ComplexFunction.i32RemSigned() = instruction(stackSizeChange = -1) {
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        REM()

        i32Mod()
    }

    fun ComplexFunction.i32RemUnsigned() = instruction(stackSizeChange = -1) {
        swap2()
        REM()

        i32Mod()
    }

    fun ComplexFunction.i32Shl() = instruction(stackSizeChange = -1) {
        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        bitshift()

        i32Mod()
    }

    fun ComplexFunction.i32ShrUnsigned() = instruction(stackSizeChange = -1) {
        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        u32Shr()

        i32Mod()
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

        i32Mod()
    }

    // Used by both i32Rotl and i32Rotr
    private fun ComplexFunction.i32RotateLeft() {
        bitshift()
        // a<<by
        dup()
        // a<<by a<<by
        push(32)
        u32Shr()
        // a<<by (a<<by)>>32
        bitor()
        // (a<<by) | ((a<<by)>>32)

        i32Mod()
    }

    fun ComplexFunction.i32Rotl() = instruction(stackSizeChange = -1) {
        // a by
        push(32)
        swap2()
        modulo()
        // a by%32
        // a by    (for simplification)
        i32RotateLeft()
    }

    fun ComplexFunction.i32Rotr() = instruction(stackSizeChange = -1) {
        // a by
        push(32)
        swap2()
        modulo()
        // a by%32
        // a by    (for simplification)
        ifZero {
            // a 0
        } otherwise {
            push(32)
            subabs()
            // a 32-by
        }
        // a 32-by
        i32RotateLeft()
    }

    fun ComplexFunction.i32Eqz() = instruction(stackSizeChange = 0) {
        // a
        zeroNot()
    }

    fun ComplexFunction.i32Eq() = instruction(stackSizeChange = -1) {
        // a b
        // unlike in i64, where we would need cmp(), we can do subabs as it cannot overflow
        subabs()
        // |a-b|
        zeroNot()
        // a==b?1:0
    }

    fun ComplexFunction.i32Ne() = instruction(stackSizeChange = -1) {
        // a b
        // unlike in i64, where we would need cmp(), we can do subabs as it cannot overflow
        subabs()
        // |a-b|
        zeroNot()
        // a==b?1:0
        zeroNotPositive()
        // a==b?0:1
    }

    private fun ComplexFunction.i32CountSetBits() {
        // a
        push(0)
        // a res
        swap2()
        // res a
        whileNonZero {
            // res a
            swap2()
            inc()
            swap2()
            // res+1 a
            dup()
            // res+1 a a
            dec() // we can do this because this is i32
            // res+1 a a-1
            bitand()
            // res+1 (a&(a-1))
        }
        // res
    }

    fun ComplexFunction.i32PopCnt() = instruction(stackSizeChange = 0) {
        i32CountSetBits()
    }

    fun ComplexFunction.i32Clz() = instruction(stackSizeChange = 0) {
        ifZero {
            // 0
            push(32)
            // 0 32
            pop2()
            // 32
        } otherwise {
            // a
            push(0)
            // a res
            swap2()
            // res a
            dup()
            push(2147483648)
            bitand()
            // res a a&(2^31)
            zeroNotPositive() // works only because this is i32 and not i64
            whileNonZero {
                // res-1 a check
                pop()
                // res-1 a
                swap2()
                inc()
                swap2()
                // res a

                push(1)
                bitshift()
                // res a<<1
                dup()
                // res a<<1 a<<1
                push(2147483648)
                bitand()
                // res a<<1 (0 if MSB is not set)
                zeroNotPositive()
                // res a<<1 (1 if MSB is not set)
            }
            // res a<<clz
            pop()
        }
    }

    fun ComplexFunction.i32Ctz() = instruction(stackSizeChange = 0) {
        // a
        dup()
        // a a
        dec() // only works for i32
        // a a-1
        swap2()
        // a-1 a
        bitnot()
        // a-1 ~a
        bitand()
        // (a-1)&(~a)
        i32Mod()
        i32CountSetBits()
    }

    private fun ComplexFunction.i32Lt() {
        i32Ge()
        zeroNotPositive()
    }

    private fun ComplexFunction.i32Gt() {
        i32Le()
        zeroNotPositive()
    }

    private fun ComplexFunction.i32Le() {
        // a b
        sub()
        sgn()
        // sgn(a-b)
        // 1 if a > b, 0 if a = b, -1 if a < b
        zeroNotPositive()
        // 0 if a > b, 1 if a = b, 1 if a < b
    }

    private fun ComplexFunction.i32Ge() {
        // a b
        sub()
        sgn()
        // sgn(a-b)
        // 1 if a > b, 0 if a = b, -1 if a < b
        negate()
        // -1 if a > b, 0 if a = b, 1 if a < b
        zeroNotPositive()
        // 1 if a > b, 1 if a = b, 0 if a < b
    }

    fun ComplexFunction.i32LtUnsigned() = instruction(stackSizeChange = -1) {
        i32Lt()
    }

    fun ComplexFunction.i32LtSigned() = instruction(stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Gt() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32GtUnsigned() = instruction(stackSizeChange = -1) {
        i32Gt()
    }

    fun ComplexFunction.i32GtSigned() = instruction(stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Lt() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32LeUnsigned() = instruction(stackSizeChange = -1) {
        i32Le()
    }

    fun ComplexFunction.i32LeSigned() = instruction(stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Ge() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32GeUnsigned() = instruction(stackSizeChange = -1) {
        i32Ge()
    }

    fun ComplexFunction.i32GeSigned() = instruction(stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Le() // we swapped the arguments, no swapping back (for perf)
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

    private fun ComplexFunction.i64WrappingAdd() {
        // We must avoid SIGNED addition overflow here. Note that this is different from UNSIGNED overflow
        // for example 0xFF...FF + 1 would be unsigned overflow, but is NOT signed overflow.

        // It is tempting to just mask out top bit and then do standard addition, and apply the top bit later.
        // However, that would still potentially do a signed overflow, we can only do 62-bit addition safely.

        // Instead of handling top 2 bits, we instead use two 32-bit additions, which should be the same amount of effort

        // a b
        dupAb()
        // a b a b
        i32Mod()
        swap2()
        i32Mod()
        // a b b_lo a_lo
        add() // result may be 33-bit
        // a b lo+  -- the + indicates lo can have carry (be 33-bit)
        dup()
        // a b lo+ lo+
        push(32)
        u32Shr()
        // a b lo+ lo_carry   -- either 0 or 1
        roll(4, 2)
        // lo+ lo_carry a b
        push(32)
        u32Shr() // doing this may result in sign extension, but we are getting rid of that with i32Mod
        i32Mod()
        // lo+ lo_carry a b_hi
        swap2()
        push(32)
        u32Shr() // doing this may result in sign extension, but we are getting rid of that with i32Mod
        i32Mod()
        // lo+ lo_carry b_hi a_hi
        add() // result may be 33-bit
        // lo+ lo_carry b_hi+a_hi
        add()
        // lo+ lo_carry+b_hi+a_hi
        // lo+ hi+
        push(32)
        bitshift()
        // lo+ hi+<<32
        // lo+ hi<<32  -- bitshift will get rid of the carry on hi+
        swap2()
        // hi<<32 lo+  -- lo+ may still be 33-bit if it had carry
        i32Mod()
        // hi<<32 lo
        bitor()
        // (hi<<32)|lo
    }

    fun ComplexFunction.i64Add() = instruction(stackSizeChange = -1) {
        i64WrappingAdd()
    }

    fun ComplexFunction.i64Sub() = instruction(stackSizeChange = -1) {
        // a b
        dup()
        isMinRaw()
        // a b isMin(b)?0:nonzero
        ifZero(popChecked = true) {
            // a b
        } otherwise {
            negate()
            // a -b
        }
        i64WrappingAdd()
    }

    fun ComplexFunction.i64DivSigned() = instruction(stackSizeChange = -1) {
        // a b
        swap2()
        // b a
        div() // can only overflow in case when a is MIN and b is -1, which is undefined behavior in WASM
    }

    fun ComplexFunction.i64Shl() = instruction(stackSizeChange = -1) {
        // val by
        push(64)
        swap2()
        modulo()
        // val by%64
        bitshift()
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

