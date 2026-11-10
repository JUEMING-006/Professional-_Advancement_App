plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.liquidglass"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    kotlin {
        jvmToolchain(21)
        compilerOptions {
            // 底层引擎 1.0.6 使用 Kotlin 2.3 的 context parameters。
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}

dependencies {
    // 引擎以普通 Maven 制品引入，不移植其源码。
    api(libs.kyant.backdrop)
    api(libs.kyant.shapes)

    // Compose 版本由 BOM 统一管理，与 :app 保持一致。
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.foundation)
    compileOnly("androidx.annotation:annotation:1.9.1")
}
