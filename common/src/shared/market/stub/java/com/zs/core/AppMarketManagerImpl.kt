/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 02-10-2024.
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

package com.zs.core

import android.app.Activity
import com.zs.core.market.AppMarketManager
import com.zs.core.market.isVersionNewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * پیاده‌سازی مستقل بازار برای AS Gallery.
 *
 * در نسخه فعلی هیچ Play Billing/Review یا Firebase SDK فعال نیست. بررسی نسخه فقط متادیتای
 * آخرین GitHub Release را می‌خواند و هیچ فایل رسانه‌ای یا داده شخصی کاربر ارسال نمی‌شود.
 */
internal class AppMarketManagerImpl : AppMarketManager {

    override suspend fun initiateReviewFlow(activity: Activity) {
        // Review فروشگاهی تا زمان تعریف کانال رسمی AS Team عمداً no-op است.
    }

    override suspend fun initiateUpdateFlow(
        activity: Activity,
        provider: suspend (result: Float) -> Int
    ) {
        val currentVersion = activity.packageManager
            .getPackageInfo(activity.packageName, 0)
            .versionName
            .orEmpty()

        val latestVersion = runCatching { fetchLatestVersion() }.getOrNull()
        if (latestVersion == null) {
            provider(AppMarketManager.UPDATE_NOT_SUPPORTED)
            return
        }

        if (isVersionNewer(latestVersion, currentVersion))
            provider(AppMarketManager.UPDATE_AVAILABLE)
        else
            provider(AppMarketManager.UPDATE_NOT_AVAILABLE)
    }

    /** آخرین tag پایدار ریپو را از API عمومی GitHub می‌خواند. */
    private suspend fun fetchLatestVersion(): String? = withContext(Dispatchers.IO) {
        val connection = (URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "AS-Gallery-Android")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        }

        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(body).optString("tag_name").takeIf { it.isNotBlank() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val LATEST_RELEASE_API =
            "https://api.github.com/repos/waxew/AS-Gallery/releases/latest"
    }
}
