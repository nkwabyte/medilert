import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
    kotlin("native.cocoapods")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    // CocoaPods integration: generates MediLert.podspec and links Firebase iOS SDK
    // so that gitlive-firebase can resolve its native symbols at link time.
    cocoapods {
        version = "1.0"
        summary = "MediLert Kotlin Multiplatform shared module"
        homepage = "https://github.com/nkwabyte/medilert"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "MediLert"
            isStatic = true
        }
        // Firebase pods required by gitlive-firebase for iOS linking
        pod("FirebaseCore")
        pod("FirebaseAuth")
        pod("FirebaseFirestore")
        pod("FirebaseStorage")
        pod("FirebaseMessaging")
        pod("FirebaseCrashlytics")
        pod("FirebaseAnalytics")
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

            // Multiplatform Settings with no-arg factory (works on all platforms)
            implementation(libs.multiplatform.settings.no.arg)
            
            // Coil — Compose image loading
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            // Firebase Android BoM — pins all native firebase-* versions that
            // gitlive Firebase transitively requires (no explicit versions needed).
            // Using project.dependencies.platform() because KMP's platform(Any) is
            // deprecated at ERROR level since Kotlin 2.1 (KT-58759).
            implementation(project.dependencies.platform(libs.firebase.android.bom))

            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.material)

            // Android-specific coroutines + Firebase Task extensions
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlinx.coroutines.play.services)

            // Credential Manager for Google Sign-In (Android only)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.google.identity.googleid)

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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nkwabyte.medilert"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose secrets from local.properties → BuildConfig
        val localProps = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()
                ?.use { load(it) }
        }
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",
            "\"${localProps["GOOGLE_WEB_CLIENT_ID"] ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME",
            "\"${localProps["CLOUDINARY_CLOUD_NAME"] ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY",
            "\"${localProps["CLOUDINARY_API_KEY"] ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET",
            "\"${localProps["CLOUDINARY_API_SECRET"] ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_FOLDER", "\"medilert/profiles\"")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Point Android source sets at the KMP androidMain layout.
    // src/main/res and src/main/java are intentionally excluded — all resources
    // and sources live in src/androidMain/ and src/commonMain/ (KMP source sets).
    // The old src/main/ directory is a pre-KMP leftover and must not be compiled.
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.directories.clear()
            res.directories.add("src/androidMain/res")
            assets.directories.clear()
            assets.directories.add("src/androidMain/assets")
            java.directories.clear()  // Exclude src/main/java; KMP owns all sources
        }
    }
}

compose.resources {
    packageOfResClass = "com.nkwabyte.medilert.generated.resources"
    publicResClass = true
}
