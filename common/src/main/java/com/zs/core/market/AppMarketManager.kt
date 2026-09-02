package com.zs.core.market

import android.app.Activity
import com.zs.core.AppMarketManagerImpl

/**
 * قرارداد مستقل بررسی به‌روزرسانی و Review.
 * پیاده‌سازی فعلی AS Gallery به جای حساب فروشگاهی پروژه upstream از GitHub Releases استفاده می‌کند.
 */
interface AppMarketManager {

    companion object {
        const val UPDATE_NOT_AVAILABLE = -1f
        const val UPDATE_DOWNLOADED = -2f
        const val UPDATE_AVAILABLE = -3f
        const val UPDATE_NOT_SUPPORTED = -4f

        // برای سازگاری با پیاده‌سازی‌های فروشگاهی آینده حفظ شده است.
        const val FLEXIBLE_UPDATE_MAX_STALENESS_DAYS = 2

        const val ACTION_IGNORE = 0
        const val ACTION_INSTALL = 1

        operator fun invoke(): AppMarketManager = AppMarketManagerImpl()
    }

    suspend fun initiateReviewFlow(activity: Activity)

    suspend fun initiateUpdateFlow(
        activity: Activity,
        provider: suspend (result: Float) -> Int
    )
}
