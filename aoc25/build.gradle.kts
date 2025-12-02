plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    implementation(project(":gen"))
    implementation(project(":benchmarks"))

    testImplementation(libs.kotest.framework.engine)
}

kotlin {
    jvmToolchain(21)
}
