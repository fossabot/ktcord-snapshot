plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    repositories {
        mavenCentral()
        jcenter()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":ktcord-common"))
                api(project(":ktcord-rest"))
                api(project(":ktcord-gateway"))
                api(project(":ktcord-interactions"))
                implementation(kotlin("stdlib-common"))
            }
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf("-Xallow-kotlin-package")
            }
        }
    }
}