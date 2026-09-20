plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingStorePath = System.getenv("TELEMETRICO_KEYSTORE_PATH")

android {
    namespace = "com.telemetrico.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.telemetrico.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "0.4.3.5-test"
    }

    signingConfigs {
        if (!signingStorePath.isNullOrBlank()) {
            create("release") {
                storeFile = file(signingStorePath)
                storePassword = System.getenv("TELEMETRICO_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TELEMETRICO_KEY_ALIAS")
                keyPassword = System.getenv("TELEMETRICO_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
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
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
