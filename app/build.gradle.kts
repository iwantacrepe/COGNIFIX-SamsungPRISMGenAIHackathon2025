plugins {
    // Plugin for Android applications
    alias(libs.plugins.android.application)
    // Plugin for Kotlin on Android
    alias(libs.plugins.kotlin.android)
    // Plugin for Jetpack Compose support
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.vaibhav.playground" // Package name for code
    compileSdk = 36 // Android SDK version used for compiling

    defaultConfig {
        applicationId = "com.vaibhav.playground" // Unique ID of your app
        minSdk = 24   // Minimum Android version supported
        targetSdk = 36 // The version you target/test on
        versionCode = 1 // Internal version (for Play Store updates)
        versionName = "1.0" // Human-readable version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Controls code shrinking & obfuscation (not needed for debug builds)
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configure Java compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Configure Kotlin compatibility
    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable Jetpack Compose
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    // Add this for JSON parsing with Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0") // Or the latest version

    // Firebase AI Logic SDK (Gemini Developer API + Imagen etc.)
    implementation("com.google.firebase:firebase-ai")
    // Core library
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.38.0-b01")

    // Material 3 integration
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.38.0-b01")

//    // Coil3 for image rendering inside markdown
    implementation("com.mikepenz:multiplatform-markdown-renderer-coil3:0.38.0-b01")

//    // (Optional) Syntax highlighting for code blocks
    implementation("com.mikepenz:multiplatform-markdown-renderer-code:0.38.0-b01")

    implementation("com.mikepenz:multiplatform-markdown-renderer-android:0.38.0-b01")

    // Optional: Firebase Analytics (if you want analytics)
    implementation("com.google.firebase:firebase-analytics")
    // Jetpack Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Core Android/Kotlin libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose + Activity integration
    implementation(libs.androidx.activity.compose)

    // Compose BOM (Bill of Materials = version alignment)
    implementation(platform(libs.androidx.compose.bom))
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Core Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) // Material3 components

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug tools (for previews and testing in IDE)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
