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

tasks.matching { it.name.startsWith("assemble") }.configureEach {
    doLast {
        val buildType = name.removePrefix("assemble").lowercase()
        val outputDir = layout.buildDirectory.dir("outputs/apk/$buildType").get().asFile
        val defaultApk = outputDir.resolve("app-$buildType.apk")

        if (defaultApk.exists()) {
            val date = "260519" // 임시로 고정된 날짜 사용
            val baseName = "PDF_${date}_"

            // Find the next available number
            val existingFiles = outputDir.listFiles()?.filter { it.name.startsWith(baseName) } ?: emptyList()
            val nextNumber = (existingFiles.mapNotNull {
                it.name.removePrefix(baseName).removeSuffix(".apk").toIntOrNull()
            }.maxOrNull() ?: 0) + 1

            val newApkName = "$baseName${"%02d".format(nextNumber)}.apk"
            val newApk = outputDir.resolve(newApkName)

            defaultApk.renameTo(newApk)
            println("APK renamed to: ${newApk.name}")
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