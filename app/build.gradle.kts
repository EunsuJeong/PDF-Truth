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
    doLast {
        val variant = name.removePrefix("assemble").lowercase()
        val outputDir = layout.buildDirectory.dir("outputs/apk/$variant").get().asFile
        val defaultCandidates = listOf(
            outputDir.resolve("app-$variant.apk"),
            outputDir.resolve("app-$variant-unsigned.apk"),
        )
        val defaultApk = defaultCandidates.firstOrNull { it.exists() } ?: return@doLast

        val today = SimpleDateFormat("yyMMdd").format(Date())
        val prefix = "PDF_${today}_"
        val existing = outputDir.listFiles().orEmpty()
            .map { it.name }
            .filter { it.startsWith(prefix) && it.endsWith(".apk") }
            .mapNotNull { it.removePrefix(prefix).removeSuffix(".apk").toIntOrNull() }

        val nextNumber = ((existing.maxOrNull() ?: 0) + 1).coerceAtMost(99)
        val targetName = "${prefix}${"%02d".format(nextNumber)}.apk"
        val targetApk = outputDir.resolve(targetName)
        if (targetApk.exists()) {
            targetApk.delete()
        }
        defaultApk.renameTo(targetApk)
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