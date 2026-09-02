# AS Gallery

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
