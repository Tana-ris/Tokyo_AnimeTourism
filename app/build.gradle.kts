plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.tokyo2"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.tokyo2"
        minSdk = 29
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {
    implementation ("com.google.maps:google-maps-services:0.11.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation  ("com.squareup.okhttp3:okhttp:4.8.0")
    implementation ("com.google.appengine:appengine-api-1.0-sdk:1.9.85")
    implementation ("org.slf4j:slf4j-simple:1.7.25")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("com.google.android.gms:play-services-location:17.0.0")  //ここら辺書いたz
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.5")
    // Dependency on a local library module


}