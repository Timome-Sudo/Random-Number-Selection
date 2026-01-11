import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// 从 Git 提交数获取版本号
fun getVersionCodeFromGit(): Int {
    return try {
        val result = project.providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }
        result.standardOutput.asText.get().trim().toInt()
    } catch (e: Exception) {
        println("获取 Git 版本号失败，使用默认值: ${e.message}")
        1
    }
}

// 从 Git 标签获取版本名称
fun getVersionNameFromGit(): String {
    return try {
        val result = project.providers.exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
        }
        val latestTag = result.standardOutput.asText.get().trim()
        
        // 如果获取到标签，去除开头的 "v" 前缀
        if (latestTag.isNotEmpty() && latestTag != "") {
            return latestTag.removePrefix("v")
        }
        
        // 如果没有标签，使用提交哈希的前7位
        val hashResult = project.providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }
        val shortHash = hashResult.standardOutput.asText.get().trim()
        
        "0.1.0-$shortHash"
    } catch (e: Exception) {
        println("获取 Git 版本名失败，使用默认值: ${e.message}")
        "1.0"
    }
}

android {
    namespace = "com.timome.sjxh"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.timome.sjxh"
        minSdk = 21
        targetSdk = 36
        versionCode = getVersionCodeFromGit()
        versionName = getVersionNameFromGit()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}