plugins {
    alias(libs.plugins.android.application)

    // Firebase Google Services plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.learningapp"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.learningapp"

        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // AndroidX
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.core:core-ktx:1.17.0")

    // Material Design
    implementation(libs.material)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // PDF text extraction (real PDF parser)
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lifecycle - lifecycleScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Firebase BoM
    implementation(
        platform("com.google.firebase:firebase-bom:34.16.0")
    )

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Testing
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}