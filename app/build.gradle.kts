import java.util.Properties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Room 编译器支持
}

android {
    namespace = "cn.wi6.x2"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.wi6.x2"
        minSdk = 24
        targetSdk = 34

        // --- 自动创建 version.properties + 默认值 ---
        val versionPropsFile = rootProject.file("version.properties")
        val versionProps = Properties().apply {
            if (!versionPropsFile.exists()) {
                versionPropsFile.createNewFile()
                setProperty("VERSION_NAME", "1.0.0.0") // 默认初始版本
                store(FileOutputStream(versionPropsFile), "Auto-generated version config")
            }
            load(FileInputStream(versionPropsFile))
        }

        // 安全获取版本号
        val versionNameStr = versionProps.getProperty("VERSION_NAME", "1.0.0.0")
        versionName = versionNameStr

        // 版本号转 versionCode（处理异常）
        versionCode = try {
            versionNameStr.replace(".", "").toInt()
        } catch (e: Exception) {
            println("⚠️ 版本号格式错误，使用默认 versionCode: 10000")
            10000
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// --- afterEvaluate: 延迟绑定 assembleRelease 任务 ---
afterEvaluate {
    val assembleReleaseTask = tasks.findByName("assembleRelease")
    assembleReleaseTask?.doLast {
        val versionPropsFile = rootProject.file("version.properties")
        val versionProps = Properties().apply {
            load(FileInputStream(versionPropsFile))
        }

        val oldVersion = versionProps.getProperty("VERSION_NAME", "1.0.0.0")
        val parts = oldVersion.split(".").mapNotNull { it.toIntOrNull() }.toMutableList()

        // 确保4段式版本号
        while (parts.size < 4) parts.add(0)

        // 四段式递增，末位满9进1
        var i = parts.lastIndex
        parts[i]++
        while (i > 0 && parts[i] > 9) {
            parts[i] = 0
            parts[i - 1]++
            i--
        }
        val newVersion = parts.joinToString(".")

        // 更新并保存
        versionProps.setProperty("VERSION_NAME", newVersion)
        versionProps.store(FileOutputStream(versionPropsFile), "Updated version: $newVersion")
        println("✅ 版本号更新: $oldVersion → $newVersion")

        // --- 更新 changelog.json ---
        val assetsDir = File("app/src/main/assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
            println("✅ 创建 assets 目录: ${assetsDir.absolutePath}")
        }

        val changelogFile = File(assetsDir, "changelog.json")
        val changelogMap = mutableMapOf<String, String>()

        if (changelogFile.exists() && changelogFile.length() > 0) {
            try {
                val jsonText = changelogFile.readText().trim()
                jsonText.removeSurrounding("{", "}")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .forEach { kvStr ->
                        val kv = kvStr.split(":").map { it.trim().removeSurrounding("\"") }
                        if (kv.size == 2) changelogMap[kv[0]] = kv[1]
                    }
            } catch (e: Exception) {
                println("⚠️ 解析 changelog.json 错误，将重新创建")
                changelogFile.delete()
            }
        }

        if (!changelogMap.containsKey(newVersion)) {
            changelogMap[newVersion] = "初始化版本"
        }

        val newJson = changelogMap.entries
            .sortedByDescending { it.key }
            .joinToString(",\n    ") { "\"${it.key}\":\"${it.value}\"" }
        changelogFile.writeText("{\n    $newJson\n}")
        println("✅ changelog.json 更新完成，新增版本: $newVersion")
    } ?: run {
        println("⚠️ 未找到 assembleRelease 任务，版本号递增逻辑未绑定")
    }
}

dependencies {
    // Assists 库 - 一个第三方工具库，提供基础功能辅助
    implementation("com.github.ven-coder.Assists:assists-base:v3.2.17")

    // AndroidX 核心库 - 提供 Kotlin 扩展和基础功能
    implementation("androidx.core:core-ktx:1.10.1")

    // 生命周期管理 - 帮助管理组件生命周期
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Compose 的 Activity 集成
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose (使用 BOM 统一版本管理)
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")                // Compose UI 核心
    implementation("androidx.compose.ui:ui-graphics")       // 图形绘制
    implementation("androidx.compose.material3:material3")  // Material 3 设计组件
    implementation("androidx.compose.material:material-icons-extended") // 扩展图标

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")      // Room 运行时
    implementation("androidx.room:room-ktx:2.6.1")         // Kotlin 扩展和协程支持
    kapt("androidx.room:room-compiler:2.6.1")               // Room 注解处理器
    implementation("androidx.window:window:1.2.0")

}