import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pdftruth"
    compileSdk = 35

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    defaultConfig {
        applicationId = "com.pdftruth"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.matching { it.name == "assembleDebug" || it.name == "assembleRelease" }.configureEach {
    doFirst {
        val variant = name.removePrefix("assemble").lowercase()
        val outputDir = layout.buildDirectory.dir("outputs/apk/$variant").get().asFile
        val cacheDir = layout.buildDirectory.dir("apk-cache/$variant").get().asFile
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        // 캐시에 있던 APK를 출력 폴더로 복원해서 빌드 산출물 폴더 정리 이후에도 이력을 유지
        cacheDir.listFiles().orEmpty()
            .filter { it.isFile && it.name.matches(Regex("^PDF_\\d{6}_\\d{2}\\.apk$")) }
            .forEach { cached ->
                val restored = outputDir.resolve(cached.name)
                if (!restored.exists()) {
                    cached.copyTo(restored, overwrite = false)
                }
            }
    }

    doLast {
        val variant = name.removePrefix("assemble").lowercase()
        val outputDir = layout.buildDirectory.dir("outputs/apk/$variant").get().asFile
        val cacheDir = layout.buildDirectory.dir("apk-cache/$variant").get().asFile

        // 빌드 중 정리된 기존 PDF APK를 먼저 복원
        cacheDir.listFiles().orEmpty()
            .filter { it.isFile && it.name.matches(Regex("^PDF_\\d{6}_\\d{2}\\.apk$")) }
            .forEach { cached ->
                val target = outputDir.resolve(cached.name)
                if (!target.exists()) {
                    cached.copyTo(target, overwrite = false)
                }
            }

        val defaultCandidates = listOf(
            outputDir.resolve("app-$variant.apk"),
            outputDir.resolve("app-$variant-unsigned.apk"),
        )
        val defaultApk = defaultCandidates.firstOrNull { it.exists() } ?: return@doLast

        val day = SimpleDateFormat("yyMMdd").format(Date())
        val prefix = "PDF_${day}_"
        val pattern = Regex("^PDF_${day}_(\\d{2})\\.apk$")

        val historyFiles = outputDir.listFiles().orEmpty().toList() + cacheDir.listFiles().orEmpty().toList()
        val usedNumbers = historyFiles
            .map { it.name }
            .mapNotNull { name -> pattern.matchEntire(name)?.groupValues?.get(1)?.toIntOrNull() }
            .toSet()

        var next = 1
        while (next in usedNumbers) {
            next += 1
        }

        var target = outputDir.resolve("${prefix}${"%02d".format(next)}.apk")
        while (target.exists()) {
            next += 1
            target = outputDir.resolve("${prefix}${"%02d".format(next)}.apk")
        }

        // 기존 APK를 절대 삭제/덮어쓰기하지 않고 새 번호 파일로 이동
        if (defaultApk.canonicalPath != target.canonicalPath) {
            defaultApk.renameTo(target)
        }

        // 다음 빌드를 위한 APK 이력 캐시 동기화
        outputDir.listFiles().orEmpty()
            .filter { it.isFile && it.name.matches(Regex("^PDF_\\d{6}_\\d{2}\\.apk$")) }
            .forEach { apk ->
                apk.copyTo(cacheDir.resolve(apk.name), overwrite = true)
            }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
}