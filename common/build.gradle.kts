import com.android.build.api.dsl.VariantDimension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// BUILD CONFIG VALUES
// -----------------------------------------------------------------------------
// این فیلد برای سازگاری API داخلی نگه داشته شده است. تا زمان راه‌اندازی Billing رسمی AS Team
// مقدار آن خالی می‌ماند و تمام Flavorها از پیاده‌سازی‌های no-op استفاده می‌کنند.
private val secrets = arrayOf("PLAY_CONSOLE_APP_RSA_KEY")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

/** Adds a string BuildConfig field to the project. */
private fun VariantDimension.buildConfigField(name: String, value: String) =
    buildConfigField("String", name, "\"" + value + "\"")

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.addAll(
            "-XXLanguage:+NestedTypeAliases",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xwhen-guards",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xnon-local-break-continue",
            "-Xcontext-sensitive-resolution",
            "-Xcontext-parameters"
        )
    }
}

android {
    // Namespace داخلی کتابخانه برای جلوگیری از مهاجرت پرریسک سورس فعلاً حفظ می‌شود.
    namespace = "com.zs.core"
    compileSdk { version = release(36) }
    buildFeatures { buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        for (secret in secrets)
            buildConfigField(secret, System.getenv(secret) ?: "")

        // ثابت‌های Flavor در کد برنامه استفاده می‌شوند؛ بنابراین حتی در حالت بدون فروشگاه حفظ می‌شوند.
        buildConfigField("FLAVOR_COMMUNITY", "community")
        buildConfigField("FLAVOR_STANDARD", "standard")
        buildConfigField("FLAVOR_PLUS", "plus")
        buildConfigField("FLAVOR_PREMIUM", "premium")
    }

    flavorDimensions += "edition"
    productFlavors {
        create("community") { dimension = "edition" }
        create("standard") { dimension = "edition" }
        create("plus") { dimension = "edition" }
    }

    // تا زمانی که سرویس‌های متعلق به AS Team تعریف نشده‌اند، هر سه Flavor از پیاده‌سازی‌های
    // محلی و بدون telemetry/payment/store استفاده می‌کنند. این کار جلوی اتصال ناخواسته به
    // Firebase، Google Billing و Play account پروژه upstream را می‌گیرد.
    sourceSets {
        listOf("community", "standard", "plus").forEach { edition ->
            getByName(edition) {
                java.srcDirs(
                    "src/shared/analytics/stub/java",
                    "src/shared/billing/stub/java",
                    "src/shared/ads/stub/java",
                    "src/shared/market/stub/java"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    api(libs.bundles.coil)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.media3)

    // تست منطق‌های pure JVM مانند Version Comparator.
    testImplementation("junit:junit:4.13.2")

    // Firebase Analytics، Google Billing و Play Update/Review عمداً حذف شده‌اند.
    // هر سرویس آنلاین آینده باید با حساب و کلیدهای رسمی AS Team دوباره اضافه شود.
}
