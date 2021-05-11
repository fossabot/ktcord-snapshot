plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    repositories {
        mavenCentral()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ktcord-common"))
                implementation("io.ktor:ktor-client-core:1.5.2")
                implementation("io.ktor:ktor-client-serialization:1.5.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
                implementation("org.jetbrains.kotlinx:atomicfu:0.15.2")
                implementation("io.github.microutils:kotlin-logging:2.0.2")
                implementation(kotlin("stdlib-common"))
            }
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}