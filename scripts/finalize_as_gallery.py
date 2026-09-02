#!/usr/bin/env python3
"""Final deterministic source cleanup for AS Gallery.

This script intentionally performs guarded replacements: if an expected upstream block is
missing, it exits with an error instead of silently producing a partial release source.
"""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def write(path: str, content: str) -> None:
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(content, encoding="utf-8")


def replace_required(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise SystemExit(f"Required block not found: {label}")
    return text.replace(old, new, 1)


def regex_required(text: str, pattern: str, replacement: str, label: str) -> str:
    changed, count = re.subn(pattern, replacement, text, count=1, flags=re.S)
    if count != 1:
        raise SystemExit(f"Required regex block not found exactly once: {label} ({count})")
    return changed


# ---------------------------------------------------------------------------
# Settings: remove every remaining upstream monetization/store target.
# ---------------------------------------------------------------------------
settings_path = "app/src/main/java/com/zs/gallery/settings/Settings.kt"
settings = read(settings_path)

old_rate = '''                    // RateUs
                    FilledTonalButton(
                        stringResource(R.string.rate_us),
                        icon = Icons.Outlined.RateReview,
                        onClick = {
                            if (BuildConfig.FLAVOR != BuildConfig.FLAVOR_COMMUNITY)
                                facade.launchAppStore()
                            else
                                facade.launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/iZakirSheikh/Gallery")))
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            backgroundColor = AppTheme.colors.background(
                                4.dp
                            )
                        )
                    )
'''
new_rate = '''                    // مخزن رسمی AS Gallery؛ تا زمان انتشار در فروشگاه، Rate/Release از GitHub انجام می‌شود.
                    FilledTonalButton(
                        stringResource(R.string.rate_us),
                        icon = Icons.Outlined.RateReview,
                        onClick = { facade.launch(Settings.GithubIntent) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            backgroundColor = AppTheme.colors.background(4.dp)
                        )
                    )
'''
settings = replace_required(settings, old_rate, new_rate, "Settings rate button")

old_support = '''                    // Coffee
                    Button(
                        stringResource(R.string.buy_me_a_coffee),
                        icon = Icons.Outlined.DataObject,
                        onClick = {
                            when(BuildConfig.FLAVOR){
                                BuildConfig.FLAVOR_COMMUNITY -> facade.launch(Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/sponsors/iZakirSheikh")))
                                else -> facade.initiatePurchaseFlow(Paymaster.IAP_BUY_ME_COFFEE)
                            }
                        },
                    )
'''
new_support = '''                    // کانال پشتیبانی رسمی AS Team؛ هیچ پرداخت upstream از این صفحه فراخوانی نمی‌شود.
                    Button(
                        stringResource(R.string.buy_me_a_coffee),
                        icon = Icons.Outlined.DataObject,
                        onClick = { facade.launch(Settings.FeedbackIntent) },
                    )
'''
settings = replace_required(settings, old_support, new_support, "Settings support button")

settings = replace_required(
    settings,
    '''        footer = {
            if (BuildConfig.FLAVOR == BuildConfig.FLAVOR_COMMUNITY)
                return@BaseListItem Spacer(Modifier)
            Row(''',
    '''        footer = {
            Row(''',
    "community update footer",
)
settings = settings.replace("enabled = false", "enabled = true", 1)

privacy_pref = '''    Preference(
        text = textResource(R.string.pref_privacy_policy),
        icon = Icons.Outlined.PrivacyTip,
        modifier = Modifier
            .clip(AppTheme.shapes.medium)
            .clickable { facade.launch(Settings.PrivacyPolicyIntent) },
    )
'''
contact_pref = privacy_pref + '''
    // اطلاعات تماس رسمی AS Team در About.
    Preference(
        text = textResource(R.string.support_email),
        icon = Icons.Outlined.BugReport,
        modifier = Modifier
            .clip(AppTheme.shapes.medium)
            .clickable { facade.launch(Settings.FeedbackIntent) },
    )
'''
settings = replace_required(settings, privacy_pref, contact_pref, "About contact preference")

# Defensive URL cleanup for this screen.
settings = settings.replace("https://github.com/iZakirSheikh/Gallery", "https://github.com/waxew/AS-Gallery")
settings = settings.replace("https://github.com/sponsors/iZakirSheikh", "https://github.com/waxew/AS-Gallery")
write(settings_path, settings)


# ---------------------------------------------------------------------------
# MainActivity: AS update channel, remove upstream promotional/store behavior,
# connect right-side drawer.
# ---------------------------------------------------------------------------
main_path = "app/src/main/java/com/zs/gallery/MainActivity.kt"
main = read(main_path)

main = replace_required(
    main,
    '''                return@initiateUpdateFlow  when(result){
                    AppMarketManager.UPDATE_NOT_AVAILABLE -> {''',
    '''                return@initiateUpdateFlow when (result) {
                    AppMarketManager.UPDATE_AVAILABLE -> {
                        val res = snackbarHostState.showSnackbar(
                            message = resources.getText2(R.string.msg_update_available),
                            action = resources.getText2(R.string.get),
                            duration = SnackbarDuration.Long,
                            icon = Icons.Outlined.NewReleases
                        )
                        if (res == SnackbarResult.ActionPerformed)
                            launch(Settings.JoinBetaIntent)
                        AppMarketManager.ACTION_IGNORE
                    }

                    AppMarketManager.UPDATE_NOT_AVAILABLE -> {''',
    "update available branch",
)

main = replace_required(
    main,
    '''                    AppMarketManager.UPDATE_NOT_SUPPORTED -> {
                        /*No-op*/
                        AppMarketManager.ACTION_IGNORE
                    }''',
    '''                    AppMarketManager.UPDATE_NOT_SUPPORTED -> {
                        if (report) showToast(R.string.msg_update_check_error)
                        AppMarketManager.ACTION_IGNORE
                    }''',
    "update unsupported reporting",
)

main = regex_required(
    main,
    r'''    private fun showPromoToast\(\n.*?\n    override fun onNewIntent''',
    '''    /** فقط پیام تغییرات نسخه خود AS Gallery نمایش داده می‌شود؛ تبلیغات upstream حذف شده‌اند. */
    private fun showPromoToast(index: Int, delay: Long = 1_000) {
        if (index != 0) return
        lifecycleScope.launch {
            if (delay > 0) delay(delay)
            showSnackbar(
                R.string.what_s_new_latest,
                duration = SnackbarDuration.Long,
                icon = Icons.Outlined.NewReleases
            )
        }
    }

    override fun onNewIntent''',
    "legacy promo function",
)

main = regex_required(
    main,
    r'''            // Promote media player on every 5th launch\n.*?        // Set up the window to fit the system windows''',
    '''            // نمایش What's New فقط هنگام تغییر versionCode و ثبت شمارنده اجراها.
            lifecycleScope.launch {
                val versionCode = packageManager.getPackageInfoCompat(packageName)?.versionCode ?: 0
                val savedVersionCode = preferences[KEY_APP_VERSION_CODE]
                if (savedVersionCode != versionCode) {
                    preferences[KEY_APP_VERSION_CODE] = versionCode
                    showPromoToast(0)
                }
                preferences[Settings.KEY_LAUNCH_COUNTER] =
                    preferences[Settings.KEY_LAUNCH_COUNTER] + 1
            }
        }
        // Set up the window to fit the system windows''',
    "legacy cold-start promotions",
)

old_home = '''            Home(
                when {
                    intent.action == Intent.ACTION_VIEW -> RouteIntentViewer
                    isAuthenticationRequired -> RouteLockScreen
                    else -> RouteFiles
                },
                snackbarHostState,
                navController
            )'''
new_home = '''            AsNavigationDrawer(navController) {
                Home(
                    when {
                        intent.action == Intent.ACTION_VIEW -> RouteIntentViewer
                        isAuthenticationRequired -> RouteLockScreen
                        else -> RouteFiles
                    },
                    snackbarHostState,
                    navController
                )
            }'''
main = replace_required(main, old_home, new_home, "connect AS navigation drawer")

# Remaining upstream package/store strings must not execute in AS build.
main = main.replace('launchAppStore("com.googol.android.apps.photos")', 'launch(Settings.JoinBetaIntent)')
main = main.replace('launchAppStore("com.prime.player")', 'launch(Settings.GithubIntent)')
write(main_path, main)


# ---------------------------------------------------------------------------
# Android 14+: allow either full READ_MEDIA_* OR selected visual media access.
# ---------------------------------------------------------------------------
home_path = "app/src/main/java/com/zs/gallery/Home.kt"
home = read(home_path)
if "import androidx.compose.ui.platform.LocalContext\n" not in home:
    home = replace_required(
        home,
        "import androidx.compose.ui.platform.LocalDensity\n",
        "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalDensity\n",
        "LocalContext import",
    )

marker = '''private val REQUIRED_PERMISSIONS = buildList {
    // For Android Tiramisu (33) and above, use media permissions for scoped storage
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       // this += android.Manifest.permission.ACCESS_MEDIA_LOCATION
        this += android.Manifest.permission.READ_MEDIA_VIDEO
        this += android.Manifest.permission.READ_MEDIA_IMAGES
    }
    // For Android Upside Down Cake (34) and above, add permission for user-selected visual media
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        this += android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    // For Android versions below Tiramisu 10(29), request WRITE_EXTERNAL_STORAGE for
    // legacy storage access
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
        this += android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        this += android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
'''
replacement = marker + '''
/**
 * دسترسی معتبر رسانه‌ای: در Android 14+ کاربر می‌تواند «همه رسانه‌ها» یا فقط موارد انتخابی
 * را بدهد. در نسخه‌های قدیمی همان permissionهای استاندارد قبلی بررسی می‌شوند.
 */
private fun Context.hasRequiredMediaAccess(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val fullAccess = checkSelfPermissions(
            listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        )
        val selectedAccess = androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fullAccess || selectedAccess
    }
    return checkSelfPermissions(REQUIRED_PERMISSIONS)
}
'''
home = replace_required(home, marker, replacement, "media access helper")

home = replace_required(
    home,
    '''private fun Permission() {
    val controller = LocalNavController.current
    // Compose the permission state.''',
    '''private fun Permission() {
    val controller = LocalNavController.current
    val context = LocalContext.current
    // Compose the permission state.''',
    "Permission context",
)
home = replace_required(
    home,
    '''        Permissions(permissions = REQUIRED_PERMISSIONS) {
            if (!it.all { (_, state) -> state }) return@Permissions''',
    '''        Permissions(permissions = REQUIRED_PERMISSIONS) {
            if (!context.hasRequiredMediaAccess()) return@Permissions''',
    "limited media permission callback",
)
home = replace_required(
    home,
    "                val granted = activity.checkSelfPermissions(REQUIRED_PERMISSIONS)",
    "                val granted = activity.hasRequiredMediaAccess()",
    "Home media permission start",
)
write(home_path, home)


# ---------------------------------------------------------------------------
# Launcher/splash: distinct AS Gallery vector and no upstream branding image.
# ---------------------------------------------------------------------------
as_icon = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- AS Gallery: dark rounded tile, photo frame, sun and mountain mark. -->
    <path android:fillColor="#111827" android:pathData="M18,8h72c5.52,0 10,4.48 10,10v72c0,5.52 -4.48,10 -10,10H18C12.48,100 8,95.52 8,90V18C8,12.48 12.48,8 18,8z"/>
    <path android:fillColor="#F8FAFC" android:pathData="M25,26h58c4.42,0 8,3.58 8,8v40c0,4.42 -3.58,8 -8,8H25c-4.42,0 -8,-3.58 -8,-8V34c0,-4.42 3.58,-8 8,-8z"/>
    <path android:fillColor="#2563EB" android:pathData="M24,72l17,-18 10,10 12,-14 21,22z"/>
    <path android:fillColor="#F59E0B" android:pathData="M72,38m-7,0a7,7 0,1 0,14,0a7,7 0,1 0,-14,0"/>
    <path android:fillColor="#111827" android:pathData="M30,88h48v4H30z"/>
</vector>
'''
write("app/src/main/res/drawable/ic_launcher_foreground.xml", as_icon)

manifest_path = "app/src/main/AndroidManifest.xml"
manifest = read(manifest_path)
manifest = manifest.replace('android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/ic_launcher_foreground"')
manifest = manifest.replace('android:roundIcon="@mipmap/ic_launcher_round"', 'android:roundIcon="@drawable/ic_launcher_foreground"')
write(manifest_path, manifest)

theme_path = "app/src/main/res/values/themes.xml"
theme = read(theme_path)
theme = theme.replace('<item name="windowSplashScreenAnimatedIcon">@drawable/ic_splash</item>', '<item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>')
theme = re.sub(r'\s*<item name="android:windowSplashScreenBrandingImage"[^>]*>.*?</item>', '', theme)
write(theme_path, theme)


# ---------------------------------------------------------------------------
# Brand consistency across legacy translations. Preserve translations but make
# identity/author/update labels unambiguous.
# ---------------------------------------------------------------------------
for locale_file in sorted((ROOT / "app/src/main/res").glob("values-*/strings.xml")):
    if locale_file.parent.name == "values-fa":
        continue
    text = locale_file.read_text(encoding="utf-8")
    text = re.sub(r'<string name="app_name">.*?</string>', '<string name="app_name">AS Gallery</string>', text, flags=re.S)
    text = re.sub(r'<string name="about_gallery">.*?</string>', '<string name="about_gallery">About AS Gallery</string>', text, flags=re.S)
    text = re.sub(r'<string name="update_gallery">.*?</string>', '<string name="update_gallery">Update AS Gallery</string>', text, flags=re.S)
    text = re.sub(r'<string name="pref_scr_version_by_author_s">.*?</string>', '<string name="pref_scr_version_by_author_s">v%1$s by AS Team Group</string>', text, flags=re.S)
    locale_file.write_text(text, encoding="utf-8")


# ---------------------------------------------------------------------------
# Documentation / release metadata. Upstream legal attribution is retained.
# ---------------------------------------------------------------------------
readme = '''# AS Gallery

AS Gallery is the AS Team Android photo and video gallery. It is designed for fast local media
browsing, folders, favourites, recycle-bin workflows, media viewing, biometric app lock and
Persian/RTL use.

## Release identity

- Application ID: `com.asteam.gallery`
- Current version: `1.1.0` (`versionCode 84`)
- Minimum Android: API 24
- Primary edition: `community`
- Repository: `waxew/AS-Gallery`
- Support: `AS.Developers.Support@Gmail.Com`
- Developer label: `AS Team Group`

The Kotlin namespace remains `com.zs.gallery` internally to avoid a high-risk source migration.
This does not affect the public Android application ID or update compatibility.

## Features

- Timeline and folder-based photo/video browsing
- Image/video viewer and supported media intents
- Favourites, trash/recycle-bin and restore operations
- Share, edit-in, wallpaper, Lens/Quick Share integrations when available
- Biometric app lock and secure recents mode
- Light/dark/system themes, dynamic colors and UI scaling
- Right-side AS navigation drawer with editable local profile/avatar
- Persian localization and RTL support
- Android 14+ full-media and selected-media permission modes
- GitHub Releases based version checking

## Privacy architecture

AS Gallery does not enable the upstream Firebase Analytics, Crashlytics, Ads, Play Billing or Play
Update SDKs in the AS editions. Media browsing remains local. The updater reads only public release
metadata from GitHub. See `PRIVACY_POLICY.md` for details.

## Build

Use JDK 17 and the checked-in Gradle wrapper:

```bash
./gradlew :common:testCommunityDebugUnitTest :app:testCommunityDebugUnitTest
./gradlew :app:assembleCommunityDebug
./gradlew :app:assembleCommunityRelease
```

The unsigned Release build is useful for CI verification. Official distribution must use the same
owner-controlled signing key for every future update.

## Secure release signing

Never commit a keystore. Set these environment variables:

```text
AS_GALLERY_KEYSTORE_PATH
AS_GALLERY_KEYSTORE_PASSWORD
AS_GALLERY_KEY_ALIAS
AS_GALLERY_KEY_PASSWORD
```

When all four are present, Gradle signs the `communityRelease` APK with that stable key. See
`docs/RELEASE.md` for the release procedure.

## Update policy

Keep `com.asteam.gallery` unchanged and increment `versionCode` for every release. User settings and
local app state use stable keys so normal package updates preserve them.

## CI

GitHub Actions runs unit tests, resource/code compilation and Community Debug/Release builds. Build
artifacts include APKs and SHA-256 checksum files.

## Attribution and license

AS Gallery is based on the open-source Gallery project originally developed by Zakir Sheikh and
contributors. Existing source-file copyright notices and upstream attribution are intentionally
preserved where required.

Licensed under the Apache License 2.0. See `LICENSE` for the complete terms.
'''
write("README.md", readme)

privacy = '''# AS Gallery Privacy Policy

**Effective date: September 2, 2026**

AS Gallery is an Android photo and video gallery maintained under the AS Team Group project. This
policy describes the behavior of the AS-branded builds in this repository.

## Media and storage

AS Gallery requests Android media/storage permissions only to display and manage photos and videos
that the user can access on the device. On Android 14 and newer, the app supports both full media
access and Android's selected-photos/videos access mode. Media files are not uploaded to an AS Team
server by the gallery browsing features.

## Local settings and profile

Theme, gallery preferences, favourites, security preferences, the optional drawer display name and
the selected profile image URI are stored locally using Android application storage/preferences.
The profile image is selected by the user through Android's document picker.

## Biometrics

If App Lock is enabled, authentication is performed using Android's biometric/device credential
framework. AS Gallery does not receive or store fingerprint or facial biometric templates.

## Analytics, advertising and payments

The current AS editions use local/no-op implementations for analytics, crash reporting,
advertising, billing and app-store review/update APIs. The legacy upstream Firebase/Google service
configuration is not included in AS Gallery builds.

If AS Team enables an online analytics, advertising or payment service in a future release, this
policy must be updated before that release.

## Update checks and network access

AS Gallery can request public release metadata from the GitHub API for `waxew/AS-Gallery` to tell
the user when a newer version exists. This request does not contain gallery media or the local
profile. GitHub may receive ordinary network metadata such as IP address and HTTP request metadata
under GitHub's own privacy terms.

Google Fonts or other Android/platform components may also make network requests depending on the
device and feature used.

## User-initiated external actions

Features such as Share, Quick Share, Google Lens, wallpaper apps, email support and opening GitHub
hand control to another installed app/service only after user action. Those services have their own
privacy practices.

## Data sale

AS Team Group does not sell personal gallery data through AS Gallery.

## Security and backups

Android may back up eligible app preferences according to the device/account backup configuration.
Secure Mode can hide app content from the recent-app preview. Users remain responsible for device
security and backups of their original media.

## Contact

For privacy questions, bug reports or support:

`AS.Developers.Support@Gmail.Com`

Official source and issue tracker: `https://github.com/waxew/AS-Gallery`

## Open-source attribution

AS Gallery is derived from the open-source Gallery project. Existing Apache 2.0 licensing and
copyright notices remain applicable to derived source code. See `LICENSE` and repository history.
'''
write("PRIVACY_POLICY.md", privacy)

release_doc = '''# AS Gallery Release Procedure

## 1. Version

Keep `applicationId = com.asteam.gallery`. Increment `versionCode` for every published build and set
a human-readable `versionName`.

## 2. Signing key

Create and protect one owner-controlled Android keystore. Never commit it. Configure:

- `AS_GALLERY_KEYSTORE_PATH`
- `AS_GALLERY_KEYSTORE_PASSWORD`
- `AS_GALLERY_KEY_ALIAS`
- `AS_GALLERY_KEY_PASSWORD`

The same key must sign every future update.

## 3. Verification build

```bash
./gradlew clean :common:testCommunityDebugUnitTest :app:testCommunityDebugUnitTest \
  :app:assembleCommunityDebug :app:assembleCommunityRelease
```

## 4. Checksums

```bash
sha256sum app/build/outputs/apk/community/debug/*.apk
sha256sum app/build/outputs/apk/community/release/*.apk
```

## 5. Signature verification

For an officially signed APK:

```bash
apksigner verify --verbose --print-certs app/build/outputs/apk/community/release/*.apk
```

Keep the certificate SHA-256 fingerprint with release records.

## 6. Publish

Create a GitHub Release tag matching the version, for example `v1.1.0`, attach the signed APK and
checksum, and describe changes. The in-app updater reads the latest GitHub Release tag.

## 7. Update test

Before distribution, install the previous signed version with user data, then install the new APK
over it. Confirm the app updates without uninstalling and settings/media preferences remain intact.
'''
write("docs/RELEASE.md", release_doc)

# Store-facing text (images remain separate assets and can be regenerated for a store submission).
write("fastlane/metadata/android/en-US/title.txt", "AS Gallery\n")
write("fastlane/metadata/android/en-US/short_description.txt", "Private, fast photo and video gallery by AS Team Group.\n")
write(
    "fastlane/metadata/android/en-US/full_description.txt",
    "AS Gallery is a fast Android photo and video gallery with timeline and folder browsing, "
    "favourites, recycle bin, media viewer, biometric app lock, themes, Persian/RTL support and "
    "Android 14 selected-media access. The AS build removes legacy upstream analytics, ads and "
    "billing integrations. Support: AS.Developers.Support@Gmail.Com\n",
)
write(
    "fastlane/metadata/android/en-US/changelogs/84.txt",
    "AS Gallery 1.1.0\n- AS Team branding and package identity\n- Persian/RTL support\n- Right-side AS navigation drawer and local profile\n- GitHub release update checker\n- Android 14 selected-media permission support\n- Legacy Firebase/Play billing/analytics detached\n",
)

# Old Crowdin project configuration belongs to upstream and must not receive AS strings.
crowdin = ROOT / "crowdin.yml"
if crowdin.exists():
    crowdin.unlink()

print("AS Gallery finalizer completed successfully.")
