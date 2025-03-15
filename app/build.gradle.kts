import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.staysafe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.staysafe"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Load the API key from local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        // Set API keys in BuildConfig
        buildConfigField("String", "MAP_API_GOOGLE", "\"${properties.getProperty(" MAP_API_GOOGLE ")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        android.buildFeatures.buildConfig = true
        release {
            isMinifyEnabled = true
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.firestore.ktx)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // ViewModel for Jetpack Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Runtime dependency
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coroutines for asynchronous programming
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.location)

    // Google API Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)

    // Retrofit2
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)

    // JSON
    implementation(libs.gson)
    implementation(libs.converter.gson)

    // API request
    implementation(libs.volley)

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Communication with API and monitor log
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)

    // Permissions
    implementation(libs.accompanist.permissions)
}
