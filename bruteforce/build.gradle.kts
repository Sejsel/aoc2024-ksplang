plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":interpreter"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

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
