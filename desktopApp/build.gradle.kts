import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.lang.System.getenv

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared"))
            }
        }
    }
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        version = getenv()["VERSION"] ?: "1.0.0"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "OpenOTP"

            windows {
                menu = false
                upgradeUuid = "b3aef4be-d942-4334-bdfc-6f42a7689c17"
                iconFile.set(projectDir.resolve("src/jvmMain/resources/ic_launcher.ico"))
            }

            linux {
                iconFile.set(projectDir.resolve("src/jvmMain/resources/ic_launcher.png"))
            }

            macOS {
                bundleID = "ml.dev.kotlin.openotp.OpenOtp"
                appStore = false
                iconFile.set(projectDir.resolve("src/jvmMain/resources/ic_launcher.icns"))
                signing {
                    sign.set(false)
                }
                runtimeEntitlementsFile.set(project.file("runtime-entitlements.plist"))
                infoPlist {
                    extraKeysRawXml = """
                        <key>NSCameraUsageDescription</key>
                        <string></string>
                    """.trimIndent()
                }
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
            isEnabled = true
            optimize = true
            obfuscate = false
        }
    }
}
