import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
// AS Gallery is intentionally kept independent from the upstream Firebase project.
// Telemetry / store integrations will be added later with AS Team owned configuration.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// -----------------------------------------------------------------------------
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.addAll(
            "-XXLanguage:+NestedTypeAliases",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xwhen-guards",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=com.zs.compose.theme.ExperimentalThemeApi",
            "-Xnon-local-break-continue",
            "-Xcontext-sensitive-resolution",
            "-Xcontext-parameters"
        )
    }
}

// -----------------------------------------------------------------------------
// COMPOSE COMPILER CONFIGURATION
// -----------------------------------------------------------------------------
composeCompiler {
    stabilityConfigurationFiles = listOf(
        rootProject.layout.projectDirectory.file("stability_config.conf")
    )
}

// -----------------------------------------------------------------------------
// ANDROID CONFIGURATION
// -----------------------------------------------------------------------------
android {
    // Internal source namespace is preserved for now to avoid a high-risk mass package move.
    // The published Android application ID is the AS Team package below.
    namespace = "com.zs.gallery"
    compileSdk { version = release(36) }
    buildFeatures { compose = true }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        jniLibs.keepDebugSymbols.add("**/*.so")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        // Stable AS Team package. Future releases must keep this ID unchanged so updates install
        // over previous AS Gallery versions without losing user settings.
        applicationId = "com.asteam.gallery"
        minSdk = 24
        targetSdk = 37
        versionCode = 84
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // -------------------------------------------------------------------------
    // PRODUCT FLAVORS
    // -------------------------------------------------------------------------
    flavorDimensions += "edition"
    productFlavors {
        create("standard") {
            dimension = "edition"
        }

        create("community") {
            dimension = "edition"
            applicationIdSuffix = ".community"
            versionNameSuffix = "-foss"
        }

        create("plus") {
            dimension = "edition"
            applicationIdSuffix = ".plus"
            versionNameSuffix = "-plus"
        }
    }

    // -------------------------------------------------------------------------
    // BUILD TYPES
    // -------------------------------------------------------------------------
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "launcher_label", "AS Gallery Debug")
            versionNameSuffix = "-debug"
        }
    }
}

// -----------------------------------------------------------------------------
// APP DEPENDENCIES
// -----------------------------------------------------------------------------
dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.navigation.compose)
    implementation(libs.toolkit.theme)
    implementation(libs.toolkit.foundation)
    implementation(libs.toolkit.preferences)

    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.google.fonts)

    implementation(libs.telephoto.zoomable)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.koin)
    implementation(libs.chrisbanes.haze)
    implementation(libs.lottie.compose)

    implementation(libs.bundles.coil)
    implementation(libs.bundles.icons)

    // JVM unit tests under app/src/test.
    testImplementation("junit:junit:4.13.2")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
