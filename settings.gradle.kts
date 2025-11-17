plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ksplang"
include("gen")
include("aoc")
include("bruteforce")
include("interpreter")
include("wasm2ksplang")
include("debugger")
include("annotools")
include("benchmarks")