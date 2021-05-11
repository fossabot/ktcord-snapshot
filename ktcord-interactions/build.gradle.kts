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
                implementation(project(":ktcord-rest"))
                implementation("io.ktor:ktor-auth:1.5.2")
                implementation("io.ktor:ktor-client-core:1.5.2")
                implementation("io.ktor:ktor-client-serialization:1.5.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
                implementation("org.jetbrains.kotlinx:atomicfu:0.15.2") { because("atomics") }
                implementation("io.github.microutils:kotlin-logging:2.0.2") { because("logging") }
                implementation(kotlin("stdlib-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("net.i2p.crypto:eddsa:0.3.0") { because("ed25519 impl for verifying webhook requests") }
                implementation("io.ktor:ktor-server-core:1.5.3")
                implementation("io.ktor:ktor-server-netty:1.5.3")
            }
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.languageVersion = "1.5"
            languageSettings.apiVersion = "1.5"
        }
    }
}