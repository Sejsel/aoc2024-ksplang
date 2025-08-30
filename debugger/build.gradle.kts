plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":gen"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
