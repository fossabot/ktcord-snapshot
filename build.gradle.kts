plugins {
    val kotlinVersion = "1.5.0"
    kotlin("multiplatform") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
}

allprojects {
    group = "net.lostillusion.ktcord"

    repositories {
        mavenCentral()
    }
}


//subprojects {
//    this.apply { plugin(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin::class) }
//
//    this.the<KotlinMultiplatformExtension>().apply {
//        sourceSets {
//            all {
//                languageSettings.languageVersion = "1.5"
//                languageSettings.apiVersion = "1.5"
//            }
//        }
//    }
//}

//kotlin {
//    /* Targets configuration omitted.
//    *  To find out how to configure the targets, please follow the link:
//    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
//
//    sourceSets {
//        commonMain {
//            dependencies {
//                implementation kotlin('stdlib-common')
//            }
//        }
//        commonTest {
//            dependencies {
//                implementation kotlin('test-common')
//                implementation kotlin('test-annotations-common')
//            }
//        }
//    }
//}