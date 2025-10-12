package cz.sejsel.ksplang.dsl.core

/**
 * A ksplang program with callable functions.
 *
 * Note that in many cases, you can use [ComplexFunction] directly (through [buildComplexFunction]),
 * as long as you don't need to call functions, and inlining them is good enough for you.
 * On the other hand, using this class allows for recursion.
 */
@KsplangMarker
class KsplangProgramBuilder {
    var body: ComplexFunction? = null
        private set

    private val functions: MutableMap<String, ProgramFunctionBase> = mutableMapOf()

    fun hasFunction(functionName: String): Boolean {
        return functions.containsKey(functionName)
    }

    fun installFunction(function: ProgramFunctionBase) {
        require(function.name.isNotEmpty()) { "Function name cannot be empty." }
        require(!functions.containsKey(function.name)) { "Function '${function.name}' is already defined." }

        functions[function.name] = function
    }

    fun body(init: ComplexFunction.() -> Unit) {
        require(body == null) { "Body can only be set once." }

        val block = ComplexFunction("ksplang_program_body")
        block.init()
        body = block
    }

    fun build(): KsplangProgram {
        requireNotNull(body) { "Body of the program must be defined." }
        functions.forEach { (name, function) ->
            requireNotNull(function.body) { "Forward-declared function '${name}' does not have a body." }
        }
        return KsplangProgram(body!!, functions.values.toList())
    }

    fun function(name: String, args: Int, outputs: Int, init: ComplexFunction.() -> Unit): ProgramFunction {
        val programFunction = ProgramFunction(name, args, outputs, null)
        programFunction.setBody(init)

        installFunction(programFunction)
        return programFunction
    }

    fun function(name: String, args: Int, outputs: Int, body: ComplexFunction): ProgramFunction {
        val programFunction = ProgramFunction(name, args, outputs, null)
        programFunction.setBody(body)

        installFunction(programFunction)
        return programFunction
    }

    /**
     * Creates a forward-declared function that can be defined later.
     * This is useful for recursive functions or when you want to define the function body later.
     */
    fun function(name: String, args: Int, outputs: Int): ProgramFunction {
        val programFunction = ProgramFunction(name, args, outputs, null)
        installFunction(programFunction)
        return programFunction
    }

