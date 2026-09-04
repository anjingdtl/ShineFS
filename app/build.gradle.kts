plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shinefs.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shinefs.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
