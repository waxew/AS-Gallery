/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 03-04-2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.gallery.settings

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.zs.gallery.R
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.Route
import com.zs.preferences.Key
import com.zs.preferences.StringSaver
import com.zs.preferences.booleanPreferenceKey
import com.zs.preferences.floatPreferenceKey
import com.zs.preferences.intPreferenceKey
import com.zs.preferences.stringPreferenceKey

/** مسیر صفحه تنظیمات. */
object RouteSettings : Route

private val fontProvider by lazy {
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
}

private val _OutfitFontFamily = FontFamily("Outfit")
private val _DancingScriptFontFamily = FontFamily("Dancing Script")

val FontFamily.Companion.OutfitFontFamily get() = _OutfitFontFamily
val FontFamily.Companion.DancingScriptFontFamily get() = _DancingScriptFontFamily

/** یک خانواده فونت Google Fonts با وزن‌های متداول می‌سازد. */
@Stable
private fun FontFamily(name: String): FontFamily {
    val font = GoogleFont(name)
    return FontFamily(
        Font(fontProvider = fontProvider, googleFont = font, weight = FontWeight.Light),
        Font(fontProvider = fontProvider, googleFont = font, weight = FontWeight.Medium),
        Font(fontProvider = fontProvider, googleFont = font, weight = FontWeight.Normal),
        Font(fontProvider = fontProvider, googleFont = font, weight = FontWeight.Bold),
    )
}

/** قرارداد تغییر تنظیمات از UI. */
interface SettingsViewState {
    fun <S, O> set(key: Key<S, O>, value: O)
}

/**
 * تنظیمات پایدار برنامه.
 * نام کلیدهای قدیمی عمداً حفظ شده‌اند تا آپدیت نسخه جدید باعث از دست رفتن تنظیمات کاربر نشود.
 */
object Settings {
    const val PREFIX_MARKET_URL = "market://details?id="
    const val PREFIX_MARKET_FALLBACK = "https://play.google.com/store/apps/details?id="
    const val PKG_MARKET_ID = "com.android.vending"

    private const val PREFIX = "global"
    val STANDARD_TILE_SIZE = 100.dp

    val KEY_NIGHT_MODE = stringPreferenceKey(
        "${PREFIX}_night_mode",
        NightMode.FOLLOW_SYSTEM,
        object : StringSaver<NightMode> {
            override fun restore(value: String): NightMode = NightMode.valueOf(value)
            override fun save(value: NightMode): String = value.name
        }
    )

    val KEY_TRASH_CAN_ENABLED =
        booleanPreferenceKey(PREFIX + "_trash_can_enabled", defaultValue = true)
    val KEY_GRID_ITEM_SIZE_MULTIPLIER =
        floatPreferenceKey(PREFIX + "_grid_item_size_multiplier", defaultValue = 1.0f)
    val KEY_DYNAMIC_GALLERY =
        booleanPreferenceKey(PREFIX + "_dynamic_gallery", defaultValue = true)
    val KEY_FONT_SCALE = floatPreferenceKey(PREFIX + "_font_scale", -1f)
    val KEY_TRANSPARENT_SYSTEM_BARS = booleanPreferenceKey(
        PREFIX + "_transparent_system_bars",
        defaultValue = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    )
    val KEY_IMMERSIVE_VIEW =
        booleanPreferenceKey(PREFIX + "_immersive_view", defaultValue = false)

    val KEY_FAVOURITE_FILES = stringPreferenceKey(
        "${PREFIX}_favourite_files",
        emptyList(),
        object : StringSaver<List<Long>> {
            private val separator = ","

            override fun restore(value: String): List<Long> {
                if (value.isEmpty()) return emptyList()
                return value.split(separator).map { it.toLong() }
            }

            override fun save(value: List<Long>): String = value.joinToString(separator)
        }
    )

    val KEY_SECURE_MODE =
        booleanPreferenceKey(PREFIX + "_secure_gallery", defaultValue = false)
    val KEY_LAUNCH_COUNTER = intPreferenceKey(PREFIX + "_launch_counter", 0)
    val KEY_APP_LOCK_TIME_OUT = intPreferenceKey("${PREFIX}_app_lock_time_out", -1)
    val KEY_USE_ACCENT_IN_NAV_BAR = booleanPreferenceKey("use_accent_in_nav_bar", false)
    val KEY_DYNAMIC_COLORS = booleanPreferenceKey(
        PREFIX + "_dynamic_colors",
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    )
    val KEY_VISUAL_EFFECT_MODE = intPreferenceKey("${PREFIX}_visual_effect_mode", 0)

    val DefaultFontFamily get() = FontFamily.Default

    // همه کانال‌های ارتباطی و انتشار فقط به منابع رسمی همین ریپو اشاره می‌کنند.
    val FeedbackIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:AS.Developers.Support@Gmail.Com".toUri()
        putExtra(Intent.EXTRA_SUBJECT, "Feedback / Suggestion for AS Gallery")
    }

    val PrivacyPolicyIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/waxew/AS-Gallery/blob/main/PRIVACY_POLICY.md".toUri()
    }

    val GitHubIssuesPage = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/waxew/AS-Gallery/issues".toUri()
    }

    // تا زمانی که کانال پیام‌رسان رسمی AS Team تعریف نشده، این اکشن به ایمیل پشتیبانی می‌رود.
    val TelegramIntent = FeedbackIntent

    val GithubIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/waxew/AS-Gallery".toUri()
    }

    // نسخه Beta فروشگاهی هنوز منتشر نشده؛ Releases منبع رسمی دریافت نسخه‌هاست.
    val JoinBetaIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/waxew/AS-Gallery/releases".toUri()
    }

    val ShareAppIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "AS Gallery - private photo and video gallery\nhttps://github.com/waxew/AS-Gallery"
        )
    }

    val TranslateIntent = com.zs.core.Intent(Intent.ACTION_VIEW) {
        data = "https://github.com/waxew/AS-Gallery/issues".toUri()
    }
}
