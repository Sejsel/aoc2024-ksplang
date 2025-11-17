package cz.sejsel.ksplang.dumper

import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.buildComplexFunction
import cz.sejsel.ksplang.std.cmp
import java.io.File

fun main(args: Array<String>) {
    val builder = KsplangBuilder()

    // see gen/src/main/kotlin/cz/sejsel/ksplang/std/ for stdlib

    val program = buildComplexFunction {
        cmp()
    }

    val ksplang = builder.buildAnnotated(program)
    File("cmp.ksplang").writeText(ksplang.toRunnableProgram())
}

