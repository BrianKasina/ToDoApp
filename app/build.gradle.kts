plugins {
    alias(libs.plugins.kotlin.android)
    id("com.android.application")

    // Add the Google services Gradle plugin

    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace = "com.example.programmingassignment"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.programmingassignment"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        viewBinding = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.foundation.android)
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.material) // For Material Design components
    implementation (libs.androidx.activity.compose)// Required for Jetpack Compose activities
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Choose one of the following:
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation("androidx.compose.ui:ui")

    //navigation for jetpack compose
    implementation ("androidx.navigation:navigation-compose:2.8.2")


    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    // Firebase UI for Authentication
    implementation ("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation ("com.google.firebase:firebase-firestore-ktx")

    implementation("com.google.firebase:firebase-analytics")
    // https://firebase.google.com/docs/android/setup#available-libraries

}