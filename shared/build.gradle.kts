import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "ml.dev.kotlin.openotp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources {
            enable = true
        }
    }

    jvm("desktop")

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            export(libs.decompose)
            export(libs.essenty.lifecycle)
            export(libs.essenty.stateKeeper)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val desktopMain by getting

        all {
            languageSettings.apply {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("com.russhwolf.settings.ExperimentalSettingsApi")
                optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("com.arkivanov.decompose.ExperimentalDecomposeApi")
            }
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.animation.graphics)
            implementation(libs.compose.components.resources)

            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
            implementation(libs.encoding.base32)

            implementation(libs.kotlincrypto.hash.sha2)
            implementation(libs.kotlincrypto.macs.hmac.sha1)
            implementation(libs.kotlincrypto.macs.hmac.sha2)
            implementation(libs.kotlincrypto.random.crypto.rand)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.kermit)
            implementation(libs.uriKmp)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            api(libs.decompose)
            api(libs.decompose.extensionsCompose)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.cbor)

            api(libs.essenty.lifecycle)
            api(libs.essenty.stateKeeper)
            api(libs.essenty.instanceKeeper)

            implementation(libs.compose.extensions.camera.permission)
            implementation(libs.compose.extensions.camera.qr)
            implementation(libs.compose.extensions.util)

            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain {
            dependencies {
                api(libs.androidx.activity.compose)
                api(libs.androidx.appcompat.appcompat)
                api(libs.androidx.core.ktx)

                implementation(libs.androidx.biometric)

                implementation(libs.mlkit.barcodeScanning)
                implementation(libs.androidx.security.crypto)

                implementation(libs.ktor.client.okhttp)

                runtimeOnly(libs.kotlinx.coroutines.android)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(libs.webcam.capture)
            implementation(libs.webcam.capture.driver.native)
            implementation(libs.zxing.core)
            implementation(libs.zxing.javase)

            runtimeOnly(libs.kotlinx.coroutines.swing)

            implementation(libs.ktor.client.okhttp)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }

    jvmToolchain(17)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ml.dev.kotlin.openotp.shared"
    generateResClass = always
}

tasks.withType<KotlinCompilationTask<*>>().all {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
