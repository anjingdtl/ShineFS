plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

fun signingEnv(primary: String, legacy: String): String? =
    System.getenv(primary)?.takeIf { it.isNotBlank() }
        ?: System.getenv(legacy)?.takeIf { it.isNotBlank() }

val releaseBuildRequested = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true)
}
val ciBuild = System.getenv("CI").equals("true", ignoreCase = true)
val releaseStoreFileValue = signingEnv("SHINEFS_RELEASE_STORE_FILE", "SHINE_WRITER_RELEASE_STORE_FILE")
val releaseStorePasswordValue = signingEnv("SHINEFS_RELEASE_STORE_PASSWORD", "SHINE_WRITER_RELEASE_STORE_PASSWORD")
val releaseKeyAliasValue = signingEnv("SHINEFS_RELEASE_KEY_ALIAS", "SHINE_WRITER_RELEASE_KEY_ALIAS")
val releaseKeyPasswordValue = signingEnv("SHINEFS_RELEASE_KEY_PASSWORD", "SHINE_WRITER_RELEASE_KEY_PASSWORD")
val releaseSigningConfigured = releaseStoreFileValue != null &&
    releaseStorePasswordValue != null &&
    releaseKeyAliasValue != null &&
    releaseKeyPasswordValue != null

if (releaseBuildRequested && !releaseSigningConfigured && !ciBuild) {
    val missing = buildList {
        if (releaseStoreFileValue == null) add("SHINEFS_RELEASE_STORE_FILE/SHINE_WRITER_RELEASE_STORE_FILE")
        if (releaseStorePasswordValue == null) add("SHINEFS_RELEASE_STORE_PASSWORD/SHINE_WRITER_RELEASE_STORE_PASSWORD")
        if (releaseKeyAliasValue == null) add("SHINEFS_RELEASE_KEY_ALIAS/SHINE_WRITER_RELEASE_KEY_ALIAS")
        if (releaseKeyPasswordValue == null) add("SHINEFS_RELEASE_KEY_PASSWORD/SHINE_WRITER_RELEASE_KEY_PASSWORD")
    }
    if (missing.isNotEmpty()) {
        throw GradleException(
            "Release signing requires environment variable(s): ${missing.joinToString(", ")}. " +
                "Debug builds do not require release signing secrets.",
        )
    }
    val releaseStoreFile = file(releaseStoreFileValue!!)
    if (!releaseStoreFile.exists()) {
        throw GradleException("Release signing keystore not found: ${releaseStoreFile.absolutePath}")
    }
}

android {
    namespace = "com.shinefs.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shinefs.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "2.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing is environment-only. The existing TAVO-MINI variables
    // are accepted for key reuse; ShineFS-specific names take precedence.
    // There is deliberately no debug-keystore fallback for Release.
    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = file(releaseStoreFileValue!!)
                storePassword = releaseStorePasswordValue!!
                keyAlias = releaseKeyAliasValue!!
                keyPassword = releaseKeyPasswordValue!!
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            // CI verifies the release variant as an unsigned artifact; local release
            // builds keep the environment-only signing requirement above.
            signingConfig = if (releaseSigningConfigured) signingConfigs.getByName("release") else null
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        // PropertyEscape 对已按规范转义的 local.properties 在 Windows 上仍误报；
        // 该文件不入库（.gitignore）、不随 APK 发布，故仅对此检查关闭。
        disable += "PropertyEscape"
    }

    buildFeatures {
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:compass"))
    implementation(project(":core:yijing"))
    implementation(project(":core:calendar"))
    implementation(project(":core:divination"))
    implementation(project(":core:classics"))
    implementation(project(":core:interpretation"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    // androidTest 用独立 JUnit（不吃 app 主依赖的 test scope；库来自同一版本目录，离线可用）
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
