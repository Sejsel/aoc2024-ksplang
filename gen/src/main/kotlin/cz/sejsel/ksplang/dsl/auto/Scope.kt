package cz.sejsel.ksplang.dsl.auto

import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.ComplexFunction
import cz.sejsel.ksplang.dsl.core.IfZero
import cz.sejsel.ksplang.dsl.core.KsplangMarker
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.dsl.core.ifZero
import cz.sejsel.ksplang.dsl.core.whileNonZero
import cz.sejsel.ksplang.std.auto.copy
import cz.sejsel.ksplang.std.auto.dec
import cz.sejsel.ksplang.std.auto.max2
import cz.sejsel.ksplang.std.auto.sgn
import cz.sejsel.ksplang.std.auto.subabs
import cz.sejsel.ksplang.std.dupKthZeroIndexed
import cz.sejsel.ksplang.std.popKth
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.roll
import cz.sejsel.ksplang.std.zeroNot


sealed interface Parameter

data class Variable(val name: String, val ownerScope: Scope) : Parameter
data class Constant(val value: Long) : Parameter

fun const(value: Int): Constant = const(value.toLong())
fun const(value: Long): Constant = Constant(value)

val Int.const: Constant get() = const(this)
val Long.const: Constant get() = const(this)


@KsplangMarker
class Scope(initVariableNames: List<String>, internal var block: ComplexBlock, private val parent: Scope?) {
    init {
        require(initVariableNames.toSet().size == initVariableNames.size) { "Variable names must be unique" }
    }

    internal val variables = initVariableNames.map { Variable(it, this) }.toMutableList()

    /** Create a new stack variable */
    fun variable(name: String? = null, defaultValue: Long = 0): Variable {
        if (name == null) {
            return variable("anon${variables.size}_${randomVarSuffix()}", defaultValue)
        }

        require(name !in variables.map { it.name }) { "Variable $name already exists" }
        val variable = Variable(name, this)
        variables.add(variable)

        block.push(defaultValue)

        return variable
    }

    /** Create a new stack variable */
    fun variable(defaultValue: Int) = variable(null, defaultValue.toLong())

    /** Create a new stack variable */
    fun variable(defaultValue: Long = 0) = variable(null, defaultValue)

    /** Create a new stack variable */
    fun variable(defaultValue: Constant) = variable(null, defaultValue.value)

    /** Create a new stack variable. False is 0, true is 1. */
    fun variable(defaultValue: Boolean) = variable(null, defaultValue = if (defaultValue) 1 else 0)
    /**
     * Adopt non-tracked values on top of the stack as variables (while the stack layout invariant is broken).
     * Mainly used to capture results of function calls and similar.
     */
    internal fun adoptVariables(vararg names: String): List<Variable> {
        require(names.all { it !in variables.map { it.name } }) { "Variable names must be unique" }
        val newVariables = names.map { Variable(it, this) }
        variables.addAll(newVariables)
        return newVariables
    }

    internal fun findIndexFromTop(variable: Variable): Int {
        // We check this scope and if it's not there, we check the parent until the root scope is reached
        var above = 0
        var current = this
        var indexInCurrent = -1
        while (true) {
            val index = current.variables.indexOf(variable)
            if (index == -1) {
                // not found, try parent
                above += current.variables.size
                current = current.parent ?: break
            } else {
                // found
                indexInCurrent = index
                break
            }
        }
        check(indexInCurrent != -1) { "Variable $variable not found" }

        above += current.variables.size - indexInCurrent - 1
        return above
    }

    /**
     * Prepares the provided parameters on top of the stack, for use by another scope.
     * This breaks the stack variable layout invariant. Mainly a building block for function calls and similar.
     */
    internal fun prepareParams(params: List<Parameter>) {
        // [stack]
        params.forEachIndexed { i, parameter ->
            when (parameter) {
                is Constant -> {
                    block.push(parameter.value)
                    // [stack] ... constant
                }

                is Variable -> {
                    val index = findIndexFromTop(parameter)
                    block.dupKthZeroIndexed(index + i)
                    // [stack] ... variable
                }
            }
        }
        // [stack] [vars]
    }

