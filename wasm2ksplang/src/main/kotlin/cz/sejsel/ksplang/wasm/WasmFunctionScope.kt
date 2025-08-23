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


    // TODO: Find last local usage in function, and instead of duplicating, we can consume it.
    fun ComplexFunction.getLocal(index: Int) {
        check(!localsPopped)
        dupLocal(index)
    }

    fun ComplexFunction.i32Add() {
        check(!localsPopped)
        add()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32Sub() {
        check(!localsPopped)
        sub()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32Mul() {
        check(!localsPopped)
        // Unfortunately, we only have signed i64 multiplication, so we can overflow
        // even with multiplication of two i32 values.

        TODO("Not yet implemented due to overflows") // See Hacker's delight TABLE 2–2. OVERFLOW TEST FOR SIGNED MULTIPLICATION onwards, we can have a fast case

        /*
        mul()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
        */
    }

    fun ComplexFunction.i32DivSigned() {
        check(!localsPopped)
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        div()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32DivUnsigned() {
        check(!localsPopped)
        swap2()
        div()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32RemSigned() {
        check(!localsPopped)
        // a b
        i32ToSigned()
        // a b_s
        swap2()
        i32ToSigned()
        // b_s a_s
        REM()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
    }

    fun ComplexFunction.i32RemUnsigned() {
        check(!localsPopped)
        swap2()
        REM()
        intermediateStackValues -= 1

        push(I32_MOD)
        swap2()
        modulo()
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

