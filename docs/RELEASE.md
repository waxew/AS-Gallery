# AS Gallery Release Procedure

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
./gradlew clean :common:testCommunityDebugUnitTest :app:testCommunityDebugUnitTest   :app:assembleCommunityDebug :app:assembleCommunityRelease
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