    fun keepOnly(vararg vars: Variable) {
        require(vars.all { it in variables }) { "All variables must be registered" }

        val varsToKeep = vars.toSet()
        // Iterate from the back
        for (i in variables.size - 1 downTo 0) {
            if (variables[i] !in varsToKeep) {
                val varsAfter = variables.size - i - 1
                block.popKth(varsAfter + 1L)
                variables.removeAt(i)
            }
        }
    }

    /** Removes all variables from the stack */
    internal fun removeAllVariables() {
        // Iterate from the back
        for (i in variables.size - 1 downTo 0) {
            block.pop()
            variables.removeAt(i)
        }
    }

    /** Do while the checked variable is non-zero (or true, for bools) */
    fun doWhileNonZero(checkedVariable: Scope.() -> Variable, inner: Scope.() -> Unit) {
        block.doWhileNonZero {
            val innerScope = Scope(emptyList(), block = this, parent = this@Scope)
            innerScope.inner()
            innerScope.removeAllVariables()

            val checkScope = Scope(emptyList(), block = this, parent = this@Scope)
            val checkedVar = checkScope.checkedVariable()
            if (checkedVar.ownerScope == checkScope) {
                checkScope.keepOnly(checkedVar)
            } else {
                checkScope.removeAllVariables()
                checkScope.prepareParams(listOf(checkedVar))
            }
        }
    }

    /** If checked variable is non-zero, execute the inner block */
    fun ifBool(checkedVariable: Scope.() -> Variable, inner: Scope.() -> Unit): IfBool {
        val checkScope = Scope(emptyList(), block = block, parent = this@Scope)
        val checkedVar = checkScope.checkedVariable()
        if (checkedVar.ownerScope == checkScope) {
            checkScope.keepOnly(checkedVar)
        } else {
            checkScope.removeAllVariables()
            checkScope.prepareParams(listOf(checkedVar))
        }
        block.zeroNot()
        val ifZero = block.ifZero {
            pop() // Pop the checked variable

            val innerScope = Scope(emptyList(), block = this, parent = this@Scope)
            innerScope.inner()
            innerScope.removeAllVariables()
        }

        // Default for else branch is just a pop of the checked variable.
        ifZero.orElse = buildComplexFunction { pop() }

        return IfBool(this, ifZero)
    }

    /** If checked variable is non-zero, execute the inner block */
    fun ifBool(checkedVariable: Variable, inner: Scope.() -> Unit): IfBool {
        prepareParams(listOf(checkedVariable))
        block.zeroNot()
        val ifZero = block.ifZero {
            pop() // Pop the checked variable

            val innerScope = Scope(emptyList(), block = this, parent = this@Scope)
            innerScope.inner()
            innerScope.removeAllVariables()
        }

        // Default for else branch is just a pop of the checked variable.
        ifZero.orElse = buildComplexFunction { pop() }

        return IfBool(this, ifZero)
    }

    /**
     * Runs the inner block while the checked variable is non-zero (or true, for bools).
     */
    fun whileNonZero(checkedVariable: Scope.() -> Variable, inner: Scope.() -> Unit) {
        fun emitCheckedVariable(block: ComplexBlock) {
            val checkScope = Scope(emptyList(), block = block, parent = this@Scope)
            val checkedVar = checkScope.checkedVariable()
            if (checkedVar.ownerScope == checkScope) {
                checkScope.keepOnly(checkedVar)
            } else {
                checkScope.removeAllVariables()
                checkScope.prepareParams(listOf(checkedVar))
            }
        }

        emitCheckedVariable(block = block)

        block.whileNonZero {
            // remove the checked variable
            pop()

            val innerScope = Scope(emptyList(), block = this, parent = this@Scope)
            innerScope.inner()
            innerScope.removeAllVariables()

            emitCheckedVariable(block = this)
        }
    }

    // private because this is easy to use wrong - if called with a function call,
    // it would not get rerun every iteration. The lambda overload will do that.
    private fun whileNonZero(checkedVariable: Variable, inner: Scope.() -> Unit) {
        prepareParams(listOf(checkedVariable))

        block.whileNonZero {
            // remove the checked variable
            pop()

            val innerScope = Scope(emptyList(), block = this, parent = this@Scope)
            innerScope.inner()
            innerScope.removeAllVariables()

            innerScope.prepareParams(listOf(checkedVariable))
        }
    }

