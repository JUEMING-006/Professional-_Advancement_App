import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.competition"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.example.competition"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // 编译/测试/运行三处统一用 JDK 21。
    kotlin {
        jvmToolchain(21)
    }

    // 后端地址从 local.properties 注入，不硬编码在代码里。
    //   local.properties 里写：api.baseUrl=http://10.0.2.2:8080
    // 模拟器访问宿主机用 10.0.2.2；真机用局域网 IP（如 http://192.168.1.5:8080）。
    //
    // ⚠️ 必须自己读 local.properties：
    // Gradle **不会**把 local.properties 自动加载成 project property（只自动加载 gradle.properties），
    // 直接 `findProperty("api.baseUrl")` 永远是 null，会静默退回默认值——
    // 表现为「改了 local.properties 但 APK 里还是老地址」（已实测踩过）。
    //
    // 注意：脚本里不能写 `java.util.Properties` —— `java` 是 Gradle 的隐式扩展属性，
    // 会把 `java.util` 当成属性链解析而报 "Unresolved reference 'util'"。
    // 所以本文件的顶部已 `import java.util.Properties`。
    val localProps = Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use { stream -> localProps.load(stream) }
    }
    val apiBaseUrl: String = (localProps.getProperty("api.baseUrl")
        ?: (project.findProperty("api.baseUrl") as String?)
        ?: "http://10.0.2.2:8080").trim()

    logger.lifecycle("API_BASE_URL = $apiBaseUrl")

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        }
        release {
            // 液态玻璃与引擎的类名会被反射与 shader 缓存查找用到，R8 必须保留。
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // 液态玻璃组件套（源码模块；引擎依赖由其 POM 传递）。
    // 项目模块引用不进版本目录 —— 那里只放外部构件的坐标与版本。
    implementation(project(":liquidglass"))

    // 网络层：契约用 kotlinx.serialization，HTTP 用 OkHttp。
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // 启动性能：Baseline Profile。
    implementation(libs.androidx.profileinstaller)
}
