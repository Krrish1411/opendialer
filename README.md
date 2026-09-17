# OpenDialer 📱

A modern, high-performance, open-source Android dialer and phone application built with **Jetpack Compose**, **Material 3**, **Hilt**, and **Room**. Designed for personal use and GitHub hosting with privacy-first principles—no cloud sync, no tracking, no ads, and no Play Store billing.

---

## ✨ Flagship Highlights & Features

- **🎨 Dynamic Theme Engine (6 Inspired Themes)**:
  - **Aura Nebula Modern (Flagship)**: Ultra-sleek, sticky fluid UI with electric violet, magnetic cyan/teal, and glassmorphic card styling.
  - **Pixel Material**: Clean Google Material You with subtle dynamic accents.
  - **OneUI Modern**: Samsung One UI-inspired layout with generous reachability curves and squircle controls.
  - **Cupertino Glass**: iPhone-inspired grouped style with circular dials and frosted tab navigation.
  - **Android Classic**: High-contrast, dense utility design with teal accents.
  - **AMOLED Pitch Black**: Pure `#000000` dark theme with neon accents for maximum OLED battery savings.
  - **Dynamic Color (Monet)**: Material You palette extraction from wallpaper on Android 12+.

- **🔄 In-App OTA Updates (Direct Updates Without Uninstalling)**:
  - Continuous release signing keystore configuration in GitHub Actions CI ensures every release is signed with the exact same certificate.
  - Integrated in-app update checker queries GitHub Releases API (`api.github.com/repos/{owner}/opendialer/releases/latest`).
  - Downloads and installs newer versions in-place via Android's `FileProvider` without uninstalling, preserving all call logs, favorites, recordings, and settings!

- **📞 Complete Dual SIM Telephony**:
  - Automatically queries active SIM subscriptions via `SubscriptionManager`.
  - Discovers call-capable phone accounts via `TelecomManager` (`PhoneAccountHandle`).
  - Outgoing SIM chooser with global default mode ("Always Ask", "SIM 1", "SIM 2").
  - Remembers last-used SIM card per contact or dialed number.

- **🎙️ Working Call Recorder (Multi-Engine Fallback)**:
  - Bypasses Android 9/10+ call stream blocking using a specialized foreground service (`FOREGROUND_SERVICE_MICROPHONE`).
  - Optional **Speakerphone Boost**: Automatically routes or boosts speakerphone audio during active recording so both caller and receiver audio are captured cleanly into high-fidelity AAC `.m4a` files.
  - Full Room metadata tracking, built-in waveform player, share via Android Sharesheet, and legal consent banner.

- **⚙️ Advanced Carrier & Call Features**:
  - **Call Forwarding**: Unconditional, When Busy, When Unanswered, and When Unreachable with carrier MMI execution (`*21*`, `*67*`, etc.).
  - **Call Barring**: Outgoing international, roaming, and incoming barring.
  - **Call Waiting**: Status check and toggle (`*43#`).
  - **Do Not Disturb & VIP Mode**: Integrated DND toggle, "Silence Unknown Callers", and VIP whitelist for favorite contacts.

- **⚡ Fast T9 Keypad & Speed Dial**:
  - Full 3x4 dial pad with haptic feedback.
  - Smart T9 search matching letters and digits against your contacts in real-time.
  - Long press `0` for `+` international prefix.
  - Long press digits `1` through `9` for instant speed dial!

---

## 🚀 Zero-Disk-Wear Cloud Builds (GitHub Actions)

**You do NOT need to install the Android SDK or Gradle dependencies on your computer!**

This repository comes pre-configured with a GitHub Actions CI/CD pipeline (`.github/workflows/build-apk.yml`). Every time you push code or a tag to GitHub, GitHub's cloud runners will:
1. Spin up a clean Ubuntu virtual machine.
2. Download Android SDK 34 and JDK 17 in the cloud.
3. Sign the APK with a consistent release key (so you can update without uninstalling).
4. Produce downloadable `opendialer-debug.apk` and `opendialer-release.apk` artifacts under the **Actions** tab of your repository.

### How to Create the GitHub Repository & Push:

```bash
# 1. Initialize remote
git remote add origin https://github.com/Krrish1411/opendialer.git

# 2. Add and commit all files
git add .
git commit -m "Initial commit: OpenDialer flagship release"

# 3. Push to main
git push -u origin main
```

