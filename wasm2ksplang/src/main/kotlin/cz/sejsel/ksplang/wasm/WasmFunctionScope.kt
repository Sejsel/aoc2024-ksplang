package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.wasm.types.ValType
import cz.sejsel.ksplang.dsl.core.Block
import cz.sejsel.ksplang.dsl.core.CallInline
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.otherwise
import cz.sejsel.ksplang.dsl.core.whileNonZero
import cz.sejsel.ksplang.std.*

class WasmFunctionScope private constructor(
    val localTypes: List<ValType>,
    val returnTypes: List<ValType>,
    val globalState: ModuleTranslatorState
) {
    // The locals are on the stack, going from 0 to localTypes.size - 1.
    // Then there may be intermediate values on top of the locals.
    private var intermediateStackValues = 0
    private var localsPopped: Boolean = false

    private fun ComplexBlock.dupLocalValue(index: Int) {
        dupKthZeroIndexed(localTypes.size + intermediateStackValues - index - 1)
    }

    private fun ComplexBlock.setLocalValue(index: Int) {
        setKth(localTypes.size + intermediateStackValues - index - 1)
    }

    fun ComplexFunction.drop() = instruction("drop", stackSizeChange = -1) {
        pop()
    }

    fun ComplexFunction.select() = instruction("select", stackSizeChange = -2) {
        // this is defined on i32, but we can support any value (also because of validation)
        // a b cond
        ifZero {
            // a b 0
            pop()
            pop2()
            // b
        } otherwise {
            // a b nonzero
            pop()
            pop()
            // a
        }
    }

    fun ComplexFunction.i32Const(const: Long) = instruction("i32Const($const)", stackSizeChange = 1) {
        push(const)
    }

    fun ComplexFunction.i64Const(const: Long) = instruction("i64Const($const)", stackSizeChange = 1) {
        push(const)
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

    private fun ComplexBlock.i32Mod() {
        push(4294967295)
        bitand()
    }


    private fun ComplexFunction.instruction(name: String, stackSizeChange: Int, block: ComplexFunction.() -> Unit) {
        check(!localsPopped)
        complexFunction(name) {
            block()
        }
        intermediateStackValues += stackSizeChange
    }

    fun ComplexFunction.getGlobal(index: Int) = instruction("getGlobal($index)", stackSizeChange = 1) {
        call(globalState.getGlobalFunction(index), inline = CallInline.ALWAYS)
    }

    fun ComplexFunction.setGlobal(index: Int) = instruction("setGlobal($index)", stackSizeChange = -1) {
        call(globalState.setGlobalFunction(index), inline = CallInline.ALWAYS)
    }

    // TODO: Find last local usage in function, and instead of duplicating, we can consume it.
    fun ComplexFunction.getLocal(index: Int) = instruction("getLocal($index)", stackSizeChange = 1) {
        dupLocalValue(index)
    }

    fun ComplexFunction.setLocal(index: Int) = instruction("setLocal($index)", stackSizeChange = -1) {
        setLocalValue(index)
    }

    fun ComplexFunction.teeLocal(index: Int) = instruction("teeLocal($index)", stackSizeChange = 0) {
        dup()
         // At this point, setLocalValue is not aware of the new intermediate value,
        // so we are adding - 1 to the index.
        setLocalValue(index - 1)
    }

    fun ComplexFunction.i32Add() = instruction("i32Add", stackSizeChange = -1) {
        add()

        i32Mod()
    }

    fun ComplexFunction.i32Sub() = instruction("i32Sub", stackSizeChange = -1) {
        sub()

        i32Mod()
    }

    /**
     * Multiplies two 32-bit (!) unsigned integers, producing a 64-bit result.
     */
    private fun ComplexBlock.u32Mul() = complexFunction("u32Mul") {
        // Unfortunately, we only have signed i64 multiplication natively, so we can overflow
        // even with multiplication of two i32 values.
        // The input here is always positive (thanks to i32 bit layout invariant there are at least 32 leading zeroes),
        // so overflow occurs iff the result would need to set the MSB (bit 63).

        // If 32-bit MSB is NOT set on either a or b, there cannot be 64-bit signed overflow.
        // If both are set, there may be, so we fallback to a slower approach in that case.

        // a b
        dupAb()
        // a b a b
        push(2147483648) // 1<<31 (32-bit MSB)
        bitand()
        zeroNotPositive()
        // a b a b_32MSB?0:1
        swap2()
        push(2147483648) // 1<<31 (32-bit MSB)
        bitand()
        zeroNotPositive()
        // a b b_32MSB?0:1 a_32MSB?0:1
        add()
        // a b (b_32MSB?0:1)+(a_32MSB?0:1) -- 0 if both MSB are set, 1 or 2 otherwise
        ifZero {
            // a b 0
            // mask out the MSB on b, multiply, then add a << 31 (wrapping i64)
            pop()
            push(2147483647) // (1<<31)-1
            bitand()
            // a b&0x7FFF...FFF
            dupSecond()
            // a b&0x7FFF...FFF a
            push(31)
            bitshift()
            // a b&0x7FFF...FFF a<<31
            roll(3, 1)
            // a<<31 a b&0x7FFF...FFF
            mul()
            // a<<31 a*b&0x7FFF...FFF
            i64WrappingAdd()
            // (a<<31)+(a*(b&0x7FFF...FFF))
        } otherwise {
            // a b 1/2
            pop()
            mul()
            // a*b
        }
    }

    fun ComplexFunction.i32Mul() = instruction("i32Mul", stackSizeChange = -1) {
        u32Mul()
        i32Mod()
    }

    fun ComplexFunction.i32DivSigned() = instruction("i32DivSigned", stackSizeChange = -1) {
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        div()

        i32Mod()
    }

    fun ComplexFunction.i32DivUnsigned() = instruction("i32DivUnsigned", stackSizeChange = -1) {
        swap2()
        div()

        i32Mod()
    }

    fun ComplexFunction.i32RemSigned() = instruction("i32RemSigned", stackSizeChange = -1) {
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        REM()

        i32Mod()
    }

    fun ComplexFunction.i32RemUnsigned() = instruction("i32RemUnsigned", stackSizeChange = -1) {
        swap2()
        REM()

        i32Mod()
    }

    fun ComplexFunction.i32Shl() = instruction("i32Shl", stackSizeChange = -1) {
        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        bitshift()

        i32Mod()
    }

    fun ComplexFunction.i32ShrUnsigned() = instruction("i32ShrUnsigned", stackSizeChange = -1) {
        // val by
        push(32)
        swap2()
        modulo()
        // val by%32
        u63Shr()

        i32Mod()
    }

    /**
     * Unsigned shift right. Do not use with by > 63, it would fail with division by zero.
     * Does not do sign extension (hence it is unsigned).
     * Cannot handle negative values (MSB set), hence it is u63Shr and not u64Shr.
     *
     * Signature: `a by -> a>>by`
     */
    private fun ComplexBlock.u63Shr() {
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

    fun ComplexFunction.i32ShrSigned() = instruction("i32ShrSigned", stackSizeChange = -1) {
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
        u63Shr()
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
        u63Shr()
        // a<<by (a<<by)>>32
        bitor()
        // (a<<by) | ((a<<by)>>32)

        i32Mod()
    }

    fun ComplexFunction.i32Rotl() = instruction("i32Rotl", stackSizeChange = -1) {
        // a by
        push(32)
        swap2()
        modulo()
        // a by%32
        // a by    (for simplification)
        i32RotateLeft()
    }

    fun ComplexFunction.i32Rotr() = instruction("i32Rotr", stackSizeChange = -1) {
        // a by
        push(32)
        swap2()
        modulo()
        // a by%32
        // a by    (for simplification)
        ifZero {
            // a 0
            pop()
            // a
        } otherwise {
            push(32)
            subabs()
            // a 32-by
            i32RotateLeft()
        }
    }

    fun ComplexFunction.i32Eqz() = instruction("i32Eqz", stackSizeChange = 0) {
        // a
        zeroNot()
    }

    fun ComplexFunction.i32Eq() = instruction("i32Eq", stackSizeChange = -1) {
        // a b
        // unlike in i64, where we would need cmp(), we can do subabs as it cannot overflow
        subabs()
        // |a-b|
        zeroNot()
        // a==b?1:0
    }

    fun ComplexFunction.i32Ne() = instruction("i32Ne", stackSizeChange = -1) {
        // a b
        // unlike in i64, where we would need cmp(), we can do subabs as it cannot overflow
        subabs()
        // |a-b|
        zeroNot()
        // a==b?1:0
        zeroNotPositive()
        // a==b?0:1
    }

    /**
     * Counts set bits, but does not work with -2^63.
     */
    private fun ComplexBlock.i63CountSetBits() {
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

    private fun ComplexFunction.i64CountSetBits() {
        // a
        dup()
        push(Long.MIN_VALUE)
        bitand()
        // a a_MSB
        ifZero {
            // a 0
            pop()
            i63CountSetBits()
        } otherwise {
            // a 1<<63
            inc()
            negate()
            // a 0x7FFF...FFF
            bitand()
            // a&0x7FFF...FFF
            i63CountSetBits()
            // popcnt-1
            inc()
            // popcnt
        }
    }

    fun ComplexFunction.i32PopCnt() = instruction("i32PopCnt", stackSizeChange = 0) {
        i63CountSetBits()
    }

    private fun ComplexBlock.i32CountLeadingZeros() = complexFunction {
        ifZero {
            // 0
            pushOn(0, 32)
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

    fun ComplexFunction.i32Clz() = instruction("i32Clz", stackSizeChange = 0) {
        i32CountLeadingZeros()
    }

    fun ComplexFunction.i32Ctz() = instruction("i32Ctz", stackSizeChange = 0) {
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
        i63CountSetBits()
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

    fun ComplexFunction.i32LtUnsigned() = instruction("i32LtUnsigned", stackSizeChange = -1) {
        i32Lt()
    }

    fun ComplexFunction.i32LtSigned() = instruction("i32LtSigned", stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Gt() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32GtUnsigned() = instruction("i32GtUnsigned", stackSizeChange = -1) {
        i32Gt()
    }

    fun ComplexFunction.i32GtSigned() = instruction("i32GtSigned", stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Lt() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32LeUnsigned() = instruction("i32LeUnsigned", stackSizeChange = -1) {
        i32Le()
    }

    fun ComplexFunction.i32LeSigned() = instruction("i32LeSigned", stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Ge() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32GeUnsigned() = instruction("i32GeUnsigned", stackSizeChange = -1) {
        i32Ge()
    }

    fun ComplexFunction.i32GeSigned() = instruction("i32GeSigned", stackSizeChange = -1) {
        i32ToSigned()
        swap2()
        i32ToSigned()
        // b a
        i32Le() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.bitAnd() = instruction("bitAnd", stackSizeChange = -1) {
        bitand()
        // No need to MOD as it cannot set any higher bits
    }

    fun ComplexFunction.bitOr() = instruction("bitOr", stackSizeChange = -1) {
        bitor()
        // No need to MOD as it cannot set any higher bits
    }

    fun ComplexFunction.bitXor() = instruction("bitXor", stackSizeChange = -1) {
        bitxor()
        // No need to MOD as it cannot set any higher bits
    }

    private fun ComplexBlock.i64WrappingAdd() {
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
        u63Shr()
        // a b lo+ lo_carry   -- either 0 or 1
        roll(4, 2)
        // lo+ lo_carry a b
        push(32)
        u64Shr()
        i32Mod()
        // lo+ lo_carry a b_hi
        swap2()
        push(32)
        u64Shr()
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

    private fun ComplexBlock.i64WrappingSub() {
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

    fun ComplexFunction.i64Add() = instruction("i64Add", stackSizeChange = -1) {
        i64WrappingAdd()
    }

    fun ComplexFunction.i64Sub() = instruction("i64Sub", stackSizeChange = -1) {
        i64WrappingSub()
    }

    private fun ComplexBlock.i64WrappingMul() = complexFunction("i64WrappingMul") {
        // We split the calculation into 4 32-bit multiplications and add them up:
        // - a_lo * b_lo   -- there is carry here which also gets added up
        // - (a_hi * b_lo) << 32
        // - (b_hi * a_lo) << 32
        // - (a_hi * b_hi) << 64 (always zero, so we skip it)

        // a b
        dupAb()
        dupAb()
        // a b a b a b
        i32Mod()
        swap2()
        i32Mod()
        // a b a b b_lo a_lo
        u32Mul()
        // a b a b b_lo*a_lo
        roll(5, 1)
        // b_lo*a_lo a b a b
        i32Mod()
        // b_lo*a_lo a b a b_lo
        swap2()
        // b_lo*a_lo a b b_lo a
        push(32); u64Shr()
        i32Mod()
        // b_lo*a_lo a b b_lo a_hi
        u32Mul()
        // b_lo*a_lo a b b_lo*a_hi
        push(32); bitshift()
        // b_lo*a_lo a b (b_lo*a_hi)<<32
        roll(4, 1)
        // (b_lo*a_hi)<<32 b_lo*a_lo a b
        push(32); u64Shr()
        i32Mod()
        // (b_lo*a_hi)<<32 b_lo*a_lo a b_hi
        swap2()
        i32Mod()
        // (b_lo*a_hi)<<32 b_lo*a_lo b_hi a_lo
        u32Mul()
        // (b_lo*a_hi)<<32 b_lo*a_lo b_hi*a_lo
        push(32); bitshift()
        // (b_lo*a_hi)<<32 b_lo*a_lo (b_hi*a_lo)<<32
        i64WrappingAdd()
        i64WrappingAdd()
        // ((b_lo*a_hi)<<32)+(b_lo*a_lo)+((b_hi*a_lo)<<32)
    }

    fun ComplexFunction.i64Mul() = instruction("i64Mul", stackSizeChange = -1) {
        // TODO: Try specialization with small inputs (no overflow) for a significant speedup (push mask + bitand + ifzero)
        i64WrappingMul()
    }

    fun ComplexFunction.i64DivSigned() = instruction("i64DivSigned", stackSizeChange = -1) {
        // a b
        swap2()
        // b a
        div() // can only overflow in case when a is MIN and b is -1, which is undefined behavior in WASM
    }

    fun ComplexFunction.i64DivUnsigned() = instruction("i64DivUnsigned", stackSizeChange = -1) {
        // Here we need to implement 64-bit unsigned division,
        // but we only have 63-bit unsigned division available (by masking out MSB)

        // a b
        swap2()
        // b a
        dup()
        // b a a
        push(Long.MIN_VALUE)
        bitand()
        // b a a_MSB
        ifZero {
            // b a 0
            pop()
            // b a
            dupSecond()
            push(Long.MIN_VALUE)
            bitand()
            // b a b_MSB
            ifZero {
                // b a 0
                pop()
                div()
            } otherwise {
                // b > a, result is 0

                // b a 1<<63
                push(0)
                pop2()
                pop2()
                pop2()
                // 0
            }
        } otherwise {
            // b a 1<<63
            dupThird()
            // b a 1<<63 b
            bitand()
            // b a b_MSB
            ifZero {
                // b a 0     -- b is positive, a is negative
                pop()
                // b a
                // we calculate
                //  q = ((a>>1)/b)*2
                //  r = a - qb
                //  if r >= b (unsigned) then q++
                //  q is result
                // source: Hacker's Delight Chapter 9.3 Using Signed Short Division
                dupAb()
                // b a b a
                push(1)
                u64ShrForNegativeNumbers()
                // b a b a>>1
                div()
                // b a a>>1/b
                push(1)
                bitshift()
                // b a ((a>>1)/b)*2
                // b a q
                dup()
                // b a q q
                dupFourth()
                // b a q q b
                i64WrappingMul()
                // b a q qb
                permute("b a q qb", "q b a qb")
                // q b a qb
                i64WrappingSub()
                // q b a-qb
                // q b r
                u64Le()
                // q b<=r?1:0
                // q r>=b?1:0
                ifZero {
                    // q 0
                    pop()
                } otherwise {
                    // q 0
                    pop()
                    inc()
                    // q+1
                }
                // q
            } otherwise {
                // since a and b both have MSB set, the only result can be 0 or 1
                // b a 1<<63
                pop()
                // b a
                cmp() // unfortunately, we can have a = MIN, so we cannot just do sgn(b-a) (negating a would overflow)
                // sgn(b-a)  - note this is signed and on negative numbers, so it inverted from unsigned result
                //    1 if a < b (unsigned)
                //    0 if a == b (unsigned)
                //   -1 if a > b (unsigned)
                zeroNotPositive()
                // 0 if a < b
                // 1 if a >= b
                // that is also our division result
            }
        }
    }

    fun ComplexFunction.i64RemSigned() = instruction("i64RemSigned", stackSizeChange = -1) {
        // a b
        swap2()
        // b a
        REM()
    }

    fun ComplexFunction.i64RemUnsigned() = instruction("i64RemUnsigned", stackSizeChange = -1) {
        // Here we need to implement 64-bit unsigned division,
        // but we only have 63-bit unsigned division available (by masking out MSB)

        // a b
        swap2()
        // b a
        dup()
        // b a a
        push(Long.MIN_VALUE)
        bitand()
        // b a a_MSB
        ifZero {
            // b a 0
            pop()
            // b a
            dupSecond()
            push(Long.MIN_VALUE)
            bitand()
            // b a b_MSB
            ifZero {
                // b a 0
                pop()
                REM() // both b and a are positive, so we can use native REM
            } otherwise {
                // b > a, result is a
                // b a 1<<63
                pop()
                // b a
                pop2()
                // a
            }
        } otherwise {
            // b a 1<<63
            dupThird()
            // b a 1<<63 b
            bitand()
            // b a b_MSB
            ifZero {
                // b a 0     -- b is positive, a is negative
                pop()
                // b a
                // we calculate
                //  q = ((a>>1)/b)*2
                //  r = a - qb
                //  r is the reminder, but it may be >= b, so we need to mod by b
                // source: Hacker's Delight Chapter 9.3 Using Signed Short Division
                dupAb()
                // b a b a
                push(1)
                u64ShrForNegativeNumbers()
                // b a b a>>1
                div()
                // b a a>>1/b
                push(1)
                bitshift()
                // b a ((a>>1)/b)*2
                // b a q
                dupFourth()
                // b a q b
                i64WrappingMul()
                // b a qb
                i64WrappingSub()
                // b a-qb
                // b r
                // Now we need an unsigned REM, yet again
                dupAb()
                // b r b r
                u64Le()
                // b r r>=b?1:0
                ifZero {
                    // b r 0
                    pop()
                    pop2()
                    // r
                } otherwise {
                    // b r 0
                    pop()
                    swap2()
                    // r b
                    i64WrappingSub()
                    // r-b
                }
                // r%b
            } otherwise {
                // since a and b both have MSB set, the only div result can be 0 or 1, which we can use to choose the rem
                // b a 1<<63
                pop()
                // b a
                dupAb()
                // b a b a
                cmp() // unfortunately, we can have a = MIN, so we cannot just do sgn(b-a) (negating a would overflow)
                // sgn(b-a)  - note this is signed and on negative numbers, so it inverted from unsigned result
                //    1 if a < b (unsigned)
                //    0 if a == b (unsigned)
                //   -1 if a > b (unsigned)
                zeroNotPositive()
                // 0 if a < b (unsigned) - result is a
                // 1 if a >= b - result is unsigned b-a
                ifZero {
                    // b a 0
                    pop()
                    pop2()
                    // a
                } otherwise {
                    // b a 1
                    pop()
                    swap2()
                    // a b
                    i64WrappingSub()
                }
            }
        }
    }

    fun ComplexFunction.i64Shl() = instruction("i64Shl", stackSizeChange = -1) {
        // val by
        push(64)
        swap2()
        modulo()
        // val by%64
        bitshift()
    }

    /**
     * Unsigned shift right for use with values with MSB set.
     * Skips the check for the optimized non-MSB-set path in [u64Shr].
     * Do not use with by > 63, it would fail with division by zero.
     * Does not do sign extension (hence it is unsigned).
     *
     * Signature: `a by -> a>>by`
     */
    private fun ComplexBlock.u64ShrForNegativeNumbers() = complexFunction {
        push(Long.MAX_VALUE)
        u64ShrInner()
    }

    /**
     * Unsigned shift right, expects 2^63-1 on top of the parameters.
     * Do not use with by > 63, it would fail with division by zero.
     * Does not do sign extension (hence it is unsigned).
     *
     * Signature: `a by 2^63-1 -> a>>by`
     */
    private fun ComplexBlock.u64ShrInner() = complexFunction {
        // a by 0x7FFF...FFF    -- the max positive value
        roll(3, 2)
        // by 0x7FFF...FFF a
        bitand()
        // by a&(0x7FFF...FFF)  -- a with MSB masked out
        dupSecond()
        // by a&(0x7FFF...FFF) by
        u63Shr()
        // by a&(0x7FFF...FFF)>>by
        swap2()
        // a&(0x7FFF...FFF)>>by by
        push(63)
        subabs()
        // a&(0x7FFF...FFF)>>by 63-by
        push(1)
        swap2()
        // a&(0x7FFF...FFF)>>by 1 63-by
        bitshift()
        // a&(0x7FFF...FFF)>>by 1<<(63-by)
        bitor()
        // a>>by
    }

    /**
     * Unsigned shift right. Do not use with by > 63, it would fail with division by zero.
     * Does not do sign extension (hence it is unsigned).
     *
     * Signature: `a by -> a>>by`
     */
    private fun ComplexBlock.u64Shr() = complexFunction {
        // We have two cases to handle:
        // - if the MSB is not set, we can just use u63Shr (standard division)
        // - if the MSB is set, we need to mask out the bit, use u63Shr, and then put the bit back (shifted obviously)
        // a by
        dupSecond()
        // a by a
        push(Long.MIN_VALUE) // just msb set
        bitand()
        // a by a&(1<<63)
        ifZero {
            // a by 0
            pop()
            // a by
            u63Shr()
            // a>>by
        } otherwise {
            // a by 1<<63           -- the min negative value
            inc()
            negate()
            // a by 0x7FFF...FFF    -- the max positive value
            u64ShrInner()
        }
    }

    fun ComplexFunction.i64ShrUnsigned() = instruction("i64Shr", stackSizeChange = -1) {
        // a by
        push(64)
        swap2()
        modulo()
        // a by%64
        u64Shr()
    }

    fun ComplexFunction.i64ShrSigned() = instruction("i64ShrSigned", stackSizeChange = -1) {
        // Approach: handle negative and positive values separately

        // val by
        push(64)
        swap2()
        modulo()
        // val by%64
        ifZero {
            // val 0
            pop()
        } otherwise {
            // val by    -- by is in [1,63]
            swap2()
            // by val
            dup()
            push(Long.MIN_VALUE)
            bitand()
            // by val val_MSB
            ifZero {
                // by val 0
                // this is a positive number, we can safely do unsigned shift
                pop()
                swap2()
                // val by val
                u64Shr()
            } otherwise {
                // do unsigned shift; then we do sign extension

                // unlike for i32, we do not have high-performance ops we could use with Hacker's Delight
                // unsigned shift-right formulas, we would have to fall back to i64WrappingAdd, and that is way too slow
                // instead we just do a sign extension using (x & 2^(by-1)) - (x & (2^(by-1)-1))

                // by val 1<<63
                pop()
                // by val
                dupSecond()
                // by val by
                u64Shr()
                // by val>>by
                swap2()
                // val>>by by
                // by is in [1,63], so we can safely do 63-by without modulo
                push(63)
                subabs()
                // val>>by 63-by  -- 63-by is in [0,62]
                dupAb()
                // val>>by 63-by val>>by 63-by
                push(1)
                swap2()
                bitshift()
                // val>>by 63-by val>>by 1<<(63-by)
                bitand()
                // val>>by 63-by ((val>>by)&(1<<(63-by)))
                roll(3, 1)
                // ((val>>by)&(1<<(63-by))) val>>by 63-by
                push(1)
                swap2()
                bitshift()
                // ((val>>by)&(1<<(63-by))) val>>by 1<<(63-by)
                dec()
                // ((val>>by)&(1<<(63-by))) val>>by (1<<(63-by)-1)
                bitand()
                // ((val>>by)&(1<<(63-by))) (val>>by&(1<<(63-by)-1))
                swap2()
                sub() // This should be safe because MSB cannot be set (we subtracting two positive numbers)
                // ((val>>by)&(1<<(63-by)))-(val>>by&(1<<(63-by)-1))
            }
        }
    }

    private fun ComplexBlock.i64RotateLeft() {
        // a by
        dupAb()
        // a by a by
        bitshift()
        // a by a<<by
        roll(3, 1)
        // a<<by a by
        push(64)
        subabs()
        // a<<by a 64-by
        push(64)
        swap2()
        modulo() // if we don't do this, by = zero will cause this to be 64, which u64Shr cannot handle
        // a<<by a (64-by)%64
        u64Shr()
        // a<<by a>>(64-by)
        bitor()
        // a<<by|a>>(64-by)
    }

    fun ComplexFunction.i64Rotl() = instruction("i64Rotl", stackSizeChange = -1) {
        // a by
        push(64)
        swap2()
        modulo()
        // a by%64
        i64RotateLeft()
    }

    fun ComplexFunction.i64Rotr() = instruction("i64Rotr", stackSizeChange = -1) {
        // a by
        push(64)
        swap2()
        modulo()
        // a by%64
        // a by    (for simplification)
        ifZero {
            // a 0
            pop()
            // a
        } otherwise {
            // a by
            push(64)
            subabs()
            // a 64-by
            i64RotateLeft()
        }
    }

    fun ComplexFunction.i64Ctz() = instruction("i64Ctz", stackSizeChange = 0) {
        // a
        dup()
        isMinRaw()
        // a is_min?0:nonzero
        ifZero {
            // a 0
            pushOn(0, 63)
            // a 0 63
            pop2(); pop2()
            // 63
        } otherwise {
            // a nonzero
            pop()
            // a
            dup()
            dec() // this is why we need to handle -2^63 in a special way
            // a a-1
            swap2()
            // a-1 a
            bitnot()
            // a-1 ~a
            bitand()
            // (a-1)&(~a)
            i64CountSetBits() // a is not MIN, but (a-1)&(~a) can be MIN when a = 0
            // ctz
        }
    }

    fun ComplexFunction.i64Clz() = instruction("i64Clz", stackSizeChange = 0) {
        ifZero {
            // 0
            pushOn(0, 64)
            // 0 64
            pop2()
            // 64
        } otherwise {
            // a
            push(0)
            // a res
            swap2()
            // res a
            dup()
            push(Long.MIN_VALUE)
            bitand()
            // res a a_MSB
            zeroNot()
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
                push(Long.MIN_VALUE)
                bitand()
                // res a<<1 (0 if MSB is not set)
                zeroNot()
                // res a<<1 (1 if MSB is not set)
            }
            // res a<<clz
            pop()
        }
    }


    fun ComplexFunction.i64PopCnt() = instruction("i64PopCnt", stackSizeChange = 0) {
        i64CountSetBits()
    }

    fun ComplexFunction.i64Eq() = instruction("i64Eq", stackSizeChange = -1) {
        cmp()
        zeroNot()
    }

    fun ComplexFunction.i64Ne() = instruction("i64Ne", stackSizeChange = -1) {
        cmp()
        zeroNot()
        zeroNotPositive()
    }

    fun ComplexFunction.i64Eqz() = instruction("i64Eqz", stackSizeChange = 0) {
        zeroNot()
    }

    private fun ComplexBlock.i64Lt() {
        i64Ge()
        zeroNotPositive()
    }

    private fun ComplexBlock.i64Gt() {
        i64Le()
        zeroNotPositive()
    }

    private fun ComplexBlock.i64Le() {
        // a b
        cmp()
        // sgn(a-b)
        // 1 if a > b, 0 if a = b, -1 if a < b
        zeroNotPositive()
        // 0 if a > b, 1 if a = b, 1 if a < b
    }

    private fun ComplexBlock.i64Ge() {
        // a b
        cmp()
        // sgn(a-b)
        // 1 if a > b, 0 if a = b, -1 if a < b
        negate()
        // -1 if a > b, 0 if a = b, 1 if a < b
        zeroNotPositive()
        // 1 if a > b, 1 if a = b, 0 if a < b
    }

    private fun ComplexBlock.u64Lt() {
        u64Ge()
        zeroNotPositive()
    }

    private fun ComplexBlock.u64Gt() {
        u64Le()
        zeroNotPositive()
    }

    private fun ComplexBlock.u64Le() {
        // a b
        cmpAsUnsigned()
        // sgn(a-b)
        // 1 if a > b, 0 if a = b, -1 if a < b
        zeroNotPositive()
        // 0 if a > b, 1 if a = b, 1 if a < b
    }

    private fun ComplexBlock.u64Ge() {
        // a b
        cmpAsUnsigned()
        // sgn(a-b)
        // 1 if a > b, 0 if a = b, -1 if a < b
        negate()
        // -1 if a > b, 0 if a = b, 1 if a < b
        zeroNotPositive()
        // 1 if a > b, 1 if a = b, 0 if a < b
    }

    private fun ComplexBlock.cmpAsUnsigned() = complexFunction("cmpAsUnsigned") {
        // a b
        dupSecond()
        // a b a
        push(Long.MIN_VALUE) // just msb set
        bitand()
        // a b a_MSB
        dupSecond()
        // a b a_MSB b
        push(Long.MIN_VALUE) // just msb set
        bitand()
        // a b a_MSB b_MSB
        ifZero {
            // a b a_MSB 0
            pop()
            // a b a_MSB
            ifZero {
                // a b 0
                pop()
                // this is two positive numbers, we can safely sgn(a-b)
                // a b
                negate()
                add()
                sgn()
                // sgn(a-b)
            } otherwise {
                // a b 1<<63
                // a > b
                push(1)
                // a b 1<<63 1
                pop2(); pop2(); pop2()
                // 1
            }
        } otherwise {
            // a b a_MSB 1<<63
            pop()
            // a b a_MSB
            ifZero {
                // a b 0
                // a < b
                push(-1)
                // a b 0 -1
                pop2(); pop2(); pop2()
                // -1
            } otherwise {
                // a b 1<<63
                // we need to mask out the MSB on both, then we can do sgn(a-b)
                inc()
                negate()
                // a b 0x7FFF...FFF
                bitand()
                // a b&0x7FFF...FFF
                swap2()
                // b&0x7FFF...FFF a
                push(Long.MAX_VALUE) // 0x7FFF...FFF
                bitand()
                // b&0x7FFF...FFF a&0x7FFF...FFF
                swap2()
                // a&0x7FFF...FFF b&0x7FFF...FFF
                negate()
                add()
                // a&0x7FFF...FFF-b&0x7FFF...FFF
                sgn()
                // sgn(a&0x7FFF...FFF-b&0x7FFF...FFF)
                // sgn(b-a)
            }
        }
    }

    fun ComplexFunction.i64LtUnsigned() = instruction("i64LtUnsigned", stackSizeChange = -1) {
        u64Lt()
    }

    fun ComplexFunction.i64LtSigned() = instruction("i64LtSigned", stackSizeChange = -1) {
        swap2()
        // b a
        i64Gt() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i64GtUnsigned() = instruction("i64GtUnsigned", stackSizeChange = -1) {
        u64Gt()
    }

    fun ComplexFunction.i64GtSigned() = instruction("i64GtSigned", stackSizeChange = -1) {
        swap2()
        // b a
        i64Lt() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i64LeUnsigned() = instruction("i64LeUnsigned", stackSizeChange = -1) {
        u64Le()
    }

    fun ComplexFunction.i64LeSigned() = instruction("i64LeSigned", stackSizeChange = -1) {
        swap2()
        // b a
        i64Ge() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i64GeUnsigned() = instruction("i64GeUnsigned", stackSizeChange = -1) {
        u64Ge()
    }

    fun ComplexFunction.i64GeSigned() = instruction("i64GeSigned", stackSizeChange = -1) {
        swap2()
        // b a
        i64Le() // we swapped the arguments, no swapping back (for perf)
    }

    fun ComplexFunction.i32Load() = instruction("i32Load", stackSizeChange = 0) { loadInt(4) }
    fun ComplexFunction.i64Load() = instruction("i64Load", stackSizeChange = 0) { loadInt(8) }
    fun ComplexFunction.load8Unsigned() = instruction("load8Unsigned", stackSizeChange = 0) { loadInt(1) }

    private fun ComplexFunction.loadInt(bytes: Int) = complexFunction("loadInt(${bytes}B)") {
        // TODO: Compare with implementation with rolls
        require(bytes >= 1)
        require(bytes <= 8) { "Can fit at most 8 bytes into a i64, got $bytes" }

        if (bytes == 1) {
            getFromMemory()
            return@complexFunction
        }

        val offsets = (0..<bytes).map { it * 8 }.reversed()

        // i
        getBytesFromMemory(bytes)
        // m[i] ... m[i+bytes]
        push(offsets.first()); bitshift()
        // m[i] ... m[i+bytes]<<offset
        lswap(); pop()
        // m[i] ...  ; lswap = m[i+bytes]<<offset
        // m[i] ...  ; lswap = sum
        offsets.drop(1).dropLast(1).forEach { shift ->
            push(shift); bitshift()
            // m[i] ... x<<shift ; lswap = sum
            CS(); lswap()
            // m[i] ... x<<shift sum ; lswap = CS
            add()
            // m[i] ... x<<shift+sum ; lswap = CS
            lswap(); pop()
            // m[i] ... ; lswap = x<<shift+sum
        }
        // m[i] ; lswap = sum
        CS(); lswap()
        add()
        // m[i]+...+m[i+bytes]<<bytes*8
    }

    /**
     * Signature: ```i -> m[i:i+count]```
     */
    private fun ComplexBlock.getBytesFromMemory(count: Int) = complexFunction("getBytesFromMemory(${count}B)") {
        require(count in 1..8) { "Can only get between 1 and 8 bytes from memory, got $count" }
        if (count == 1) {
            getFromMemory()
            return@complexFunction
        }

        // i
        repeat(count) {
            // i
            dup()
            // i i
            getFromMemory()
            // i m[i]
            swap2()
            // m[i] i
            inc()
            // m[i] i+1
        }
        // m[i:i+count] i+count
        pop()
        // m[i:i+count]
    }

    private fun ComplexBlock.getFromMemory() {
        call(globalState.getMemoryFunction(), inline = CallInline.ALWAYS)
    }

    fun ComplexFunction.popLocals() {
        check(!localsPopped) { "Locals have already been popped in this scope" }
        // There should be only return values + locals on the stack now.
        check(intermediateStackValues == returnTypes.size) {
            "Inconsistent stack size: expected ${returnTypes.size} values on top of locals, got $intermediateStackValues values"
        }

        complexFunction("popLocals(${localTypes.size},${returnTypes.size})") {
            roll((localTypes.size + returnTypes.size).toLong(), returnTypes.size.toLong())
            repeat(localTypes.size) {
                pop()
            }
        }
        localsPopped = true
    }

    companion object {
        fun ComplexFunction.initialize(
            params: List<ValType>,
            localTypes: List<ValType>,
            returns: List<ValType>,
            state: ModuleTranslatorState
        ): WasmFunctionScope {
            require(returns.all { it in listOf(ValType.I32, ValType.I64, ValType.F32, ValType.F64) }) {
                "Unsupported return type(s) found in $returns"
            }

            val scope = WasmFunctionScope(params + localTypes, returns, state)
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

