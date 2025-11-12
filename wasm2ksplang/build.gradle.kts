plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotest)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.chicory.wasm)
    implementation(libs.chicory.runtime)
    implementation(libs.chicory.wasm.tools)
    implementation(project(":gen"))

    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    testImplementation(project(":interpreter"))
}

kotlin {
    jvmToolchain(21)
}