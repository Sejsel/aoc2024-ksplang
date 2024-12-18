package cz.sejsel.ksplang.dsl.auto

import cz.sejsel.ksplang.dsl.auto.AutoBlock
import cz.sejsel.ksplang.dsl.core.ComplexBlock
import cz.sejsel.ksplang.dsl.core.KsplangMarker
import cz.sejsel.ksplang.dsl.core.doWhileNonZero
import cz.sejsel.ksplang.std.dupKthZeroIndexed
import cz.sejsel.ksplang.std.popKth
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.setKth


data class Variable(val name: String, val parentAutoBlock: AutoBlock)

class CallResultProcessor(val auto: AutoBlock, val resultCount: Int) {
    fun setTo(v: Variable, resultIndex: Long) {
        require(resultIndex >= 0)
        require(resultIndex < resultCount)
        // [stack] [vars] [results]
        auto.block.dupKthZeroIndexed(resultCount - resultIndex - 1)
        // [stack] [vars] [results] result[i]
        val targetVarIndex = auto.variables.indexOf(v)
        auto.block.setKth(auto.variables.size - targetVarIndex + resultCount)
        // [stack] [vars] [results]
    }

    fun clearResults() {
        repeat(resultCount) {
            auto.block.pop()
        }
    }
}

interface RestrictedAutoBlock {
    fun call1(var1: Variable, call: ComplexBlock.() -> Unit): CallResult1
    fun call1(var1: Variable, var2: Variable, call: ComplexBlock.() -> Unit): CallResult1
    fun call1(var1: Variable, var2: Variable, var3: Variable, call: ComplexBlock.() -> Unit): CallResult1

    fun doWhileNonZero(checkedVariable: Variable, inner: RestrictedAutoBlock.() -> Unit)
}

@KsplangMarker
class AutoBlock(initVariableNames: List<String>, internal var block: ComplexBlock) : RestrictedAutoBlock {
    init {
        require(initVariableNames.toSet().size == initVariableNames.size) { "Variable names must be unique" }
    }

    internal val variables = initVariableNames.map { Variable(it, this) }.toMutableList()


    /** Create a new stack variable */
    fun variable(name: String, defaultValue: Long = 0): Variable {
        require(name !in variables.map { it.name }) { "Variable $name already exists" }
        val variable = Variable(name, this)
        variables.add(variable)

        block.push(defaultValue)

        return variable
    }

    private fun dupVars(vars: List<Variable>) {
        // [stack]
        vars.forEachIndexed { i, variable ->
            val index = variables.indexOf(variable)
            if (index == -1) {
                throw IllegalArgumentException("Variable $variable not found")
            }
            block.dupKthZeroIndexed(variables.size - index - 1 + i)
            // [stack] ... variable
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

    override fun call1(var1: Variable, call: ComplexBlock.() -> Unit): CallResult1 {
        require(var1 in variables) { "Variable $var1 not found" }

        dupVars(listOf(var1))
        call(block)
        return CallResult1(CallResultProcessor(this, 1))
    }

    override fun call1(var1: Variable, var2: Variable, call: ComplexBlock.() -> Unit): CallResult1 {
        require(var1 in variables) { "Variable $var1 not found" }
        require(var2 in variables) { "Variable $var2 not found" }

        dupVars(listOf(var1, var2))
        call(block)
        return CallResult1(CallResultProcessor(this, 1))
    }

    override fun call1(var1: Variable, var2: Variable, var3: Variable, call: ComplexBlock.() -> Unit): CallResult1 {
        require(var1 in variables) { "Variable $var1 not found" }
        require(var2 in variables) { "Variable $var2 not found" }
        require(var3 in variables) { "Variable $var3 not found" }

        dupVars(listOf(var1, var2, var3))
        call(block)
        return CallResult1(CallResultProcessor(this, 1))
    }

    private fun withBlock(innerBlock: ComplexBlock, inner: AutoBlock.() -> Unit) {
        var outerBlock = this.block
        this.block = innerBlock
        inner()
        this.block = outerBlock
    }

    override fun doWhileNonZero(checkedVariable: Variable, inner: RestrictedAutoBlock.() -> Unit) {
        require(checkedVariable in variables) { "Variable $checkedVariable not found" }

        block.doWhileNonZero {
            this@AutoBlock.withBlock(this) {
                inner()
                dupVars(listOf(checkedVariable))
            }
        }
    }
}


fun ComplexBlock.auto(block: AutoBlock.() -> Unit) {
    val autoBlock = AutoBlock(emptyList(), this)
    autoBlock.block()
}

fun ComplexBlock.auto(
    name1: String,
    block: AutoBlock.(Variable) -> Unit
) {
    val vars = listOf(name1)
    val autoBlock = AutoBlock(vars, this)
    autoBlock.block(
        autoBlock.variables[0],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    block: AutoBlock.(Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2)
    val autoBlock = AutoBlock(vars, this)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
    )
}

fun ComplexBlock.auto(
    name1: String,
    name2: String,
    name3: String,
    block: AutoBlock.(Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3)
    val autoBlock = AutoBlock(vars, this)
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
    block: AutoBlock.(Variable, Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3, name4)
    val autoBlock = AutoBlock(vars, this)
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
    block: AutoBlock.(Variable, Variable, Variable, Variable, Variable) -> Unit
) {
    val vars = listOf(name1, name2, name3, name4, name5)
    val autoBlock = AutoBlock(vars, this)
    autoBlock.block(
        autoBlock.variables[0],
        autoBlock.variables[1],
        autoBlock.variables[2],
        autoBlock.variables[3],
        autoBlock.variables[4],
    )
}
fun RestrictedAutoBlock.runFun(num1: Variable, num2: Variable, num3: Variable, useResult: CallResult1.() -> Unit, functionCode: ComplexBlock.() -> Unit) {
    val result = call1(num1, num2, num3, functionCode)
    useResult(result)
    result.clear()
}

fun RestrictedAutoBlock.runFun(num1: Variable, num2: Variable, useResult: CallResult1.() -> Unit, functionCode: ComplexBlock.() -> Unit) {
    val result = call1(num1, num2, functionCode)
    useResult(result)
    result.clear()
}

fun RestrictedAutoBlock.runFun(num1: Variable, useResult: CallResult1.() -> Unit, functionCode: ComplexBlock.() -> Unit) {
    val result = call1(num1, functionCode)
    useResult(result)
    result.clear()
}
