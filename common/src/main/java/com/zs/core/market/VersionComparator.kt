package com.zs.core.market

/**
 * مقایسه نسخه‌های عددی برنامه بدون وابستگی به Android.
 * پیشوند v و پسوندهای prerelease/build metadata در مقایسه نسخه پایدار نادیده گرفته می‌شوند.
 */
internal fun isVersionNewer(candidate: String, current: String): Boolean {
    fun parts(value: String): List<Int> = value
        .trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore('-')
        .substringBefore('+')
        .split('.')
        .map { it.toIntOrNull() ?: 0 }

    val left = parts(candidate)
    val right = parts(current)
    val size = maxOf(left.size, right.size)

    repeat(size) { index ->
        val a = left.getOrElse(index) { 0 }
        val b = right.getOrElse(index) { 0 }
        if (a != b) return a > b
    }
    return false
}
