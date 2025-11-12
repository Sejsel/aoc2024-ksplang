plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotest)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.guava)
    api(libs.arrow)

    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.property)
}

kotlin {
    jvmToolchain(21)
}
