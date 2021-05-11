plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    repositories {
        maven(url = "https://dl.bintray.com/korlibs/korlibs/")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":ktcord-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
                implementation("io.ktor:ktor-client-core:1.5.2") { because("networking") }
                implementation("io.ktor:ktor-client-websockets:1.5.2") { because("websockets") }
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0") { because("gateway serialization") }
                implementation("org.jetbrains.kotlinx:atomicfu:0.15.2") { because("atomics") }
                implementation("io.github.microutils:kotlin-logging:2.0.2") { because("logging") }
                implementation(kotlin("stdlib-common"))
            }
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}