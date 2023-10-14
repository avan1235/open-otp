import dev.icerock.gradle.MRVisibility
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.parcelize.darwin)
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
            export(libs.parcelize.darwinRuntime)
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("com.russhwolf.settings.ExperimentalSettingsApi")
                optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("com.arkivanov.decompose.ExperimentalDecomposeApi")
                optIn("com.google.accompanist.permissions.ExperimentalPermissionsApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.animationGraphics)

                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.kotlinx.datetime)
                implementation(libs.buffer)
                implementation(libs.uuid)
                implementation(libs.encoding.base32)

                implementation(libs.kotlincrypto.macs.hmac.sha1)
                implementation(libs.kotlincrypto.macs.hmac.sha2)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                implementation(libs.kermit)
                implementation(libs.uriKmp)

                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.coroutines)

                api(libs.decompose)
                api(libs.decompose.extensionsComposeJetbrains)

                implementation(libs.kotlinx.serialization.json)

                api(libs.essenty.lifecycle)
                api(libs.essenty.stateKeeper)
                api(libs.essenty.parcelable)
                api(libs.essenty.instanceKeeper)

                api(libs.moko.resoures)
                api(libs.moko.resoures.compose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(libs.androidx.activity.compose)
                api(libs.androidx.appcompat.appcompat)
                api(libs.androidx.core.ktx)

                implementation(libs.androidx.camera)
                implementation(libs.androidx.cameraLifecycle)
                implementation(libs.androidx.cameraPreview)
                implementation(libs.androidx.biometric)

                implementation(libs.mlkit.barcodeScanning)
                implementation(libs.androidx.security.crypto)

                implementation(libs.accompanist.permissions)
                implementation(libs.accompanist.systemuicontroller)

                runtimeOnly(libs.kotlinx.coroutines.android)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                api(libs.parcelize.darwinRuntime)
            }
        }
        val desktopMain by getting {
            dependsOn(commonMain)

            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.webcam.capture)
                implementation(libs.webcam.capture.driver.native)
                implementation(libs.zxing.core)
                implementation(libs.zxing.javase)

                runtimeOnly(libs.kotlinx.coroutines.swing)
            }
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
