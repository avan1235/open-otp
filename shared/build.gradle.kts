import dev.icerock.gradle.MRVisibility
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.moko.resources)
}

kotlin {
    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
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

    applyDefaultHierarchyTemplate()

    sourceSets {
        val desktopMain by getting

        all {
            languageSettings.apply {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.ExperimentalStdlibApi")
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.animationGraphics)

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.datetime)
            implementation(libs.buffer)
            implementation(libs.uuid)
            implementation(libs.encoding.base32)

            implementation(libs.kotlincrypto.hash.sha2)
            implementation(libs.kotlincrypto.macs.hmac.sha1)
            implementation(libs.kotlincrypto.macs.hmac.sha2)
            implementation(libs.kotlincrypto.secure.random)
            implementation(libs.kotlincrypto.secure.random)

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

            api(libs.moko.resoures)
            api(libs.moko.resoures.compose)

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
            dependsOn(commonMain.get())

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

            languageSettings {
                optIn("com.google.accompanist.permissions.ExperimentalPermissionsApi")
            }
        }

        iosMain {
            dependsOn(commonMain.get())

            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        desktopMain.dependsOn(commonMain.get())
        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(libs.webcam.capture)
            implementation(libs.webcam.capture.driver.native)
            implementation(libs.zxing.core)
            implementation(libs.zxing.javase)

            runtimeOnly(libs.kotlinx.coroutines.swing)

            implementation(libs.ktor.client.okhttp)
        }
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "ml.dev.kotlin.openotp.shared"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "ml.dev.kotlin.openotp.shared"
    multiplatformResourcesClassName = "OpenOtpResources"
    multiplatformResourcesVisibility = MRVisibility.Public
    iosBaseLocalizationRegion = "en"
    multiplatformResourcesSourceSet = "commonMain"
    disableStaticFrameworkWarning = true
}

tasks.withType<KotlinCompilationTask<*>>().all {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
