import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.mul
import cz.sejsel.ksplang.std.push

fun main() {
    println("Hello, Debugger!")

    val builder = KsplangBuilder()

    val ksplang = program {
        val double = function2To1("double") {
            mul(2)
        }

        body {
            call(double)
            push(13)
            add()
        }
    }

    val annotated = builder.buildAnnotated(ksplang)
    println(annotated.toAnnotatedTreeJson())
}