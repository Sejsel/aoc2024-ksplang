plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.champeau.jmh)
    alias(libs.plugins.allopen)
    alias(libs.plugins.kapt)
    application
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(project(":gen"))
    implementation(project(":interpreter"))
    implementation(project(":wasm2ksplang"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.clikt)
    // JMH dependencies needed to execute benchmarks from main
    kapt("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    implementation("org.openjdk.jmh:jmh-core:1.37")
}

application {
    mainClass.set("cz.sejsel.ksplang.benchmarks.MainKt")
}

kotlin {
    jvmToolchain(21)
}
