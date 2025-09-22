import java.util.Properties
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cn.wi6.x2"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.wi6.x2"
        minSdk = 34
        targetSdk = 34

        // 1. 自动创建 version.properties + 默认值（保留正确逻辑）
        val versionPropsFile = rootProject.file("version.properties")
        val versionProps = Properties().apply {
            if (!versionPropsFile.exists()) {
                versionPropsFile.createNewFile() // 不存在则创建文件
                setProperty("VERSION_NAME", "1.0.0.0") // 默认初始版本
                store(versionPropsFile.outputStream(), "Auto-generated version config") // 写入注释
            }
            load(versionPropsFile.inputStream()) // 加载配置
        }

        // 安全获取版本号（缺省时用默认值，避免空指针）
        val versionNameStr = versionProps.getProperty("VERSION_NAME", "1.0.0.0")
        versionName = versionNameStr
        // 版本号转 versionCode（处理异常：若格式错误，默认用 10000）
        versionCode = try {
            versionNameStr.replace(".", "").toInt()
        } catch (e: Exception) {
            println("版本号格式错误，使用默认 versionCode: 10000")
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

// 2. 关键修复：用 afterEvaluate 延迟绑定任务（确保 assembleRelease 已生成）
afterEvaluate {
    // 安全获取 assembleRelease 任务（避免找不到任务时报错）
    val assembleReleaseTask = tasks.findByName("assembleRelease")
    assembleReleaseTask?.doLast {
        // --- 版本号递增逻辑（仅执行1次，避免重复递增）---
        val versionPropsFile = rootProject.file("version.properties")
        val versionProps = Properties().apply {
            load(versionPropsFile.inputStream())
        }

        val oldVersion = versionProps.getProperty("VERSION_NAME", "1.0.0.0")
        val parts = oldVersion.split(".").mapNotNull { it.toIntOrNull() }.toMutableList()
        // 确保版本号是4段（不足时补0，避免数组越界）
        while (parts.size < 4) parts.add(0)

        // 四段式递增（末位满9进1，如 1.0.0.9 → 1.0.1.0）
        var i = parts.lastIndex
        parts[i]++
        while (i > 0 && parts[i] > 9) {
            parts[i] = 0
            parts[i - 1]++
            i--
        }
        val newVersion = parts.joinToString(".")

        // 更新并保存版本号
        versionProps.setProperty("VERSION_NAME", newVersion)
        versionProps.store(versionPropsFile.outputStream(), "Updated version: $newVersion")
        println("✅ 版本号更新: $oldVersion → $newVersion")

        // --- 更新 changelog.json（自动创建目录/文件）---
        val assetsDir = File("app/src/main/assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
            println("✅ 创建 assets 目录: ${assetsDir.absolutePath}")
        }

        val changelogFile = File(assetsDir, "changelog.json")
        val changelogMap = mutableMapOf<String, String>()

        // 读取已有 changelog（处理空文件/格式错误）
        if (changelogFile.exists() && changelogFile.length() > 0) {
            try {
                val jsonText = changelogFile.readText().trim()
                // 解析简单 JSON（避免依赖第三方库）
                jsonText.removeSurrounding("{", "}")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .forEach { kvStr ->
                        val kv = kvStr.split(":").map { s -> s.trim().removeSurrounding("\"") }
                        if (kv.size == 2) changelogMap[kv[0]] = kv[1]
                    }
            } catch (e: Exception) {
                println("⚠️  解析 changelog.json 错误，将重新创建文件")
                changelogFile.delete() // 删除损坏文件
            }
        }

        // 追加新版本记录（不存在时添加）
        if (!changelogMap.containsKey(newVersion)) {
            changelogMap[newVersion] = "初始化版本"
        }

        // 写入更新后的 JSON（格式化输出，便于阅读）
        val newJson = changelogMap.entries
            .sortedByDescending { it.key } // 按版本号降序排列
            .joinToString(",\n    ") { "\"${it.key}\":\"${it.value}\"" }
        changelogFile.writeText("{\n    $newJson\n}")
        println("✅ changelog.json 更新完成，新增版本: $newVersion")
    } ?: run {
        // 容错：若找不到 assembleRelease 任务，打印提示（避免构建失败）
        println("⚠️  未找到 assembleRelease 任务，版本号递增逻辑未绑定")
    }
}

dependencies {
    implementation("com.github.ven-coder.Assists:assists-base:v3.2.203")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
