plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    repositories {
        jcenter()
        repositories {
            maven("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":ktcord-common"))
                implementation("io.ktor:ktor-client-core:1.5.2")
                implementation("io.ktor:ktor-network:1.5.2")
                implementation("io.ktor:ktor-client-websockets:1.5.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation("io.github.microutils:kotlin-logging:2.0.2")
                implementation("org.jetbrains.kotlinx:atomicfu:0.15.2")
                implementation(kotlin("stdlib-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("com.codahale:xsalsa20poly1305:0.10.1")
            }
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}