import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MediLert"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // KMP ViewModel + Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // gitlive Firebase (KMP-compatible Firebase wrappers)
            implementation(libs.firebase.gitlive.auth)
            implementation(libs.firebase.gitlive.firestore)
            implementation(libs.firebase.gitlive.storage)
            implementation(libs.firebase.gitlive.messaging)
            implementation(libs.firebase.gitlive.crashlytics)
            implementation(libs.firebase.gitlive.analytics)

            // Multiplatform Settings (SharedPreferences replacement)
            implementation(libs.multiplatform.settings)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.core.splashscreen)

            // Android-specific coroutines + Firebase Task extensions
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlinx.coroutines.play.services)

            // Credential Manager for Google Sign-In (Android only)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.google.identity.googleid)

            // Multiplatform Settings Android backing
            implementation(libs.multiplatform.settings.no.arg)
        }

        iosMain.dependencies {
            // iOS-specific dependencies added here if needed
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
        }
    }
}

android {
    namespace = "com.nkwabyte.medilert"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.nkwabyte.medilert"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose Web Client ID from local.properties → BuildConfig
        val localProps = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProps["GOOGLE_WEB_CLIENT_ID"]}\"")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Point Android source set at the new androidMain layout
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res", "src/main/res")
            assets.srcDirs("src/androidMain/assets")
        }
    }
}

compose.resources {
    packageOfResClass = "com.nkwabyte.medilert.generated.resources"
    publicResClass = true
}
