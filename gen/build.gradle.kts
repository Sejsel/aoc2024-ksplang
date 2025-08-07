plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.framework.datatest)
    testImplementation(project(":interpreter"))
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-Xcontext-parameters",
            )
        )
    }
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
