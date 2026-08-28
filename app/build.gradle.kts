import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlin)
    alias(libs.plugins.hiltPlugin)
    alias(libs.plugins.roomPlugin)
    kotlin("kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.gradlePlayPublisher)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val hasReleaseSigningConfig = listOf(
    "RELEASE_STORE_FILE",
    "RELEASE_STORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD"
).all { !localProperties.getProperty(it).isNullOrBlank() }

android {
    namespace = "com.rendox.shoppinggenius"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rendox.shoppinggenius"
        minSdk = 21
        targetSdk = 35
        versionCode = 12
        versionName = "0.1.8"

        testInstrumentationRunner = "com.rendox.shoppinggenius.testing.ShoppingGeniusTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (!hasReleaseSigningConfig) {
                error(
                    "Release signing is not configured. Add RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD to local.properties"
                )
            }
            signingConfig = signingConfigs.getByName("release")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
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
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

androidComponents {
    // Keep local default builds stable; benchmark variants can be re-enabled explicitly.
    beforeVariants(selector().withBuildType("benchmark")) { variantBuilder ->
        if (!providers.gradleProperty("enableBenchmark").isPresent) {
            variantBuilder.enable = false
        }
    }
}

val playServiceAccountJson = localProperties.getProperty("PLAY_SERVICE_ACCOUNT_JSON").orEmpty()
if (playServiceAccountJson.isNotBlank()) {
    play {
        serviceAccountCredentials.set(file(playServiceAccountJson))
        // Track: "internal", "alpha", "beta" oder "production"
        track.set("internal")
        // AAB (empfohlen) statt APK hochladen
        defaultToAppBundles.set(true)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.car.app)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)

    implementation(libs.com.google.android.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.com.google.dagger.hilt.android)
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    kapt(libs.com.google.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.com.google.dagger.hilt.android.testing)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    // noinspection KaptUsageInsteadOfKsp (because hilt requires kapt anyway)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.test.runner)

    implementation(libs.com.squareup.moshi)
    implementation(libs.com.squareup.moshi.kotlin)
    ksp(libs.com.squareup.moshi.kotlin.codegen)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.io.coil.compose)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.compose.ui.viewbinding)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.com.squareup.retrofit.core)
    implementation(libs.com.squareup.retrofit.moshi)
    implementation(libs.com.squareup.okhttp.logging)

    testImplementation(platform(libs.org.junit.bom))
    testImplementation(libs.org.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.engine)
    testImplementation(libs.org.junit.jupiter.params)

    testImplementation(libs.com.google.truth)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.com.google.dagger.hilt.android.testing)
    androidTestImplementation(libs.com.google.truth)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}