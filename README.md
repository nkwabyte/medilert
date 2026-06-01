# MediLert

A cross-platform medication reminder and management app built with **Kotlin Multiplatform** and **Compose Multiplatform**. A single shared codebase targets both Android and iOS, with Firebase powering authentication, data storage, push notifications, and crash reporting, and Cloudinary handling all media (profile photos).

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start — Credentials & Config Files](#quick-start--credentials--config-files)
  - [Step 1 · Copy the local.properties template](#step-1--copy-the-localproperties-template)
  - [Step 2 · Extract Firebase config from info.zip](#step-2--extract-firebase-config-from-infozip)
  - [Step 3 · Fill in credentials from cred.txt](#step-3--fill-in-credentials-from-credtxt)
- [Android Setup](#android-setup)
- [iOS Setup](#ios-setup)
- [Building and Running](#building-and-running)
- [App Architecture](#app-architecture)
- [Screens and Navigation](#screens-and-navigation)
- [Permissions](#permissions)
- [Security Notes](#security-notes)

---

## Overview

MediLert helps patients track their medications and caregivers monitor the people in their care. Users can add medications with custom schedules, receive push notification reminders at morning, afternoon, and evening intervals, and review their adherence history. Caregivers get a dedicated dashboard with weekly and daily dose statistics for each assigned patient.

The app supports two user roles established during onboarding:

- **Patient** — manages their own medication schedule
- **Caregiver** — monitors one or more assigned patients (Doctor / Pharmacist / Guardian)

---

## Features

- Email/password and phone number (OTP) authentication via Firebase Auth
- Google Sign-In (Android, via Credential Manager)
- Medication management — add, edit, and delete medications with step-by-step wizard
- Customisable reminder schedule — morning, afternoon, and evening dose times
- Push notifications via Firebase Cloud Messaging
- Dose adherence tracking with history view and weekly statistics
- Caregiver dashboard with per-patient dose monitoring
- Profile management with photo upload (Cloudinary CDN — `medilert/profiles/`)
- Dark mode and adjustable text size
- Language preference with Ghanaian language support (Twi, Ga, Ewe, Dagbani)
- Haptic / vibration feedback
- PIN setup and reset for app lock
- Voice accessibility toggle
- Privacy policy and app version screens
- Crash reporting via Firebase Crashlytics
- Analytics via Firebase Analytics

---

## Tech Stack

| Layer | Technology | Version |
| --- | --- | --- |
| Language | Kotlin | 2.3.21 |
| UI | Compose Multiplatform | 1.11.0 |
| Architecture | MVVM + shared ViewModels | — |
| Lifecycle / ViewModel | JetBrains Lifecycle KMP | 2.10.0 |
| Firebase (KMP) | gitlive-firebase | 2.4.0 |
| Firebase (Android native) | Firebase BOM | 34.13.0 |
| Media storage | Cloudinary (direct HTTP API) | — |
| Image loading | Coil Compose | 2.7.0 |
| Async | Kotlin Coroutines | 1.11.0 |
| Date / Time | kotlinx-datetime | 0.8.0 |
| Serialization | kotlinx-serialization | 1.11.0 |
| Persistent settings | multiplatform-settings | 1.3.0 |
| Google Sign-In (Android) | Credential Manager | 1.6.0 |
| iOS dependency manager | CocoaPods | — |
| Min Android SDK | 24 (Android 7.0) | — |
| Min iOS version | 16.0 | — |
| Target / Compile SDK | 36 | — |

---

## Project Structure

```text
medilert/
├── app/                          # KMP module (shared + Android)
│   ├── src/
│   │   ├── commonMain/           # Shared Kotlin/Compose code
│   │   │   ├── kotlin/com/nkwabyte/medilert/
│   │   │   │   ├── data/         # Firebase services, Cloudinary, repositories
│   │   │   │   ├── model/        # Domain models (User, Medication, etc.)
│   │   │   │   ├── navigation/   # AppDestination + AppNavigation
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/  # Reusable composables
│   │   │   │   │   ├── screens/     # All app screens
│   │   │   │   │   └── theme/       # Material 3 theme, colours, typography
│   │   │   │   ├── util/         # HapticFeedback, GhanaianPhrases, etc.
│   │   │   │   └── viewmodel/    # Shared ViewModels
│   │   │   └── composeResources/ # Shared drawables, fonts
│   │   ├── androidMain/          # Android-specific entry point + services
│   │   └── iosMain/              # iOS entry point (MainViewController)
│   ├── build.gradle.kts
│   └── app.podspec
├── iosApp/                       # Xcode project (thin shell)
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── local.properties.template     # ← copy this → local.properties and fill in values
├── info.zip                      # ← Firebase config files (gitignored once extracted)
├── cred.txt                      # ← credential reference (gitignored)
└── README.md
```

---

## Prerequisites

### Both platforms

- **JDK 17** — required by the Gradle build
- **Android Studio Ladybug (2024.2) or later** — with the Kotlin Multiplatform plugin installed
- A **Firebase project** with the following services enabled:
  - Authentication (Email/Password, Phone, Google)
  - Firestore
  - Cloud Messaging
  - Crashlytics
  - Analytics
- A **Cloudinary account** for media storage — [cloudinary.com](https://cloudinary.com) (free tier is sufficient)

### Android only

- Android SDK 36 installed via Android Studio SDK Manager

### iOS only

- **macOS** with **Xcode 15 or later**
- **CocoaPods** — install via `sudo gem install cocoapods`
- **Ruby ≥ 2.7** (ships with macOS; upgrade with `rbenv` if needed)

---

## Quick Start — Credentials & Config Files

The project requires the following files before it will build:

| File | Purpose | Tracked in git? |
| --- | --- | --- |
| `local.properties` | SDK path + all secret keys | **No** — gitignored |
| `local.properties.template` | Skeleton with placeholders | **Yes** — copy this |
| `app/google-services.json` | Firebase Android config | **No** — gitignored |
| `iosApp/iosApp/GoogleService-Info.plist` | Firebase iOS config | **No** — gitignored |
| `info.zip` | Archive containing both Firebase files | **Yes** — extract this |
| `cred.txt` | Actual credential values for quick copy-paste | **No** — gitignored |

Follow the three steps below in order.

---

### Step 1 · Copy the `local.properties` template

```bash
cp local.properties.template local.properties
```

`local.properties` is already gitignored — it will never be committed. The template shows every key the build expects, with inline comments explaining where each value comes from:

```properties
sdk.dir=<absolute-path-to-your-android-sdk>

GOOGLE_WEB_CLIENT_ID=<your-google-web-client-id>

CLOUDINARY_CLOUD_NAME=<your-cloudinary-cloud-name>
CLOUDINARY_API_KEY=<your-cloudinary-api-key>
CLOUDINARY_API_SECRET=<your-cloudinary-api-secret>
```

Android Studio will write `sdk.dir` automatically the first time you open the project. The remaining four keys must be filled in manually — see [Step 3](#step-3--fill-in-credentials-from-credtxt) below.

---

### Step 2 · Extract Firebase config from `info.zip`

`info.zip` is committed to the repo root and contains both Firebase config files. Extract it once from the repo root:

```bash
unzip info.zip
```

This produces two files:

```text
google-services.json        # Firebase Android config
GoogleService-Info.plist    # Firebase iOS config
```

Place each file in its required location:

**Android** — copy into the `app/` directory:

```bash
cp google-services.json app/google-services.json
```

**iOS** — copy into the Xcode target directory:

```bash
cp GoogleService-Info.plist iosApp/iosApp/GoogleService-Info.plist
```

> Both destination paths are gitignored. The build will fail without them — Gradle and Xcode both require these files to resolve Firebase services.

---

### Step 3 · Fill in credentials from `cred.txt`

`cred.txt` (gitignored) stores all active credential values in one place for easy copy-paste. Open it:

```bash
cat cred.txt
```

You will see output similar to:

```text
── Cloudinary ──────────────────────────────────────────────────────────────────
Cloud name : dtmzbg1aw
API Key    : <key>
API Secret : <secret>
Full URL   : cloudinary://<key>:<secret>@dtmzbg1aw
Dashboard  : https://console.cloudinary.com

── Firebase ────────────────────────────────────────────────────────────────────
Config files live in info.zip (tracked in git).
```

Copy the three Cloudinary values into `local.properties`:

```properties
CLOUDINARY_CLOUD_NAME=dtmzbg1aw
CLOUDINARY_API_KEY=<API Key from cred.txt>
CLOUDINARY_API_SECRET=<API Secret from cred.txt>
```

For `GOOGLE_WEB_CLIENT_ID`, open the [Firebase Console](https://console.firebase.google.com) and navigate to Authentication → Sign-in method → Google → Web SDK configuration → Web client ID.

The value ends in `.apps.googleusercontent.com`. Paste it into `local.properties`:

```properties
GOOGLE_WEB_CLIENT_ID=<paste-web-client-id-here>
```

All four values are read by `build.gradle.kts` and injected into `BuildConfig` at compile time — they never appear in committed source code.

---

## Android Setup

### Android step 1 · Clone the repository

```bash
git clone https://github.com/NkwaByte/medilert.git
cd medilert
```

### Android step 2 · Set up credentials and config files

Run the Quick Start steps from the section above:

```bash
# 1. Create local.properties from the template
cp local.properties.template local.properties

# 2. Extract and place the Firebase config
unzip info.zip
cp google-services.json app/google-services.json

# 3. Open local.properties and fill in keys using values from cred.txt
```

### Android step 3 · Open in Android Studio

Open the repo root as a project. Gradle sync runs automatically. If prompted, trust the project and allow Gradle to download dependencies.

### Android step 4 · Run

Select the `app` run configuration and target a device or emulator running **API 24 (Android 7.0) or higher**.

---

## iOS Setup

iOS setup requires macOS and Xcode. Run all commands from the **repo root** unless noted otherwise.

### iOS step 1 · Clone the repository

```bash
git clone https://github.com/NkwaByte/medilert.git
cd medilert
```

### iOS step 2 · Set up credentials and config files

```bash
# 1. Create local.properties from the template
cp local.properties.template local.properties

# 2. Extract and place the Firebase config
unzip info.zip
cp GoogleService-Info.plist iosApp/iosApp/GoogleService-Info.plist

# 3. Fill in local.properties using values from cred.txt
```

### iOS step 3 · Generate the KMP framework and install Pods

```bash
./gradlew :app:podInstall
```

> If CocoaPods reports a CDN issue, run `pod repo update` first.

### iOS step 4 · Open the Xcode workspace

Always open the `.xcworkspace`, **not** the `.xcodeproj`:

```bash
open iosApp/iosApp.xcworkspace
```

### iOS step 5 · Configure signing

In Xcode, select the `iosApp` target → **Signing & Capabilities** → choose your development team.

### iOS step 6 · Build and run

Select your target device or simulator and press **Run** (`Cmd+R`).

---

## Building and Running

### Android builds

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK
./gradlew :app:assembleRelease

# Install debug build on a connected device
./gradlew :app:installDebug
```

### iOS builds

```bash
# Compile Kotlin → iOS arm64 (physical device)
./gradlew :app:compileKotlinIosArm64

# Compile Kotlin → iOS Simulator (Apple Silicon Mac)
./gradlew :app:compileKotlinIosSimulatorArm64

# Full pod install (run after adding or upgrading Firebase pods)
./gradlew :app:podInstall
```

Then build and run from Xcode.

---

## App Architecture

MediLert follows **MVVM** with shared ViewModels across both platforms.

```text
UI (Compose screens)
    │
    ▼
ViewModels  (androidx.lifecycle, KMP)
    │  StateFlow / collectAsState
    ▼
Services / Repositories  (pure Kotlin, platform-agnostic)
    │
    ├── Firebase (via gitlive-firebase KMP wrappers)
    │     ├── Firestore  — user profiles, medications, schedules
    │     ├── Auth       — authentication state
    │     └── Messaging  — push notification tokens
    │
    └── Cloudinary (direct HTTP API, no SDK)
          └── Media CDN — profile photos → medilert/profiles/
```

**Key files:**

| File | Purpose |
| --- | --- |
| `MainActivity.kt` | Android entry point; observes dark-mode + font-scale |
| `AppNavigation.kt` | Root composable — custom back-stack navigation |
| `AppDestination.kt` | Sealed interface defining all navigation destinations |
| `NavViewModel.kt` | Manages the in-memory navigation back-stack |
| `AppViewModel.kt` | Current user, login state, dark mode, font scale, language |
| `AuthViewModel.kt` | Authentication flows (sign-in, sign-up, OTP, Google) |
| `MedicationViewModel.kt` | Medication CRUD and schedule management |
| `CaregiverViewModel.kt` | Caregiver dashboard — patient list, dose statistics |
| `CloudinaryService.kt` | Signed multipart upload to Cloudinary CDN |
| `HapticFeedback.kt` | Device vibration helper (success / error / light pulses) |
| `GhanaianPhrases.kt` | Time-of-day greetings in Twi, Ga, Ewe, Dagbani |

---

## Screens and Navigation

| Section | Screens |
| --- | --- |
| Splash | SplashScreen (full-image landing + auth routing) |
| Onboarding | OnboardingScreen1, OnboardingScreen2 |
| Auth | Login, SignUp, ForgotPassword, VerifyOtp, NewPassword |
| Account setup | VerifyPhone, Language, VoiceAccessibility, PersonalInfo, UserRole, SetPin, ConfirmPin, ForgetPin, ResetPin, UserProfileComplete |
| Patient dashboard | DashboardScreen, DashboardEmptyScreen |
| Caregiver dashboard | CareGiverDashboardScreen, CaregiverAddPatientScreen |
| Medication | AddMedicationStep1–4, EditMedicationScreen, MedicationHistoryScreen |
| Reminders | ReminderScreen (morning / afternoon / evening) |
| Profile | ProfileScreen (Cloudinary photo upload), ProfilePhotoViewScreen |
| Settings | SettingsScreen, LanguageSettingsScreen, PrivacyPolicyScreen, AppVersionScreen |

---

## Permissions

### Android (`AndroidManifest.xml`)

| Permission | Reason |
| --- | --- |
| `INTERNET` | Firebase, Cloudinary uploads, FCM |
| `POST_NOTIFICATIONS` | Show medication reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Schedule precise dose reminders |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule alarms after device restart |
| `VIBRATE` | Haptic feedback on dose actions |
| `CAMERA` | Capture profile photo |
| `READ_EXTERNAL_STORAGE` (≤ API 32) | Pick profile photo from gallery |

### iOS (`Info.plist`)

| Key | Reason |
| --- | --- |
| `NSCameraUsageDescription` | Capture or update profile photo |

---

## Security Notes

- **`local.properties`** — gitignored. Contains all secret keys. Never commit it.
- **`cred.txt`** — gitignored. Convenient single-file reference for the active credential set. Never commit it.
- **`google-services.json` / `GoogleService-Info.plist`** — gitignored. Distributed via `info.zip` (which is committed) so collaborators can extract them without exposing raw config in git history.
- **`local.properties.template`** — committed. Safe to share — contains only placeholder text and zero real values.
- **Cloudinary API Secret** — currently embedded in the APK via `BuildConfig`. This is acceptable for development but should be moved to a **server-side signing function** (e.g. a Firebase Cloud Function) before a production release. The client would request a time-limited signature from the function and use an unsigned upload preset instead of the raw secret.
- **Google Web Client ID** — not a secret; safe to embed in the client. It appears in `BuildConfig` for convenience but does not grant any privileged access on its own.
