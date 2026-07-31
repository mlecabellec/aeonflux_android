plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.aeonflux.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aeonflux.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime)

    // Media3
    implementation(libs.androidx.media3-exoplayer)
    implementation(libs.androidx.media3-session)
    implementation(libs.androidx.media3-ui)

    // Retrofit / OkHttp
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter-gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso-core)
}
