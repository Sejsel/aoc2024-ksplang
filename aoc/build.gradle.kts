plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":gen"))
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.framework.datatest)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