Once pushed, go to `https://github.com/Krrish1411/opendialer/actions` and download your freshly compiled APK!

---

## 🛠️ Project Structure

```
opendialer/
├── .github/workflows/build-apk.yml      # Cloud CI/CD build & APK generator
├── gradle/
│   ├── wrapper/gradle-wrapper.properties
│   └── libs.versions.toml               # Version Catalog
├── build.gradle.kts                     # Root build script
├── settings.gradle.kts                  # Project settings
├── gradlew                              # Gradle wrapper script
└── app/
    ├── build.gradle.kts                 # Application module build configuration
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml          # InCallService, permissions, FileProvider
        ├── res/                         # Strings, colors, styles, accessibility config
        └── java/com/opendialer/app/
            ├── DialerApplication.kt     # Application class & Notification Channels
            ├── MainActivity.kt          # Entry point & compose shell
            │
            ├── core/
            │   ├── common/              # DateTime, PhoneNumber, T9 utilities
            │   ├── designsystem/        # Theme engine, Aura/Pixel/OneUI/iOS themes, UI components
            │   ├── telephony/           # SimManager, CallManager, MMI carrier helpers
            │   └── updater/             # GitHub Releases OTA updater
            │
            ├── data/
            │   ├── local/               # Room Database, DAOs, Entities
            │   ├── repository/          # Contacts, CallLog, Favorites, Recordings, Settings
            │   └── model/               # Data transfer models
            │
            ├── services/
            │   ├── DialerInCallService.kt              # Telecom InCallService
            │   ├── DialerCallScreeningService.kt       # Spam & DND screening
            │   ├── CallRecorderService.kt              # Foreground audio recorder
            │   └── CallAccessibilityRecorderService.kt # Fallback stream capture
            │
            ├── ui/
            │   ├── navigation/          # Tabs & NavHost
            │   ├── permissions/         # Role Dialer prompt & runtime permissions
            │   └── screens/
            │       ├── keypad/          # Dial pad & T9 search
            │       ├── recents/         # Call logs & filters
            │       ├── contacts/        # Contacts list & lookup
            │       ├── favorites/       # Starred grid & speed dial 1-9
            │       ├── recordings/      # Local recordings & audio player
            │       ├── incall/          # Full-screen lockscreen in-call activity
            │       └── settings/        # Themes, Dual SIM, Forwarding, DND, Updates
            │
            └── di/                      # Hilt dependency injection modules
```

---

## 📋 Manual Testing Checklist

| Test Item | Steps | Expected Result |
|---|---|---|
| **Default Dialer Setup** | Launch app -> Tap "Set as Default" banner | System dialog prompts to set OpenDialer as default phone app. |
| **Outgoing Call** | Enter number on Keypad -> Tap Call button | If 2 SIMs exist, prompts for SIM selection (or uses default). Call is initiated. |
| **Incoming Call (Unlocked)** | Receive call while using phone | High-priority notification banner appears with Answer and Reject actions. |
| **Incoming Call (Locked)** | Lock screen -> Receive incoming call | `IncomingCallActivity` wakes screen with pulsing avatar, caller info, and green/red buttons. |
| **Call Recording** | During active call -> Tap "Record" | Foreground service starts with ongoing notification; audio is saved to `.m4a` with Room entry. |
| **Theme Switching** | Settings -> Theme -> Select "Aura Nebula" or "OneUI" | Entire app updates colors, cards, and corner radii immediately without restart. |
| **In-Place OTA Update** | Settings -> Check for Updates | Queries GitHub; downloads APK and triggers system package installer on top of existing version. |
| **Call Forwarding / Waiting**| Settings -> Call Forwarding -> Enter number -> Enable | Executes carrier MMI code (`*21*<number>#`) on the selected SIM. |

---

## 🔒 Privacy & Legal Compliance

- **No Analytics / Telemetry**: Absolutely no tracking or crash analytics libraries are bundled.
- **Local Storage**: All call records, logs, and preferences are stored exclusively on your device in Room SQLite database and app-private storage.
- **Recording Consent**: Complies with privacy laws by providing explicit user consent dialogs and local-only audio capture.
