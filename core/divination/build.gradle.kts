plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:yijing"))
    implementation(project(":core:calendar"))
    implementation(project(":core:compass"))
    testImplementation(libs.junit)
}
