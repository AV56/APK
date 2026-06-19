# Appa Call

A large-button, high-contrast Android phone-and-contacts app designed for a
person with **Retinitis Pigmentosa (RP)** — tunnel vision, poor low-light
vision, and reduced contrast sensitivity.

## What it does
- **Always reads the phone's real contacts** (with their photos) fresh every
  time it opens — nothing to import or keep in sync.
- **Groups shown as big circles** on the home screen (e.g. Family, Doctors).
  Open a group to see its people as large rows with photo + name + a big green
  **Call** button.
- **One-tap calling** with an optional large "Call NAME?" confirmation so a
  mis-tap never dials the wrong person.
- **Search by Voice** — say a name and it finds the contact and offers to call.
- **Reads names aloud** (text-to-speech) when focused/tapped.
- **High-contrast themes** (Yellow-on-Black, White-on-Black, Black-on-White)
  and **adjustable large text**.
- **SOS button** — a big red bar that calls one chosen person instantly.

## Accessibility choices for RP
- Important targets are large and kept toward the centre of the screen (tunnel
  vision loses the edges first).
- Strong colour contrast; never relies on colour alone (text + audio too).
- Audio feedback and voice input reduce reliance on sight.
- Generous spacing between tap targets to avoid mis-taps.

## Build
No coding needed — see **HOW_TO_BUILD_AND_INSTALL.md**. In short, push to GitHub
and the included GitHub Action builds an installable APK you download from the
Actions tab. Or open in Android Studio and Run.

## Tech
- Kotlin, single module, **no external libraries** beyond AndroidX
  `core-ktx` and `appcompat` (keeps the build simple and reliable).
- UI is built in code (no XML layouts) for fewer moving parts.
- Data (groups + settings) stored locally in SharedPreferences as JSON.
- Permissions: `READ_CONTACTS`, `CALL_PHONE`.
- minSdk 26 (Android 8.0), targetSdk 34.

## Screens
- `MainActivity` — home: voice search, group circles, SOS bar, settings.
- `GroupActivity` — contacts in a group with photos and call buttons.
- `ContactPickerActivity` — choose which contacts belong to a group.
- `SettingsActivity` — colours, text size, SOS contact, options, manage groups.
