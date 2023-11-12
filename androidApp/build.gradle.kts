import java.lang.System.getenv

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidTarget()

    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
        }
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "ml.dev.kotlin.openotp"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "ml.dev.kotlin.openotp.OpenOtp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = getAndBumpVersionCode()
        versionName = getenv()["VERSION"] ?: "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    packaging {
        resources {
            excludes += "META-INF/versions/**"
        }
    }
}

fun getAndBumpVersionCode(): Int {
    val code = libs.versions.versionCode.get().toInt()
    val bump = getenv()["BUMP_FILE_VERSION_CODE"]?.toBooleanStrictOrNull() ?: false
    if (!bump) return code

    val file = File("gradle/libs.versions.toml")
    val updatedFile = file.readLines().map { line ->
        if (!line.startsWith("versionCode")) return@map line

        val currentVersionCode = line
            .dropWhile { it != '"' }
            .removePrefix("\"")
            .takeWhile { it != '"' }
            .toInt()
        if (currentVersionCode != code) throw IllegalStateException("Two different version codes: $code vs $currentVersionCode")

        """versionCode = "${currentVersionCode + 1}""""
    }.joinToString(separator = "\n")
    file.writeText(updatedFile)
    return code
}
