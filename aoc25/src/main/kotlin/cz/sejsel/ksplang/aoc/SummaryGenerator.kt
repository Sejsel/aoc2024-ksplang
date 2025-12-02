package cz.sejsel.ksplang.aoc

import cz.sejsel.ksplang.benchmarks.KsplangInterpreter
import cz.sejsel.ksplang.benchmarks.runBenchmarks

fun main() {
    val ksplangs = buildList {
        add(KsplangInterpreter("ksplang", "ksplang", optimize = false))
        add(KsplangInterpreter("KsplangJIT", "../exyi-ksplang/target/release/ksplang-cli", optimize = true))
        add(KsplangInterpreter("KsplangJIT old", "../exyi-ksplang/ksplang-last-known-working", optimize = true))
    }

    runBenchmarks(ksplangs, enableKotlin = false, Programs, ::AoC25Solutions)
}

