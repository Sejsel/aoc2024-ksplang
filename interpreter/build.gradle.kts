plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.guava)

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
