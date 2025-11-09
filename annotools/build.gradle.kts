plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":gen"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.clikt)
}

application {
    mainClass.set("cz.sejsel.ksplang.annotools.MainKt")
}

kotlin {
    jvmToolchain(21)
}
