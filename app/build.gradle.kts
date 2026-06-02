plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nomi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nomi"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

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

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/LICENSE.txt"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    implementation("org.apache.poi:poi:3.17")
    implementation("org.apache.poi:poi-ooxml:3.17") {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }

    // ── FIREBASE (SOLUCIÓN AL ERROR) ────────────────────────
    // Usamos el BoM para coordinar las versiones
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Importamos las librerías estándar (ya incluyen Kotlin)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
}