    /**
     * Runs the inner block n times, not changing the value of n. If n is zero, the inner block is not run at all.
     *
     * Passes a variable i to the inner block, which **starts at n-1 and goes down to 0**, last iteration has i = 0.
     */
    fun doNTimes(n: Variable, inner: Scope.(i: Variable) -> Unit) {
        val count = max2(n, const(0))
        whileNonZero(count) {
            set(count) to dec(count)
            val i = dec(subabs(n, count))
            inner(i)
        }
    }
}

data class IfBool(private val parentScope: Scope, private val ifZero: IfZero) {
    infix fun otherwise(inner: Scope.() -> Unit) {
        ifZero.otherwise {
            pop() // Pop the checked variable

            val innerScope = Scope(emptyList(), block = this, parent = parentScope)
            innerScope.inner()
            innerScope.removeAllVariables()
        }
    }
}

infix fun IfZero.otherwise(init: ComplexFunction.() -> Unit) {
    val f = ComplexFunction()
    f.init()
    this.orElse = f
}


fun ComplexBlock.auto(block: Scope.() -> Unit) {
    val autoBlock = Scope(emptyList(), this, parent = null)
    autoBlock.block()
}

fun ComplexBlock.auto(
    name1: String,
    block: Scope.(Variable) -> Unit
) {
    val vars = listOf(name1)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    block: Scope.(Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    name3: String,
    block: Scope.(Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
        autoBlock.variables[2],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    name3: String,
    name4: String,
    block: Scope.(Variable, Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3, name4)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
        autoBlock.variables[2],
        autoBlock.variables[3],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    name3: String,
    name4: String,
    name5: String,
    block: Scope.(Variable, Variable, Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3, name4, name5)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
        autoBlock.variables[2],
        autoBlock.variables[3],
        autoBlock.variables[4],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    name3: String,
    name4: String,
    name5: String,
    name6: String,
    block: Scope.(Variable, Variable, Variable, Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3, name4, name5, name6)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
        autoBlock.variables[2],
        autoBlock.variables[3],
        autoBlock.variables[4],
        autoBlock.variables[5],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    name3: String,
    name4: String,
    name5: String,
    name6: String,
    name7: String,
    block: Scope.(Variable, Variable, Variable, Variable, Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3, name4, name5, name6, name7)
    val autoBlock = Scope(vars, this, parent = null)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
        autoBlock.variables[2],
        autoBlock.variables[3],
        autoBlock.variables[4],
        autoBlock.variables[5],
        autoBlock.variables[6],
    )
}


data class VarSetter(val scope: Scope, val variable: Variable, val executeAfter: Scope.() -> Unit = {}) {
    infix fun to(parameter: Parameter) {
        var toIndex = scope.findIndexFromTop(variable)
        scope.prepareParams(listOf(parameter))

        // to [vars] val
        scope.block.roll(toIndex + 2L, 1)
        // val to [vars]
        scope.block.popKth(toIndex + 1L)

        executeAfter(scope)
    }

    infix fun to(const: Long) {
        to(const(const))
    }

    infix fun to(const: Int) {
        to(const(const))
    }

    infix fun to(const: Boolean) {
        to(const(if (const) 1 else 0))
    }
}

fun Scope.set(variable: Variable): VarSetter {
    return VarSetter(this, variable)
}

fun Scope.runFun0(vararg params: Parameter, functionCode: ComplexBlock.() -> Unit) {
    prepareParams(params.toList())
    block.functionCode()
}

fun Scope.runFun1(vararg params: Parameter, functionCode: ComplexBlock.() -> Unit): Variable {
    prepareParams(params.toList())
    block.functionCode()

    val vars = adoptVariables("runFun1Result${randomVarSuffix()}")
    return vars.single()
}

fun Scope.runFun2(vararg params: Parameter, functionCode: ComplexBlock.() -> Unit): Pair<Variable, Variable> {
    prepareParams(params.toList())
    block.functionCode()

    val vars = adoptVariables("runFun2Result1${randomVarSuffix()}", "runFun2Result2${randomVarSuffix()}")
    check(vars.size == 2)
    return vars[0] to vars[1]
}

private fun randomVarSuffix(): String = java.util.UUID.randomUUID().toString()