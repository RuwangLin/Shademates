plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" // 添加这一行，版本需与你的 Kotlin 版本匹配
}

android {
    namespace = "com.example.a5120_shademate"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.a5120_shademate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val mapboxAccessToken = providers.gradleProperty("MAPBOX_ACCESS_TOKEN").orNull.orEmpty()
        val openWeatherApiKey = providers.gradleProperty("OPENWEATHER_API_KEY").orNull.orEmpty()
        val uvTipsApiKey = providers.gradleProperty("UV_TIPS_API_KEY").orNull.orEmpty()
        val backendBaseUrl = providers.gradleProperty("BACKEND_BASE_URL").orNull
            ?: "https://example.invalid/"

        resValue("string", "mapbox_access_token", mapboxAccessToken)
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherApiKey\"")
        buildConfigField("String", "UV_TIPS_API_KEY", "\"$uvTipsApiKey\"")
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")

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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.turf)
    implementation(libs.mapbox.search.place.autocomplete)
    implementation(libs.mapbox.search.android)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
