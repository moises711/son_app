plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.moises.sam"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.moises.sam"
        minSdk = 26  // Actualizado a Android 8.0 (Oreo) para compatibilidad con dependencias
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Configuración para Room
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.coordinatorlayout)
    
    // Material Design
    implementation(libs.material)

    // Seguridad (EncryptedSharedPreferences, MasterKey)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Apache POI para Excel (versión compatible con Android)
    // Comentada temporalmente hasta resolver problema de dependencia
    // implementation("com.github.SUPERCILEX:poi-android:3.17.0")

    // Gráficos (MPAndroidChart)
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview           )
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)

    // PDF Generation
    implementation(libs.itext7.core)
    
    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}