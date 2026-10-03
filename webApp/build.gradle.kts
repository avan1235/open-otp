import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
        }
    }
}

tasks.named<Copy>("wasmJsProcessResources") {
    val outputDir = destinationDir
    val oneDriveRedirectPages = listOf("onedrive-native", "onedrive-web")
    filesMatching("onedrive.html") {
        oneDriveRedirectPages.forEach { page -> copyTo(outputDir.resolve("$page.html")) }
        exclude()
    }
}
