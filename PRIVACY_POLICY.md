# AS Gallery Privacy Policy

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
