plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.to_do_list"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.to_do_list"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //volley dependency
    implementation(libs.volley)

    implementation(libs.core.ktx)
    implementation(libs.okhttp)
    implementation (libs.androidx.constraintlayout.v214)
    implementation (libs.gif.android.gif.drawable)
}