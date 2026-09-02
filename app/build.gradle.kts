import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// SECURE RELEASE SIGNING
// -----------------------------------------------------------------------------
// کلید انتشار هرگز داخل ریپو قرار نمی‌گیرد. در CI/سیستم توسعه، مسیر keystore و رمزها از
// environment دریافت می‌شوند. نبود این مقادیر Build را نمی‌شکند و فقط APK release را unsigned
// نگه می‌دارد؛ برای انتشار رسمی باید هر چهار مقدار روی همان کلید ثابت مالک تنظیم شوند.
val releaseKeystorePath = System.getenv("AS_GALLERY_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("AS_GALLERY_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("AS_GALLERY_KEY_ALIAS")
val releaseKeyPassword = System.getenv("AS_GALLERY_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

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

android {
    // Namespace داخلی فعلاً حفظ شده است؛ applicationId عمومی و پایدار متعلق به AS Team است.
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
        // این شناسه در نسخه‌های بعدی نباید تغییر کند تا آپدیت روی نسخه قبلی نصب شود.
        applicationId = "com.asteam.gallery"
        minSdk = 24
        targetSdk = 37
        versionCode = 84
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword!!
                keyAlias = releaseKeyAlias!!
                keyPassword = releaseKeyPassword!!
            }
        }
    }

    flavorDimensions += "edition"
    productFlavors {
        // نسخه رسمی AS Gallery؛ package دقیقاً com.asteam.gallery باقی می‌ماند.
        create("community") {
            dimension = "edition"
        }

        // کانال‌های رزروشده برای سرویس‌های آینده AS Team.
        create("standard") {
            dimension = "edition"
            applicationIdSuffix = ".standard"
            versionNameSuffix = "-standard"
        }

        create("plus") {
            dimension = "edition"
            applicationIdSuffix = ".plus"
            versionNameSuffix = "-plus"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning)
                signingConfig = signingConfigs.getByName("release")
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

dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material3:material3")

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

    testImplementation("junit:junit:4.13.2")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
