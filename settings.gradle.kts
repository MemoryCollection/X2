// 1. 配置插件仓库（AGP 插件、Kotlin 插件从这里下载）
pluginManagement {
    repositories {
        // 阿里云 Google 仓库（优先，AGP 插件主要来源）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 阿里云 Gradle 插件仓库（优先）
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        // 阿里云 Maven 中央仓库（优先，第三方依赖）
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // 腾讯云作为备选（阿里云不可用时自动 fallback）
        maven { url = uri("https://mirrors.cloud.tencent.com/maven/google/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/gradle-plugin/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }

        // 官方仓库兜底
        gradlePluginPortal()
        google()
    }
}

// 2. 配置项目依赖仓库（普通第三方库从这里下载）
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云优先
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // 腾讯云备选
        maven { url = uri("https://mirrors.cloud.tencent.com/maven/google/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }

        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // JitPack 仓库（第三方库常用）
    }
}

// 3. 项目基础配置
rootProject.name = "X2"
include(":app")