import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.compose.compiler)
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
        val apiKey = properties.getProperty("MAP_API_GOOGLE")
        buildConfigField("String", "MAP_API_GOOGLE", apiKey)
        manifestPlaceholders["google_maps_api_key"] = apiKey

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
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Google API Maps
//    implementation(libs.play.services.maps)
    implementation(libs.play.services.maps.v1820)
//    implementation(libs.maps.compose)
    implementation(libs.maps.compose.v2110)
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)

    // Permissions
    implementation(libs.accompanist.permissions)
}
