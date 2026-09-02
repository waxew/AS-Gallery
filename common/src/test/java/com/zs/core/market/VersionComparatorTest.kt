package com.zs.core.market

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** تست‌های منطق Version Checker بدون نیاز به دستگاه اندرویدی. */
class VersionComparatorTest {

    @Test
    fun newerPatchIsDetected() {
        assertTrue(isVersionNewer("v1.1.1", "1.1.0"))
    }

    @Test
    fun newerMinorIsDetected() {
        assertTrue(isVersionNewer("1.2.0", "1.1.9"))
    }

    @Test
    fun equalVersionIsNotNewer() {
        assertFalse(isVersionNewer("v1.1.0", "1.1.0"))
    }

    @Test
    fun olderVersionIsNotNewer() {
        assertFalse(isVersionNewer("1.0.9", "1.1.0"))
    }

    @Test
    fun prereleaseSuffixDoesNotBreakStableComparison() {
        assertTrue(isVersionNewer("v2.0.0-beta01", "1.9.9"))
    }
}