    fun function0To0(name: String, body: ComplexBlock? = null): ProgramFunction0To0 {
        val function = ProgramFunction0To0(name, body)
        installFunction(function)
        return function
    }
    fun function0To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To0 {
        val function = ProgramFunction0To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To0(name: String): ProgramFunction0To0 {
        val function = ProgramFunction0To0(name, null)
        installFunction(function)
        return function
    }

    fun function0To1(name: String, body: ComplexBlock? = null): ProgramFunction0To1 {
        val function = ProgramFunction0To1(name, body)
        installFunction(function)
        return function
    }
    fun function0To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To1 {
        val function = ProgramFunction0To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To1(name: String): ProgramFunction0To1 {
        val function = ProgramFunction0To1(name, null)
        installFunction(function)
        return function
    }

    fun function0To2(name: String, body: ComplexBlock? = null): ProgramFunction0To2 {
        val function = ProgramFunction0To2(name, body)
        installFunction(function)
        return function
    }
    fun function0To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To2 {
        val function = ProgramFunction0To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To2(name: String): ProgramFunction0To2 {
        val function = ProgramFunction0To2(name, null)
        installFunction(function)
        return function
    }

    fun function0To3(name: String, body: ComplexBlock? = null): ProgramFunction0To3 {
        val function = ProgramFunction0To3(name, body)
        installFunction(function)
        return function
    }
    fun function0To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To3 {
        val function = ProgramFunction0To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To3(name: String): ProgramFunction0To3 {
        val function = ProgramFunction0To3(name, null)
        installFunction(function)
        return function
    }

    fun function0To4(name: String, body: ComplexBlock? = null): ProgramFunction0To4 {
        val function = ProgramFunction0To4(name, body)
        installFunction(function)
        return function
    }
    fun function0To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To4 {
        val function = ProgramFunction0To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To4(name: String): ProgramFunction0To4 {
        val function = ProgramFunction0To4(name, null)
        installFunction(function)
        return function
    }

    fun function0To5(name: String, body: ComplexBlock? = null): ProgramFunction0To5 {
        val function = ProgramFunction0To5(name, body)
        installFunction(function)
        return function
    }
    fun function0To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To5 {
        val function = ProgramFunction0To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To5(name: String): ProgramFunction0To5 {
        val function = ProgramFunction0To5(name, null)
        installFunction(function)
        return function
    }

    fun function0To6(name: String, body: ComplexBlock? = null): ProgramFunction0To6 {
        val function = ProgramFunction0To6(name, body)
        installFunction(function)
        return function
    }
    fun function0To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To6 {
        val function = ProgramFunction0To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To6(name: String): ProgramFunction0To6 {
        val function = ProgramFunction0To6(name, null)
        installFunction(function)
        return function
    }

    fun function0To7(name: String, body: ComplexBlock? = null): ProgramFunction0To7 {
        val function = ProgramFunction0To7(name, body)
        installFunction(function)
        return function
    }
    fun function0To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To7 {
        val function = ProgramFunction0To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To7(name: String): ProgramFunction0To7 {
        val function = ProgramFunction0To7(name, null)
        installFunction(function)
        return function
    }

    fun function0To8(name: String, body: ComplexBlock? = null): ProgramFunction0To8 {
        val function = ProgramFunction0To8(name, body)
        installFunction(function)
        return function
    }
    fun function0To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction0To8 {
        val function = ProgramFunction0To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function0To8(name: String): ProgramFunction0To8 {
        val function = ProgramFunction0To8(name, null)
        installFunction(function)
        return function
    }

    fun function1To0(name: String, body: ComplexBlock? = null): ProgramFunction1To0 {
        val function = ProgramFunction1To0(name, body)
        installFunction(function)
        return function
    }
    fun function1To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To0 {
        val function = ProgramFunction1To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To0(name: String): ProgramFunction1To0 {
        val function = ProgramFunction1To0(name, null)
        installFunction(function)
        return function
    }

    fun function1To1(name: String, body: ComplexBlock? = null): ProgramFunction1To1 {
        val function = ProgramFunction1To1(name, body)
        installFunction(function)
        return function
    }
    fun function1To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To1 {
        val function = ProgramFunction1To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To1(name: String): ProgramFunction1To1 {
        val function = ProgramFunction1To1(name, null)
        installFunction(function)
        return function
    }

    fun function1To2(name: String, body: ComplexBlock? = null): ProgramFunction1To2 {
        val function = ProgramFunction1To2(name, body)
        installFunction(function)
        return function
    }
    fun function1To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To2 {
        val function = ProgramFunction1To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To2(name: String): ProgramFunction1To2 {
        val function = ProgramFunction1To2(name, null)
        installFunction(function)
        return function
    }

    fun function1To3(name: String, body: ComplexBlock? = null): ProgramFunction1To3 {
        val function = ProgramFunction1To3(name, body)
        installFunction(function)
        return function
    }
    fun function1To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To3 {
        val function = ProgramFunction1To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To3(name: String): ProgramFunction1To3 {
        val function = ProgramFunction1To3(name, null)
        installFunction(function)
        return function
    }

    fun function1To4(name: String, body: ComplexBlock? = null): ProgramFunction1To4 {
        val function = ProgramFunction1To4(name, body)
        installFunction(function)
        return function
    }
    fun function1To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To4 {
        val function = ProgramFunction1To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To4(name: String): ProgramFunction1To4 {
        val function = ProgramFunction1To4(name, null)
        installFunction(function)
        return function
    }

    fun function1To5(name: String, body: ComplexBlock? = null): ProgramFunction1To5 {
        val function = ProgramFunction1To5(name, body)
        installFunction(function)
        return function
    }
    fun function1To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To5 {
        val function = ProgramFunction1To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To5(name: String): ProgramFunction1To5 {
        val function = ProgramFunction1To5(name, null)
        installFunction(function)
        return function
    }

    fun function1To6(name: String, body: ComplexBlock? = null): ProgramFunction1To6 {
        val function = ProgramFunction1To6(name, body)
        installFunction(function)
        return function
    }
    fun function1To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To6 {
        val function = ProgramFunction1To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To6(name: String): ProgramFunction1To6 {
        val function = ProgramFunction1To6(name, null)
        installFunction(function)
        return function
    }

    fun function1To7(name: String, body: ComplexBlock? = null): ProgramFunction1To7 {
        val function = ProgramFunction1To7(name, body)
        installFunction(function)
        return function
    }
    fun function1To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To7 {
        val function = ProgramFunction1To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To7(name: String): ProgramFunction1To7 {
        val function = ProgramFunction1To7(name, null)
        installFunction(function)
        return function
    }

    fun function1To8(name: String, body: ComplexBlock? = null): ProgramFunction1To8 {
        val function = ProgramFunction1To8(name, body)
        installFunction(function)
        return function
    }
    fun function1To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction1To8 {
        val function = ProgramFunction1To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function1To8(name: String): ProgramFunction1To8 {
        val function = ProgramFunction1To8(name, null)
        installFunction(function)
        return function
    }

    fun function2To0(name: String, body: ComplexBlock? = null): ProgramFunction2To0 {
        val function = ProgramFunction2To0(name, body)
        installFunction(function)
        return function
    }
    fun function2To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To0 {
        val function = ProgramFunction2To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To0(name: String): ProgramFunction2To0 {
        val function = ProgramFunction2To0(name, null)
        installFunction(function)
        return function
    }

    fun function2To1(name: String, body: ComplexBlock? = null): ProgramFunction2To1 {
        val function = ProgramFunction2To1(name, body)
        installFunction(function)
        return function
    }
    fun function2To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To1 {
        val function = ProgramFunction2To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To1(name: String): ProgramFunction2To1 {
        val function = ProgramFunction2To1(name, null)
        installFunction(function)
        return function
    }

    fun function2To2(name: String, body: ComplexBlock? = null): ProgramFunction2To2 {
        val function = ProgramFunction2To2(name, body)
        installFunction(function)
        return function
    }
    fun function2To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To2 {
        val function = ProgramFunction2To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To2(name: String): ProgramFunction2To2 {
        val function = ProgramFunction2To2(name, null)
        installFunction(function)
        return function
    }

    fun function2To3(name: String, body: ComplexBlock? = null): ProgramFunction2To3 {
        val function = ProgramFunction2To3(name, body)
        installFunction(function)
        return function
    }
    fun function2To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To3 {
        val function = ProgramFunction2To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To3(name: String): ProgramFunction2To3 {
        val function = ProgramFunction2To3(name, null)
        installFunction(function)
        return function
    }

    fun function2To4(name: String, body: ComplexBlock? = null): ProgramFunction2To4 {
        val function = ProgramFunction2To4(name, body)
        installFunction(function)
        return function
    }
    fun function2To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To4 {
        val function = ProgramFunction2To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To4(name: String): ProgramFunction2To4 {
        val function = ProgramFunction2To4(name, null)
        installFunction(function)
        return function
    }

    fun function2To5(name: String, body: ComplexBlock? = null): ProgramFunction2To5 {
        val function = ProgramFunction2To5(name, body)
        installFunction(function)
        return function
    }
    fun function2To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To5 {
        val function = ProgramFunction2To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To5(name: String): ProgramFunction2To5 {
        val function = ProgramFunction2To5(name, null)
        installFunction(function)
        return function
    }

    fun function2To6(name: String, body: ComplexBlock? = null): ProgramFunction2To6 {
        val function = ProgramFunction2To6(name, body)
        installFunction(function)
        return function
    }
    fun function2To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To6 {
        val function = ProgramFunction2To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To6(name: String): ProgramFunction2To6 {
        val function = ProgramFunction2To6(name, null)
        installFunction(function)
        return function
    }

    fun function2To7(name: String, body: ComplexBlock? = null): ProgramFunction2To7 {
        val function = ProgramFunction2To7(name, body)
        installFunction(function)
        return function
    }
    fun function2To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To7 {
        val function = ProgramFunction2To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To7(name: String): ProgramFunction2To7 {
        val function = ProgramFunction2To7(name, null)
        installFunction(function)
        return function
    }

    fun function2To8(name: String, body: ComplexBlock? = null): ProgramFunction2To8 {
        val function = ProgramFunction2To8(name, body)
        installFunction(function)
        return function
    }
    fun function2To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction2To8 {
        val function = ProgramFunction2To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function2To8(name: String): ProgramFunction2To8 {
        val function = ProgramFunction2To8(name, null)
        installFunction(function)
        return function
    }

    fun function3To0(name: String, body: ComplexBlock? = null): ProgramFunction3To0 {
        val function = ProgramFunction3To0(name, body)
        installFunction(function)
        return function
    }
    fun function3To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To0 {
        val function = ProgramFunction3To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To0(name: String): ProgramFunction3To0 {
        val function = ProgramFunction3To0(name, null)
        installFunction(function)
        return function
    }

    fun function3To1(name: String, body: ComplexBlock? = null): ProgramFunction3To1 {
        val function = ProgramFunction3To1(name, body)
        installFunction(function)
        return function
    }
    fun function3To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To1 {
        val function = ProgramFunction3To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To1(name: String): ProgramFunction3To1 {
        val function = ProgramFunction3To1(name, null)
        installFunction(function)
        return function
    }

    fun function3To2(name: String, body: ComplexBlock? = null): ProgramFunction3To2 {
        val function = ProgramFunction3To2(name, body)
        installFunction(function)
        return function
    }
    fun function3To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To2 {
        val function = ProgramFunction3To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To2(name: String): ProgramFunction3To2 {
        val function = ProgramFunction3To2(name, null)
        installFunction(function)
        return function
    }

    fun function3To3(name: String, body: ComplexBlock? = null): ProgramFunction3To3 {
        val function = ProgramFunction3To3(name, body)
        installFunction(function)
        return function
    }
    fun function3To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To3 {
        val function = ProgramFunction3To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To3(name: String): ProgramFunction3To3 {
        val function = ProgramFunction3To3(name, null)
        installFunction(function)
        return function
    }

    fun function3To4(name: String, body: ComplexBlock? = null): ProgramFunction3To4 {
        val function = ProgramFunction3To4(name, body)
        installFunction(function)
        return function
    }
    fun function3To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To4 {
        val function = ProgramFunction3To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To4(name: String): ProgramFunction3To4 {
        val function = ProgramFunction3To4(name, null)
        installFunction(function)
        return function
    }

    fun function3To5(name: String, body: ComplexBlock? = null): ProgramFunction3To5 {
        val function = ProgramFunction3To5(name, body)
        installFunction(function)
        return function
    }
    fun function3To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To5 {
        val function = ProgramFunction3To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To5(name: String): ProgramFunction3To5 {
        val function = ProgramFunction3To5(name, null)
        installFunction(function)
        return function
    }

    fun function3To6(name: String, body: ComplexBlock? = null): ProgramFunction3To6 {
        val function = ProgramFunction3To6(name, body)
        installFunction(function)
        return function
    }
    fun function3To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To6 {
        val function = ProgramFunction3To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To6(name: String): ProgramFunction3To6 {
        val function = ProgramFunction3To6(name, null)
        installFunction(function)
        return function
    }

    fun function3To7(name: String, body: ComplexBlock? = null): ProgramFunction3To7 {
        val function = ProgramFunction3To7(name, body)
        installFunction(function)
        return function
    }
    fun function3To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To7 {
        val function = ProgramFunction3To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To7(name: String): ProgramFunction3To7 {
        val function = ProgramFunction3To7(name, null)
        installFunction(function)
        return function
    }

    fun function3To8(name: String, body: ComplexBlock? = null): ProgramFunction3To8 {
        val function = ProgramFunction3To8(name, body)
        installFunction(function)
        return function
    }
    fun function3To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction3To8 {
        val function = ProgramFunction3To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function3To8(name: String): ProgramFunction3To8 {
        val function = ProgramFunction3To8(name, null)
        installFunction(function)
        return function
    }

    fun function4To0(name: String, body: ComplexBlock? = null): ProgramFunction4To0 {
        val function = ProgramFunction4To0(name, body)
        installFunction(function)
        return function
    }
    fun function4To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To0 {
        val function = ProgramFunction4To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To0(name: String): ProgramFunction4To0 {
        val function = ProgramFunction4To0(name, null)
        installFunction(function)
        return function
    }

    fun function4To1(name: String, body: ComplexBlock? = null): ProgramFunction4To1 {
        val function = ProgramFunction4To1(name, body)
        installFunction(function)
        return function
    }
    fun function4To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To1 {
        val function = ProgramFunction4To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To1(name: String): ProgramFunction4To1 {
        val function = ProgramFunction4To1(name, null)
        installFunction(function)
        return function
    }

    fun function4To2(name: String, body: ComplexBlock? = null): ProgramFunction4To2 {
        val function = ProgramFunction4To2(name, body)
        installFunction(function)
        return function
    }
    fun function4To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To2 {
        val function = ProgramFunction4To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To2(name: String): ProgramFunction4To2 {
        val function = ProgramFunction4To2(name, null)
        installFunction(function)
        return function
    }

    fun function4To3(name: String, body: ComplexBlock? = null): ProgramFunction4To3 {
        val function = ProgramFunction4To3(name, body)
        installFunction(function)
        return function
    }
    fun function4To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To3 {
        val function = ProgramFunction4To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To3(name: String): ProgramFunction4To3 {
        val function = ProgramFunction4To3(name, null)
        installFunction(function)
        return function
    }

    fun function4To4(name: String, body: ComplexBlock? = null): ProgramFunction4To4 {
        val function = ProgramFunction4To4(name, body)
        installFunction(function)
        return function
    }
    fun function4To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To4 {
        val function = ProgramFunction4To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To4(name: String): ProgramFunction4To4 {
        val function = ProgramFunction4To4(name, null)
        installFunction(function)
        return function
    }

    fun function4To5(name: String, body: ComplexBlock? = null): ProgramFunction4To5 {
        val function = ProgramFunction4To5(name, body)
        installFunction(function)
        return function
    }
    fun function4To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To5 {
        val function = ProgramFunction4To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To5(name: String): ProgramFunction4To5 {
        val function = ProgramFunction4To5(name, null)
        installFunction(function)
        return function
    }

    fun function4To6(name: String, body: ComplexBlock? = null): ProgramFunction4To6 {
        val function = ProgramFunction4To6(name, body)
        installFunction(function)
        return function
    }
    fun function4To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To6 {
        val function = ProgramFunction4To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To6(name: String): ProgramFunction4To6 {
        val function = ProgramFunction4To6(name, null)
        installFunction(function)
        return function
    }

    fun function4To7(name: String, body: ComplexBlock? = null): ProgramFunction4To7 {
        val function = ProgramFunction4To7(name, body)
        installFunction(function)
        return function
    }
    fun function4To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To7 {
        val function = ProgramFunction4To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To7(name: String): ProgramFunction4To7 {
        val function = ProgramFunction4To7(name, null)
        installFunction(function)
        return function
    }

    fun function4To8(name: String, body: ComplexBlock? = null): ProgramFunction4To8 {
        val function = ProgramFunction4To8(name, body)
        installFunction(function)
        return function
    }
    fun function4To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction4To8 {
        val function = ProgramFunction4To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function4To8(name: String): ProgramFunction4To8 {
        val function = ProgramFunction4To8(name, null)
        installFunction(function)
        return function
    }

    fun function5To0(name: String, body: ComplexBlock? = null): ProgramFunction5To0 {
        val function = ProgramFunction5To0(name, body)
        installFunction(function)
        return function
    }
    fun function5To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To0 {
        val function = ProgramFunction5To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To0(name: String): ProgramFunction5To0 {
        val function = ProgramFunction5To0(name, null)
        installFunction(function)
        return function
    }

    fun function5To1(name: String, body: ComplexBlock? = null): ProgramFunction5To1 {
        val function = ProgramFunction5To1(name, body)
        installFunction(function)
        return function
    }
    fun function5To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To1 {
        val function = ProgramFunction5To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To1(name: String): ProgramFunction5To1 {
        val function = ProgramFunction5To1(name, null)
        installFunction(function)
        return function
    }

    fun function5To2(name: String, body: ComplexBlock? = null): ProgramFunction5To2 {
        val function = ProgramFunction5To2(name, body)
        installFunction(function)
        return function
    }
    fun function5To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To2 {
        val function = ProgramFunction5To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To2(name: String): ProgramFunction5To2 {
        val function = ProgramFunction5To2(name, null)
        installFunction(function)
        return function
    }

    fun function5To3(name: String, body: ComplexBlock? = null): ProgramFunction5To3 {
        val function = ProgramFunction5To3(name, body)
        installFunction(function)
        return function
    }
    fun function5To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To3 {
        val function = ProgramFunction5To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To3(name: String): ProgramFunction5To3 {
        val function = ProgramFunction5To3(name, null)
        installFunction(function)
        return function
    }

    fun function5To4(name: String, body: ComplexBlock? = null): ProgramFunction5To4 {
        val function = ProgramFunction5To4(name, body)
        installFunction(function)
        return function
    }
    fun function5To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To4 {
        val function = ProgramFunction5To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To4(name: String): ProgramFunction5To4 {
        val function = ProgramFunction5To4(name, null)
        installFunction(function)
        return function
    }

    fun function5To5(name: String, body: ComplexBlock? = null): ProgramFunction5To5 {
        val function = ProgramFunction5To5(name, body)
        installFunction(function)
        return function
    }
    fun function5To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To5 {
        val function = ProgramFunction5To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To5(name: String): ProgramFunction5To5 {
        val function = ProgramFunction5To5(name, null)
        installFunction(function)
        return function
    }

    fun function5To6(name: String, body: ComplexBlock? = null): ProgramFunction5To6 {
        val function = ProgramFunction5To6(name, body)
        installFunction(function)
        return function
    }
    fun function5To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To6 {
        val function = ProgramFunction5To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To6(name: String): ProgramFunction5To6 {
        val function = ProgramFunction5To6(name, null)
        installFunction(function)
        return function
    }

    fun function5To7(name: String, body: ComplexBlock? = null): ProgramFunction5To7 {
        val function = ProgramFunction5To7(name, body)
        installFunction(function)
        return function
    }
    fun function5To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To7 {
        val function = ProgramFunction5To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To7(name: String): ProgramFunction5To7 {
        val function = ProgramFunction5To7(name, null)
        installFunction(function)
        return function
    }

    fun function5To8(name: String, body: ComplexBlock? = null): ProgramFunction5To8 {
        val function = ProgramFunction5To8(name, body)
        installFunction(function)
        return function
    }
    fun function5To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction5To8 {
        val function = ProgramFunction5To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function5To8(name: String): ProgramFunction5To8 {
        val function = ProgramFunction5To8(name, null)
        installFunction(function)
        return function
    }

    fun function6To0(name: String, body: ComplexBlock? = null): ProgramFunction6To0 {
        val function = ProgramFunction6To0(name, body)
        installFunction(function)
        return function
    }
    fun function6To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To0 {
        val function = ProgramFunction6To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To0(name: String): ProgramFunction6To0 {
        val function = ProgramFunction6To0(name, null)
        installFunction(function)
        return function
    }

    fun function6To1(name: String, body: ComplexBlock? = null): ProgramFunction6To1 {
        val function = ProgramFunction6To1(name, body)
        installFunction(function)
        return function
    }
    fun function6To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To1 {
        val function = ProgramFunction6To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To1(name: String): ProgramFunction6To1 {
        val function = ProgramFunction6To1(name, null)
        installFunction(function)
        return function
    }

    fun function6To2(name: String, body: ComplexBlock? = null): ProgramFunction6To2 {
        val function = ProgramFunction6To2(name, body)
        installFunction(function)
        return function
    }
    fun function6To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To2 {
        val function = ProgramFunction6To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To2(name: String): ProgramFunction6To2 {
        val function = ProgramFunction6To2(name, null)
        installFunction(function)
        return function
    }

    fun function6To3(name: String, body: ComplexBlock? = null): ProgramFunction6To3 {
        val function = ProgramFunction6To3(name, body)
        installFunction(function)
        return function
    }
    fun function6To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To3 {
        val function = ProgramFunction6To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To3(name: String): ProgramFunction6To3 {
        val function = ProgramFunction6To3(name, null)
        installFunction(function)
        return function
    }

    fun function6To4(name: String, body: ComplexBlock? = null): ProgramFunction6To4 {
        val function = ProgramFunction6To4(name, body)
        installFunction(function)
        return function
    }
    fun function6To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To4 {
        val function = ProgramFunction6To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To4(name: String): ProgramFunction6To4 {
        val function = ProgramFunction6To4(name, null)
        installFunction(function)
        return function
    }

    fun function6To5(name: String, body: ComplexBlock? = null): ProgramFunction6To5 {
        val function = ProgramFunction6To5(name, body)
        installFunction(function)
        return function
    }
    fun function6To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To5 {
        val function = ProgramFunction6To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To5(name: String): ProgramFunction6To5 {
        val function = ProgramFunction6To5(name, null)
        installFunction(function)
        return function
    }

    fun function6To6(name: String, body: ComplexBlock? = null): ProgramFunction6To6 {
        val function = ProgramFunction6To6(name, body)
        installFunction(function)
        return function
    }
    fun function6To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To6 {
        val function = ProgramFunction6To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To6(name: String): ProgramFunction6To6 {
        val function = ProgramFunction6To6(name, null)
        installFunction(function)
        return function
    }

    fun function6To7(name: String, body: ComplexBlock? = null): ProgramFunction6To7 {
        val function = ProgramFunction6To7(name, body)
        installFunction(function)
        return function
    }
    fun function6To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To7 {
        val function = ProgramFunction6To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To7(name: String): ProgramFunction6To7 {
        val function = ProgramFunction6To7(name, null)
        installFunction(function)
        return function
    }

    fun function6To8(name: String, body: ComplexBlock? = null): ProgramFunction6To8 {
        val function = ProgramFunction6To8(name, body)
        installFunction(function)
        return function
    }
    fun function6To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction6To8 {
        val function = ProgramFunction6To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function6To8(name: String): ProgramFunction6To8 {
        val function = ProgramFunction6To8(name, null)
        installFunction(function)
        return function
    }

    fun function7To0(name: String, body: ComplexBlock? = null): ProgramFunction7To0 {
        val function = ProgramFunction7To0(name, body)
        installFunction(function)
        return function
    }
    fun function7To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To0 {
        val function = ProgramFunction7To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To0(name: String): ProgramFunction7To0 {
        val function = ProgramFunction7To0(name, null)
        installFunction(function)
        return function
    }

    fun function7To1(name: String, body: ComplexBlock? = null): ProgramFunction7To1 {
        val function = ProgramFunction7To1(name, body)
        installFunction(function)
        return function
    }
    fun function7To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To1 {
        val function = ProgramFunction7To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To1(name: String): ProgramFunction7To1 {
        val function = ProgramFunction7To1(name, null)
        installFunction(function)
        return function
    }

    fun function7To2(name: String, body: ComplexBlock? = null): ProgramFunction7To2 {
        val function = ProgramFunction7To2(name, body)
        installFunction(function)
        return function
    }
    fun function7To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To2 {
        val function = ProgramFunction7To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To2(name: String): ProgramFunction7To2 {
        val function = ProgramFunction7To2(name, null)
        installFunction(function)
        return function
    }

    fun function7To3(name: String, body: ComplexBlock? = null): ProgramFunction7To3 {
        val function = ProgramFunction7To3(name, body)
        installFunction(function)
        return function
    }
    fun function7To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To3 {
        val function = ProgramFunction7To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To3(name: String): ProgramFunction7To3 {
        val function = ProgramFunction7To3(name, null)
        installFunction(function)
        return function
    }

    fun function7To4(name: String, body: ComplexBlock? = null): ProgramFunction7To4 {
        val function = ProgramFunction7To4(name, body)
        installFunction(function)
        return function
    }
    fun function7To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To4 {
        val function = ProgramFunction7To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To4(name: String): ProgramFunction7To4 {
        val function = ProgramFunction7To4(name, null)
        installFunction(function)
        return function
    }

    fun function7To5(name: String, body: ComplexBlock? = null): ProgramFunction7To5 {
        val function = ProgramFunction7To5(name, body)
        installFunction(function)
        return function
    }
    fun function7To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To5 {
        val function = ProgramFunction7To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To5(name: String): ProgramFunction7To5 {
        val function = ProgramFunction7To5(name, null)
        installFunction(function)
        return function
    }

    fun function7To6(name: String, body: ComplexBlock? = null): ProgramFunction7To6 {
        val function = ProgramFunction7To6(name, body)
        installFunction(function)
        return function
    }
    fun function7To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To6 {
        val function = ProgramFunction7To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To6(name: String): ProgramFunction7To6 {
        val function = ProgramFunction7To6(name, null)
        installFunction(function)
        return function
    }

    fun function7To7(name: String, body: ComplexBlock? = null): ProgramFunction7To7 {
        val function = ProgramFunction7To7(name, body)
        installFunction(function)
        return function
    }
    fun function7To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To7 {
        val function = ProgramFunction7To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To7(name: String): ProgramFunction7To7 {
        val function = ProgramFunction7To7(name, null)
        installFunction(function)
        return function
    }

    fun function7To8(name: String, body: ComplexBlock? = null): ProgramFunction7To8 {
        val function = ProgramFunction7To8(name, body)
        installFunction(function)
        return function
    }
    fun function7To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction7To8 {
        val function = ProgramFunction7To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function7To8(name: String): ProgramFunction7To8 {
        val function = ProgramFunction7To8(name, null)
        installFunction(function)
        return function
    }

    fun function8To0(name: String, body: ComplexBlock? = null): ProgramFunction8To0 {
        val function = ProgramFunction8To0(name, body)
        installFunction(function)
        return function
    }
    fun function8To0(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To0 {
        val function = ProgramFunction8To0(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To0(name: String): ProgramFunction8To0 {
        val function = ProgramFunction8To0(name, null)
        installFunction(function)
        return function
    }

    fun function8To1(name: String, body: ComplexBlock? = null): ProgramFunction8To1 {
        val function = ProgramFunction8To1(name, body)
        installFunction(function)
        return function
    }
    fun function8To1(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To1 {
        val function = ProgramFunction8To1(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To1(name: String): ProgramFunction8To1 {
        val function = ProgramFunction8To1(name, null)
        installFunction(function)
        return function
    }

    fun function8To2(name: String, body: ComplexBlock? = null): ProgramFunction8To2 {
        val function = ProgramFunction8To2(name, body)
        installFunction(function)
        return function
    }
    fun function8To2(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To2 {
        val function = ProgramFunction8To2(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To2(name: String): ProgramFunction8To2 {
        val function = ProgramFunction8To2(name, null)
        installFunction(function)
        return function
    }

    fun function8To3(name: String, body: ComplexBlock? = null): ProgramFunction8To3 {
        val function = ProgramFunction8To3(name, body)
        installFunction(function)
        return function
    }
    fun function8To3(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To3 {
        val function = ProgramFunction8To3(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To3(name: String): ProgramFunction8To3 {
        val function = ProgramFunction8To3(name, null)
        installFunction(function)
        return function
    }

    fun function8To4(name: String, body: ComplexBlock? = null): ProgramFunction8To4 {
        val function = ProgramFunction8To4(name, body)
        installFunction(function)
        return function
    }
    fun function8To4(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To4 {
        val function = ProgramFunction8To4(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To4(name: String): ProgramFunction8To4 {
        val function = ProgramFunction8To4(name, null)
        installFunction(function)
        return function
    }

    fun function8To5(name: String, body: ComplexBlock? = null): ProgramFunction8To5 {
        val function = ProgramFunction8To5(name, body)
        installFunction(function)
        return function
    }
    fun function8To5(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To5 {
        val function = ProgramFunction8To5(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To5(name: String): ProgramFunction8To5 {
        val function = ProgramFunction8To5(name, null)
        installFunction(function)
        return function
    }

    fun function8To6(name: String, body: ComplexBlock? = null): ProgramFunction8To6 {
        val function = ProgramFunction8To6(name, body)
        installFunction(function)
        return function
    }
    fun function8To6(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To6 {
        val function = ProgramFunction8To6(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To6(name: String): ProgramFunction8To6 {
        val function = ProgramFunction8To6(name, null)
        installFunction(function)
        return function
    }

    fun function8To7(name: String, body: ComplexBlock? = null): ProgramFunction8To7 {
        val function = ProgramFunction8To7(name, body)
        installFunction(function)
        return function
    }
    fun function8To7(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To7 {
        val function = ProgramFunction8To7(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To7(name: String): ProgramFunction8To7 {
        val function = ProgramFunction8To7(name, null)
        installFunction(function)
        return function
    }

    fun function8To8(name: String, body: ComplexBlock? = null): ProgramFunction8To8 {
        val function = ProgramFunction8To8(name, body)
        installFunction(function)
        return function
    }
    fun function8To8(name: String, init: ComplexFunction.() -> Unit): ProgramFunction8To8 {
        val function = ProgramFunction8To8(name, null)
        function.setBody(init)
        installFunction(function)
        return function
    }
    fun function8To8(name: String): ProgramFunction8To8 {
        val function = ProgramFunction8To8(name, null)
        installFunction(function)
        return function
    }
}

class KsplangProgram(
    val body: ComplexFunction,
    val definedFunctions: List<ProgramFunctionBase>
)

fun program(init: KsplangProgramBuilder.() -> Unit): KsplangProgram {
    val program = KsplangProgramBuilder()
    program.init()
    return program.build()
}

/**
 * A function that can be called within a [KsplangProgram].
 */
sealed class ProgramFunctionBase(
    val name: String,
    val args: Int,
    val outputs: Int,
    body: ComplexBlock?
) {
    val innerFunctionName = "ksplang_program_function_$name"

    var body: ComplexBlock? = body
        private set

    fun setBody(block: ComplexFunction.() -> Unit) {
        val function = ComplexFunction(innerFunctionName)
        function.block()
        setBody(function)
    }

    fun setBody(body: ComplexFunction) {
        require(this.body == null) { "Function body can only be set once." }
        this.body = body
    }
}

@KsplangMarker
data class FunctionCall(val calledFunction: ProgramFunctionBase) : ComplexBlock {
    // This is quite ugly API-wise
    override var children: MutableList<Block>
        get() = error("FunctionCall does not have children.")
        set(_) = error("FunctionCall does not have children.")

    override fun addChild(block: SimpleBlock) {
        throw UnsupportedOperationException("Calls cannot contain other blocks.")
    }
}

fun ComplexBlock.call(function: ProgramFunctionBase): FunctionCall {
    val f = FunctionCall(function)
    this@call.children.add(f)
    return f
}

/**
 * Push the address of a function onto the stack.
 *
 * @param guaranteedEmittedAlready If true, indicates that the function being referenced is guaranteed to have been emitted,
 * so we can push the address directly. If false, we need to use a padded prepared push, which will be less efficient.
 * If this is violated, an exception is thrown at program generation time.
 */
@KsplangMarker
data class PushFunctionAddress(val calledFunction: ProgramFunctionBase, val guaranteedEmittedAlready: Boolean) : ComplexBlock {
    // This is quite ugly API-wise
    override var children: MutableList<Block>
        get() = error("PushFunctionAddress does not have children.")
        set(_) = error("PushFunctionAddress does not have children.")

    override fun addChild(block: SimpleBlock) {
        throw UnsupportedOperationException("Pushes of addresses cannot contain other blocks.")
    }
}

fun ComplexBlock.pushAddressOf(function: ProgramFunctionBase, guaranteedEmittedAlready: Boolean = false): PushFunctionAddress {
    val f = PushFunctionAddress(function, guaranteedEmittedAlready)
    this@pushAddressOf.children.add(f)
    return f
}


// Oh the joys of type safety.

class ProgramFunction(
    name: String,
    args: Int,
    outputs: Int,
    body: ComplexBlock? = null
) : ProgramFunctionBase(
    name = name,
    args = args,
    outputs = outputs,
    body = body,
) {
    init {
        require(args >= 0) { "Number of arguments must be non-negative." }
        require(outputs >= 0) { "Number of outputs must be non-negative." }
    }
}

class ProgramFunction0To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 0,
    body = body,
)

class ProgramFunction0To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 1,
    body = body,
)

class ProgramFunction0To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 2,
    body = body,
)

class ProgramFunction0To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 3,
    body = body,
)

class ProgramFunction0To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 4,
    body = body,
)

class ProgramFunction0To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 5,
    body = body,
)

class ProgramFunction0To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 6,
    body = body,
)

class ProgramFunction0To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 7,
    body = body,
)

class ProgramFunction0To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 0,
    outputs = 8,
    body = body,
)

class ProgramFunction1To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 0,
    body = body,
)

class ProgramFunction1To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 1,
    body = body,
)

class ProgramFunction1To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 2,
    body = body,
)

class ProgramFunction1To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 3,
    body = body,
)

class ProgramFunction1To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 4,
    body = body,
)

class ProgramFunction1To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 5,
    body = body,
)

class ProgramFunction1To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 6,
    body = body,
)

class ProgramFunction1To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 7,
    body = body,
)

class ProgramFunction1To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 1,
    outputs = 8,
    body = body,
)

class ProgramFunction2To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 0,
    body = body,
)

class ProgramFunction2To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 1,
    body = body,
)

class ProgramFunction2To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 2,
    body = body,
)

class ProgramFunction2To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 3,
    body = body,
)

class ProgramFunction2To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 4,
    body = body,
)

class ProgramFunction2To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 5,
    body = body,
)

class ProgramFunction2To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 6,
    body = body,
)

class ProgramFunction2To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 7,
    body = body,
)

class ProgramFunction2To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 2,
    outputs = 8,
    body = body,
)

class ProgramFunction3To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 0,
    body = body,
)

class ProgramFunction3To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 1,
    body = body,
)

class ProgramFunction3To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 2,
    body = body,
)

class ProgramFunction3To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 3,
    body = body,
)

class ProgramFunction3To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 4,
    body = body,
)

class ProgramFunction3To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 5,
    body = body,
)

class ProgramFunction3To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 6,
    body = body,
)

class ProgramFunction3To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 7,
    body = body,
)

class ProgramFunction3To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 3,
    outputs = 8,
    body = body,
)

class ProgramFunction4To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 0,
    body = body,
)

class ProgramFunction4To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 1,
    body = body,
)

class ProgramFunction4To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 2,
    body = body,
)

class ProgramFunction4To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 3,
    body = body,
)

class ProgramFunction4To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 4,
    body = body,
)

class ProgramFunction4To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 5,
    body = body,
)

class ProgramFunction4To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 6,
    body = body,
)

class ProgramFunction4To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 7,
    body = body,
)

class ProgramFunction4To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 4,
    outputs = 8,
    body = body,
)

class ProgramFunction5To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 0,
    body = body,
)

class ProgramFunction5To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 1,
    body = body,
)

class ProgramFunction5To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 2,
    body = body,
)

class ProgramFunction5To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 3,
    body = body,
)

class ProgramFunction5To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 4,
    body = body,
)

class ProgramFunction5To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 5,
    body = body,
)

class ProgramFunction5To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 6,
    body = body,
)

class ProgramFunction5To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 7,
    body = body,
)

class ProgramFunction5To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 5,
    outputs = 8,
    body = body,
)

class ProgramFunction6To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 0,
    body = body,
)

class ProgramFunction6To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 1,
    body = body,
)

class ProgramFunction6To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 2,
    body = body,
)

class ProgramFunction6To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 3,
    body = body,
)

class ProgramFunction6To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 4,
    body = body,
)

class ProgramFunction6To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 5,
    body = body,
)

class ProgramFunction6To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 6,
    body = body,
)

class ProgramFunction6To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 7,
    body = body,
)

class ProgramFunction6To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 6,
    outputs = 8,
    body = body,
)

class ProgramFunction7To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 0,
    body = body,
)

class ProgramFunction7To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 1,
    body = body,
)

class ProgramFunction7To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 2,
    body = body,
)

class ProgramFunction7To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 3,
    body = body,
)

class ProgramFunction7To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 4,
    body = body,
)

class ProgramFunction7To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 5,
    body = body,
)

class ProgramFunction7To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 6,
    body = body,
)

class ProgramFunction7To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 7,
    body = body,
)

class ProgramFunction7To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 7,
    outputs = 8,
    body = body,
)

class ProgramFunction8To0(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 0,
    body = body,
)

class ProgramFunction8To1(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 1,
    body = body,
)

class ProgramFunction8To2(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 2,
    body = body,
)

class ProgramFunction8To3(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 3,
    body = body,
)

class ProgramFunction8To4(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 4,
    body = body,
)

class ProgramFunction8To5(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 5,
    body = body,
)

class ProgramFunction8To6(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 6,
    body = body,
)

class ProgramFunction8To7(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 7,
    body = body,
)

class ProgramFunction8To8(
    name: String,
    body: ComplexBlock?
) : ProgramFunctionBase(
    name = name,
    args = 8,
    outputs = 8,
    body = body,
)
