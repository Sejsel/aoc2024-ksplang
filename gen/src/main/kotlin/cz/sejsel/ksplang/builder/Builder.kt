package cz.sejsel.ksplang.builder


// Instruction contains text
// SimpleFunction contains Instructions or SimpleFunctions
// ComplexFunction contains ComplexFunctions or SimpleFunctions or Instructions

@DslMarker
annotation class KsplangMarker

interface ComplexFunction {

}

class IfZero : ComplexFunction {

}

@KsplangMarker
fun function(init: SimpleFunction.() -> Unit): SimpleFunction {
    val f = SimpleFunction()
    f.init()
    return f
}

fun main() {
    val program = function {
        CS()
        inc()
        CS()
        inc()
    }

    //val program = ksplang {
    //    CS()
    //    inc()
    //    CS()
    //    inc()
    //}

    //println("program: $program")
}