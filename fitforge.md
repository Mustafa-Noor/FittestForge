# FitForge — Complete Agent Build Guide
### *"Built for humans, not habit streaks."*

---

> **Start fresh. New Android project. This is not a migration — this is a full rebuild.**
> This document is the single source of truth. Read every section before writing any code.

---

## THE STORY (Read this first — it drives every design decision)

Every major fitness app — MyFitnessPal, Strong, Hevy — was built around one idea: **track numbers and punish failure.** Miss a day, your streak dies. Skip a week, you open the app to a wall of zeroes. Research published in the *British Journal of Health Psychology* (UCL, 2025) proved this actively causes shame and dropout.

**FitForge solves the three problems nobody has fixed:**

1. **Momentum, not streaks** — Missing one day doesn't erase your progress. A Momentum Bar decays slowly. You never go to zero from a single skip. This is psychologically the right model.
2. **Personality Mode** — You choose your coach's tone (Hype Man, Drill Sergeant, Chill Coach, Chaos Goblin). Every message, notification, and AI response adapts. No other fitness app personalizes *how it speaks* to you.
3. **The Recovery Log** — "Life Happened" button. Exam week, sick, travel — all protected. Not a failure, a logged reality. The app respects your life instead of grading it.

**+ AI Coach powered by Gemini** — Your real personal trainer, inside the app, free.

This is the story you present to the evaluator. Build every screen with this story in mind.

---

## TABLE OF CONTENTS

1. [Tech Stack & Project Setup](#1-tech-stack--project-setup)
2. [Firebase Setup](#2-firebase-setup)
3. [External APIs](#3-external-apis)
4. [Design System — Light Theme](#4-design-system--light-theme)
5. [App Architecture](#5-app-architecture)
6. [Project File Structure](#6-project-file-structure)
7. [Firebase Database Schema](#7-firebase-database-schema)
8. [Authentication — Login & Signup](#8-authentication--login--signup)
9. [Splash Screen & Onboarding](#9-splash-screen--onboarding)
10. [Bottom Navigation Structure](#10-bottom-navigation-structure)
11. [Home Tab — Dashboard](#11-home-tab--dashboard)
12. [Workout Tab — Log & Library](#12-workout-tab--log--library)
13. [Progress Tab — Charts & DNA](#13-progress-tab--charts--dna)
14. [AI Coach Tab — Gemini](#14-ai-coach-tab--gemini)
15. [Profile Tab — User Management](#15-profile-tab--user-management)
16. [Momentum System Implementation](#16-momentum-system-implementation)
17. [Personality Mode System](#17-personality-mode-system)
18. [Recovery Log — Life Happened](#18-recovery-log--life-happened)
19. [Lottie Animations Guide](#19-lottie-animations-guide)
20. [Exercise GIF Animations](#20-exercise-gif-animations)
21. [Audio System](#21-audio-system)
22. [Notifications](#22-notifications)
23. [Celebration Screen — Post Workout](#23-celebration-screen--post-workout)
24. [Build Order](#24-build-order)
25. [Agent Rules](#25-agent-rules)

---

## 1. Tech Stack & Project Setup

### Create New Project
- **Project Name:** FitForge
- **Package:** `com.fitforge.app`
- **Language:** Kotlin
- **Min SDK:** API 26 (Android 8.0)
- **Target SDK:** API 35
- **Build System:** Gradle (Kotlin DSL)
- **Template:** Empty Views Activity (AppCompatActivity + XML)

### Core dependencies — `build.gradle.kts` (app level)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")          // Firebase
    id("org.jetbrains.kotlin.plugin.serialization") // JSON
    id("kotlin-parcelize")
}

dependencies {
    // ── Android Core ──────────────────────────────────────
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ── Lifecycle ─────────────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-ktx:1.9.3")

    // ── Navigation Component ──────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")

    // ── Firebase ──────────────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ── Networking ────────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // ── Image Loading ─────────────────────────────────────
    implementation("com.github.bumptech.glide:glide:4.16.0")           // for exercise GIFs
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ── Lottie Animations ─────────────────────────────────
    implementation("com.airbnb.android:lottie:6.5.2")

    // ── Coroutines ────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ── Charts (Progress tab) ─────────────────────────────
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ── Circular Progress (Momentum bar) ─────────────────
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")

    // ── Serialization ─────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ── Gemini AI SDK ─────────────────────────────────────
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // ── SharedPreferences (DataStore NOT needed — standard SharedPrefs) ──
    implementation("androidx.preference:preference-ktx:1.2.1")

    // ── Image Picker (for profile photo) ─────────────────
    implementation("com.github.dhaval2404:imagepicker:2.1")

    // ── Dots Indicator (for onboarding pager) ─────────────
    implementation("com.tbuonomo:dotsindicator:5.0")

    // ── Shimmer loading effect ────────────────────────────
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // ── Test ──────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.espresso:espresso-core:3.6.1")
}
```

### `settings.gradle.kts` — add JitPack for MPAndroidChart & ImagePicker
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### `build.gradle.kts` (project level)
```kotlin
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}
```

---

## 2. Firebase Setup

### Steps
1. Go to https://console.firebase.google.com → Create project "FitForge"
2. Add Android app with package `com.fitforge.app`
3. Download `google-services.json` → place in `app/` directory
4. Enable these Firebase services:
   - **Authentication** → Email/Password + Google Sign-In
   - **Cloud Firestore** → Start in test mode (set rules later)
   - **Firebase Storage** → for profile photos
   - **Cloud Messaging (FCM)** → for push notifications

### Firestore Security Rules (set after dev, before submission)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /workouts/{workoutId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

---

## 3. External APIs

### 3.1 ExerciseDB API (Exercise GIF Animations)
- **Source:** https://exercisedb.dev (open source, free, no key required for basic use)
- **Alternative (more reliable, 500 req/month free):** https://workoutxapp.com
- **Base URL:** `https://exercisedb.dev/api/v2`
- **Key endpoints used in FitForge:**

| Endpoint | Purpose |
|---|---|
| `GET /exercises?limit=20&offset=0` | Paginated exercise list |
| `GET /exercises/bodyPart/{bodyPart}` | Filter by muscle group |
| `GET /exercises/equipment/{equipment}` | Filter by equipment |
| `GET /exercises/{id}` | Single exercise detail with GIF |
| `GET /exercises/bodyPartList` | All available body parts |

- **Response shape per exercise:**
```json
{
  "id": "0001",
  "name": "Barbell Bench Press",
  "bodyPart": "chest",
  "target": "pectorals",
  "equipment": "barbell",
  "gifUrl": "https://v2.exercisedb.io/image/abc123.gif",
  "instructions": ["Lie on bench...", "Lower the bar..."],
  "secondaryMuscles": ["triceps", "shoulders"]
}
```

- **Android integration:** Retrofit interface + Glide to load `gifUrl` into ImageView
- **Caching strategy:** Use Glide's disk cache. After first load, GIFs are cached locally. Do NOT load GIFs on list scroll — load only on exercise detail open.

### 3.2 Gemini API (AI Coach)
- **Get API Key:** https://aistudio.google.com → "Get API Key" → free tier = 15 req/min
- **SDK:** `com.google.ai.client.generativeai:generativeai:0.9.0` (already in dependencies)
- **Model to use:** `gemini-1.5-flash` (fastest, cheapest, enough for coaching)
- **Store API key:** In `local.properties` (never commit to git)
  ```
  GEMINI_API_KEY=your_key_here
  ```
  Access in code via `BuildConfig.GEMINI_API_KEY` after adding to `build.gradle.kts`:
  ```kotlin
  buildConfigField("String", "GEMINI_API_KEY", "\"${properties["GEMINI_API_KEY"]}\"")
  ```

### 3.3 No other external APIs needed
All other data (streaks, workouts, badges, personality) lives in Firebase Firestore + local SharedPreferences. Do not add unnecessary APIs.

---

## 4. Design System — Light Theme

### Color Palette

```xml
<!-- res/values/colors.xml -->
<resources>
    <!-- ── Primary Brand ──────────────────────────── -->
    <color name="primary">#FF6B35</color>          <!-- Vibrant coral-orange — energy, action -->
    <color name="primary_dark">#E85A24</color>     <!-- Pressed state -->
    <color name="primary_light">#FFE8DF</color>    <!-- Tinted background, chips -->
    <color name="primary_container">#FFF0EB</color><!-- Card accent bg -->

    <!-- ── Accent / Secondary ─────────────────────── -->
    <color name="accent">#6C63FF</color>           <!-- Purple — AI, badges, highlights -->
    <color name="accent_light">#EEE9FF</color>

    <!-- ── Success / Positive ─────────────────────── -->
    <color name="success">#00C48C</color>          <!-- Teal-green — momentum, streaks -->
    <color name="success_light">#D6FFF4</color>

    <!-- ── Warning / Recovery ─────────────────────── -->
    <color name="warning">#FFB300</color>          <!-- Amber — life happened, rest days -->
    <color name="warning_light">#FFF8E1</color>

    <!-- ── Error ──────────────────────────────────── -->
    <color name="error">#FF5252</color>
    <color name="error_light">#FFEBEE</color>

    <!-- ── Backgrounds (Light Theme) ─────────────── -->
    <color name="bg_primary">#FFFFFF</color>       <!-- Pure white — main background -->
    <color name="bg_secondary">#F8F9FA</color>     <!-- Off-white — card bg, tabs -->
    <color name="bg_tertiary">#F1F3F5</color>      <!-- Light gray — input bg, dividers -->

    <!-- ── Text ───────────────────────────────────── -->
    <color name="text_primary">#1A1A2E</color>     <!-- Near black — headings -->
    <color name="text_secondary">#6B7280</color>   <!-- Gray — subtitles, hints -->
    <color name="text_disabled">#9CA3AF</color>    <!-- Placeholder text -->
    <color name="text_on_primary">#FFFFFF</color>  <!-- White text on orange bg -->

    <!-- ── Borders / Dividers ─────────────────────── -->
    <color name="divider">#E5E7EB</color>
    <color name="border">#D1D5DB</color>

    <!-- ── Personality Mode Colors ───────────────── -->
    <color name="mode_hype">#FF6B35</color>        <!-- Orange — Hype Man -->
    <color name="mode_drill">#1A1A2E</color>       <!-- Dark navy — Drill Sergeant -->
    <color name="mode_chill">#00C48C</color>       <!-- Teal — Chill Coach -->
    <color name="mode_chaos">#6C63FF</color>       <!-- Purple — Chaos Goblin -->

    <!-- ── Misc ───────────────────────────────────── -->
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
    <color name="transparent">#00000000</color>
    <color name="shadow">#1A000000</color>
</resources>
```

### Typography — `res/values/themes.xml`

```xml
<resources>
    <style name="Theme.FitForge" parent="Theme.MaterialComponents.Light.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_dark</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/accent</item>
        <item name="colorOnSecondary">@color/white</item>
        <item name="android:colorBackground">@color/bg_primary</item>
        <item name="android:textColorPrimary">@color/text_primary</item>
        <item name="android:textColorSecondary">@color/text_secondary</item>
        <item name="android:windowBackground">@color/bg_primary</item>
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:statusBarColor">@color/bg_primary</item>
        <item name="android:navigationBarColor">@color/bg_primary</item>
        <!-- Rounded corners on all Material components -->
        <item name="shapeAppearanceSmallComponent">@style/ShapeSmall</item>
        <item name="shapeAppearanceMediumComponent">@style/ShapeMedium</item>
        <item name="shapeAppearanceLargeComponent">@style/ShapeLarge</item>
    </style>

    <style name="ShapeSmall" parent="ShapeAppearance.MaterialComponents.SmallComponent">
        <item name="cornerSize">8dp</item>
    </style>
    <style name="ShapeMedium" parent="ShapeAppearance.MaterialComponents.MediumComponent">
        <item name="cornerSize">12dp</item>
    </style>
    <style name="ShapeLarge" parent="ShapeAppearance.MaterialComponents.LargeComponent">
        <item name="cornerSize">20dp</item>
    </style>

    <!-- Text Styles -->
    <style name="TextStyle.Display" parent="TextAppearance.MaterialComponents.Headline4">
        <item name="android:textSize">36sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:letterSpacing">-0.02</item>
    </style>
    <style name="TextStyle.Heading" parent="TextAppearance.MaterialComponents.Headline5">
        <item name="android:textSize">24sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    <style name="TextStyle.Title" parent="TextAppearance.MaterialComponents.Subtitle1">
        <item name="android:textSize">18sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    <style name="TextStyle.Body" parent="TextAppearance.MaterialComponents.Body1">
        <item name="android:textSize">14sp</item>
        <item name="android:textColor">@color/text_primary</item>
    </style>
    <style name="TextStyle.Caption" parent="TextAppearance.MaterialComponents.Caption">
        <item name="android:textSize">12sp</item>
        <item name="android:textColor">@color/text_secondary</item>
    </style>

    <!-- Button Styles -->
    <style name="Btn.Primary" parent="Widget.MaterialComponents.Button">
        <item name="backgroundTint">@color/primary</item>
        <item name="android:textColor">@color/white</item>
        <item name="android:textStyle">bold</item>
        <item name="cornerRadius">12dp</item>
        <item name="android:paddingTop">14dp</item>
        <item name="android:paddingBottom">14dp</item>
        <item name="android:elevation">4dp</item>
    </style>
    <style name="Btn.Secondary" parent="Widget.MaterialComponents.Button.OutlinedButton">
        <item name="strokeColor">@color/primary</item>
        <item name="android:textColor">@color/primary</item>
        <item name="cornerRadius">12dp</item>
        <item name="android:paddingTop">14dp</item>
        <item name="android:paddingBottom">14dp</item>
    </style>

    <!-- Card Style -->
    <style name="Card.Default" parent="Widget.MaterialComponents.CardView">
        <item name="cardElevation">0dp</item>
        <item name="strokeWidth">1dp</item>
        <item name="strokeColor">@color/divider</item>
        <item name="cardCornerRadius">16dp</item>
        <item name="cardBackgroundColor">@color/bg_primary</item>
    </style>

    <!-- Input Style -->
    <style name="Input.Default" parent="Widget.MaterialComponents.TextInputLayout.OutlinedBox">
        <item name="boxCornerRadiusTopStart">12dp</item>
        <item name="boxCornerRadiusTopEnd">12dp</item>
        <item name="boxCornerRadiusBottomStart">12dp</item>
        <item name="boxCornerRadiusBottomEnd">12dp</item>
        <item name="boxStrokeColor">@color/border</item>
        <item name="hintTextColor">@color/text_secondary</item>
    </style>

    <!-- Splash Screen -->
    <style name="Theme.FitForge.Splash" parent="Theme.FitForge">
        <item name="android:windowBackground">@color/bg_primary</item>
    </style>
</resources>
```

### Logo — New FitForge Logo
Create `res/drawable/ic_logo_fitforge.xml` — a vector drawable:
- Shape: a bold stylized "F" inside a rounded square, with a small lightning bolt integrated into the F stem
- Colors: primary orange `#FF6B35` fill, white F mark
- Size: 48×48dp base, scalable
- Also create `ic_logo_full.xml` — horizontal lockup: logo icon left + "FitForge" text right (bold, `#1A1A2E`)
- Animated version for splash: `ic_logo_animated.xml` using `<animated-vector>` — logo draws itself in 800ms via path animation

### Splash Background
- Use a subtle radial gradient drawable: white center fading to `#FFF0EB` (primary_container) at edges
- Place logo centered with the animated draw-in, then scale-bounce into final size

---

## 5. App Architecture

### Pattern: MVVM + Repository
```
UI Layer (Activities/Fragments/XML)
    ↕ observes LiveData
ViewModel Layer (one per screen)
    ↕ calls
Repository Layer (FirebaseRepository + LocalRepository)
    ↕ reads/writes
Firebase Firestore + SharedPreferences
```

### Key principle for instructor demo:
- `Activity` = entry point only. All logic is in `ViewModel`.
- `Fragment` = each tab screen. Hosted by `MainActivity`.
- `ViewModel` = holds LiveData, calls repository, has zero Android imports.
- `Repository` = only class that talks to Firebase or SharedPreferences.

---

## 6. Project File Structure

```
com.fitforge.app/
│
├── FitForgeApplication.kt              // Application class — init Firebase, Gemini
├── MainActivity.kt                     // Single activity — hosts BottomNavigation + NavHost
│
├── ui/
│   ├── splash/
│   │   └── SplashActivity.kt           // Animated logo → route to auth or home
│   │
│   ├── auth/
│   │   ├── LoginActivity.kt
│   │   ├── SignupActivity.kt
│   │   └── ForgotPasswordActivity.kt
│   │
│   ├── onboarding/
│   │   └── OnboardingActivity.kt       // 3-page ViewPager2 — shown once after signup
│   │
│   ├── home/
│   │   ├── HomeFragment.kt
│   │   └── HomeViewModel.kt
│   │
│   ├── workout/
│   │   ├── WorkoutFragment.kt          // Tab root — shows Library + active session
│   │   ├── WorkoutViewModel.kt
│   │   ├── LogWorkoutActivity.kt       // Full-screen workout logger
│   │   ├── ExerciseDetailActivity.kt   // Shows GIF + instructions + timer
│   │   └── WorkoutCompleteActivity.kt  // Celebration screen with Lottie
│   │
│   ├── progress/
│   │   ├── ProgressFragment.kt
│   │   └── ProgressViewModel.kt
│   │
│   ├── ai/
│   │   ├── AICoachFragment.kt
│   │   └── AICoachViewModel.kt
│   │
│   └── profile/
│       ├── ProfileFragment.kt
│       ├── ProfileViewModel.kt
│       ├── EditProfileActivity.kt
│       └── SettingsActivity.kt
│
├── adapters/
│   ├── ExerciseAdapter.kt
│   ├── WorkoutHistoryAdapter.kt
│   ├── BadgeAdapter.kt
│   ├── ChatAdapter.kt                  // AI chat messages
│   └── OnboardingAdapter.kt
│
├── data/
│   ├── models/
│   │   ├── User.kt
│   │   ├── Workout.kt
│   │   ├── WorkoutSet.kt
│   │   ├── Exercise.kt                 // From ExerciseDB API
│   │   ├── Badge.kt
│   │   ├── MomentumData.kt
│   │   ├── PersonalityMode.kt
│   │   └── RecoveryLog.kt
│   │
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── WorkoutRepository.kt
│   │   ├── UserRepository.kt
│   │   └── ExerciseRepository.kt      // Calls ExerciseDB API
│   │
│   ├── remote/
│   │   ├── ExerciseApiService.kt       // Retrofit interface
│   │   └── RetrofitClient.kt
│   │
│   └── local/
│       └── PrefsManager.kt            // SharedPreferences wrapper
│
├── utils/
│   ├── MomentumCalculator.kt
│   ├── BadgeChecker.kt
│   ├── PersonalityStrings.kt          // All personality-mode string tables
│   ├── WorkoutDNAAnalyzer.kt
│   ├── AudioManager.kt                // In-app sounds
│   ├── AnimationUtils.kt
│   └── DateUtils.kt
│
└── notifications/
    ├── FitNotificationManager.kt
    └── RoastReceiver.kt

res/
├── layout/
│   ├── activity_splash.xml
│   ├── activity_main.xml              // BottomNavigationView + NavHostFragment
│   ├── activity_login.xml
│   ├── activity_signup.xml
│   ├── activity_forgot_password.xml
│   ├── activity_onboarding.xml
│   ├── activity_log_workout.xml
│   ├── activity_exercise_detail.xml
│   ├── activity_workout_complete.xml
│   ├── activity_edit_profile.xml
│   ├── activity_settings.xml
│   ├── fragment_home.xml
│   ├── fragment_workout.xml
│   ├── fragment_progress.xml
│   ├── fragment_ai_coach.xml
│   ├── fragment_profile.xml
│   ├── item_exercise.xml
│   ├── item_workout_history.xml
│   ├── item_badge.xml
│   ├── item_chat_message_user.xml
│   ├── item_chat_message_ai.xml
│   ├── item_onboarding_page.xml
│   └── dialog_recovery_log.xml
│
├── drawable/
│   ├── ic_logo_fitforge.xml           // New vector logo
│   ├── ic_logo_animated.xml           // Animated vector for splash
│   ├── bg_gradient_splash.xml         // Radial gradient background
│   ├── bg_card_primary.xml            // Orange gradient card bg
│   ├── bg_momentum_track.xml          // Momentum bar background
│   ├── ic_home.xml / ic_workout.xml / ic_progress.xml / ic_ai.xml / ic_profile.xml
│   └── [other icons as needed]
│
├── anim/
│   ├── slide_in_right.xml
│   ├── slide_out_left.xml
│   ├── fade_in.xml
│   ├── fade_out.xml
│   ├── bounce_in.xml
│   └── scale_up.xml
│
├── raw/                               // Audio files
│   ├── workout_complete.mp3
│   ├── badge_unlock.mp3
│   ├── rest_timer_beep.mp3
│   ├── countdown_tick.mp3
│   └── momentum_up.mp3
│
├── font/                              // Custom fonts
│   ├── inter_bold.ttf
│   ├── inter_semibold.ttf
│   └── inter_regular.ttf
│
├── navigation/
│   └── nav_graph.xml                  // NavHostFragment destinations
│
├── menu/
│   └── bottom_nav_menu.xml
│
└── values/
    ├── colors.xml
    ├── themes.xml
    ├── strings.xml
    └── dimens.xml
```

---

## 7. Firebase Database Schema

### Firestore Collections

```
/users/{uid}/
    ├── displayName: String
    ├── email: String
    ├── photoUrl: String (Firebase Storage URL)
    ├── gender: String ("male" | "female" | "other")
    ├── weight: Float (kg)
    ├── height: Float (cm)
    ├── age: Int
    ├── fitnessGoal: String ("lose_weight" | "build_muscle" | "stay_active")
    ├── personalityMode: String ("hype" | "drill" | "chill" | "chaos")
    ├── createdAt: Timestamp
    ├── lastActiveAt: Timestamp
    │
    ├── momentum: Float (0.0 to 100.0)
    ├── currentStreak: Int
    ├── bestStreak: Int
    ├── totalWorkouts: Int
    ├── totalMinutes: Int
    ├── lastWorkoutDate: String (yyyy-MM-dd)
    ├── lastMuscleGroup: String
    ├── fcmToken: String
    │
    └── badges: Map<String, Boolean>
        (e.g. "baby_gains": true, "gym_rat": false)

/users/{uid}/workouts/{workoutId}/
    ├── id: String (UUID)
    ├── date: Timestamp
    ├── dateString: String (yyyy-MM-dd)
    ├── durationMinutes: Int
    ├── totalSets: Int
    ├── notes: String
    ├── isRecoveryDay: Boolean
    ├── recoveryReason: String? ("rest" | "sick" | "exams" | "travel" | "other")
    └── exercises: Array[
          {
            exerciseId: String,        (from ExerciseDB)
            exerciseName: String,
            muscleGroup: String,
            sets: [
              { reps: Int, weightKg: Float, completed: Boolean }
            ]
          }
        ]

/users/{uid}/aiChatHistory/{sessionId}/
    ├── sessionDate: Timestamp
    └── messages: Array[
          { role: "user"|"model", text: String, timestamp: Timestamp }
        ]
```

### SharedPreferences (local, offline-first for UX speed)
```
KEY: "uid"              → Firebase user UID
KEY: "personality_mode" → "hype" | "drill" | "chill" | "chaos"
KEY: "momentum"         → Float (synced from Firebase on launch)
KEY: "is_first_launch"  → Boolean
KEY: "onboarding_done"  → Boolean
KEY: "roast_enabled"    → Boolean
KEY: "reminder_enabled" → Boolean
KEY: "reminder_hour"    → Int (24h format)
```

---

## 8. Authentication — Login & Signup

### Flow
```
SplashActivity
    ↓ (check FirebaseAuth.currentUser)
    ├── null → LoginActivity
    └── exists → MainActivity
```

### LoginActivity (`activity_login.xml`)
**Layout — award-winning pattern:**
- Top 40% of screen: full-bleed image area with the animated logo centered on a soft orange-to-white gradient
- Bottom 60%: white card with rounded top corners (cornerRadius 28dp), slides up with animation
- Inside card: "Welcome back 👋" heading (28sp bold), subtitle in gray, email TextInputLayout, password TextInputLayout with show/hide toggle, "Forgot password?" text link, primary orange "SIGN IN" button (full width), divider with "or", Google Sign-In button (outlined), "Don't have an account? Sign Up" at bottom
- Email field: leading mail icon, outlined box style
- Password field: leading lock icon, trailing visibility toggle
- All corners: 12dp radius on inputs
- Error state: red border + shake animation on the input + red error text below

**LoginActivity.kt behavior:**
- Validate email format with regex before submitting
- On submit: show loading state (button text → spinner inside button via `CircularProgressIndicatorSpec`)
- On `FirebaseAuth.signInWithEmailAndPassword()` success → `startActivity(MainActivity)` with `finish()`
- On failure → show Snackbar with message from `exception.localizedMessage`
- Google Sign-In: use `GoogleSignInClient` → `FirebaseAuth.signInWithCredential()`

### SignupActivity (`activity_signup.xml`)
**Fields:** Full Name, Email, Password, Confirm Password
- Show password strength indicator (weak/medium/strong) as a 3-segment bar below password field using a custom `View`
- On success: create Firestore user document with defaults, then start `OnboardingActivity`

### ForgotPasswordActivity
- Single email field + "Send Reset Link" button
- `FirebaseAuth.sendPasswordResetEmail()` → show success message

---

## 9. Splash Screen & Onboarding

### SplashActivity (`activity_splash.xml`)
**Sequence (total: ~2.5 seconds):**
1. Screen starts: white bg, logo invisible
2. 0–500ms: logo fades in and scales from 0.6 → 1.0 (spring interpolator)
3. 500ms–1200ms: logo path draws itself (animated vector) — the "F" letterform traces itself in orange
4. 1200ms–1600ms: tagline "Built for humans." fades in below logo
5. 1600ms–2200ms: subtle particle effect — 6 small orange dots animate outward from logo
6. 2200ms: transition to Login or Home with a crossfade

**Implementation:**
- Use `AnimatedVectorDrawable` for the logo draw-in effect
- Use `ObjectAnimator` for scale and fade
- Particle dots: create 6 `View` objects programmatically, animate with `ObjectAnimator.ofFloat()` on `translationX`, `translationY`, `alpha`
- Status bar: transparent, light icons

### OnboardingActivity (`activity_onboarding.xml`)
**3 pages via ViewPager2 + DotsIndicator:**

Page 1: "The Momentum System"
- Full-screen Lottie animation: progress bar filling up (search LottieFiles for "progress bar" or "momentum" — use `lottiefiles.com/free-animations/fitness`)
- Heading: "No more dead streaks"
- Body: "Miss a day. Lose 15%. Not everything. Your momentum builds over time, not resets overnight."
- Color accent: success green

Page 2: "Your Coach, Your Way"
- Lottie: character celebrating / cheering
- Heading: "Pick your personality"
- Body: "Hype Man. Drill Sergeant. Chill Coach. Chaos Goblin. Your app talks how you want it to."
- Show 4 personality mode selector chips with colors
- Tapping one highlights it — save selection to SharedPreferences immediately

Page 3: "Life Happens"
- Lottie: calendar with a checkmark / person relaxing
- Heading: "Rest days aren't failures"
- Body: "Sick? Exams? Travel? Log it. We protect your streak. We respect your life."
- Color accent: warning amber

**Bottom of each page:** "Skip" text button (top right) + "Next"/"Get Started" button (bottom)
**On finish:** Set `onboarding_done = true` in SharedPreferences → `startActivity(MainActivity)` with `finish()`

---

## 10. Bottom Navigation Structure

### `activity_main.xml`
```xml
<!-- Root: CoordinatorLayout -->
<!-- NavHostFragment fills top portion -->
<!-- BottomNavigationView docked at bottom -->
<!-- elevation: 8dp shadow above nav bar -->
```

### 5 Tabs — `bottom_nav_menu.xml`
```xml
<menu>
    <item android:id="@+id/nav_home"     android:icon="@drawable/ic_home"     android:title="Home" />
    <item android:id="@+id/nav_workout"  android:icon="@drawable/ic_workout"  android:title="Workout" />
    <item android:id="@+id/nav_progress" android:icon="@drawable/ic_progress" android:title="Progress" />
    <item android:id="@+id/nav_ai"       android:icon="@drawable/ic_ai"       android:title="Coach" />
    <item android:id="@+id/nav_profile"  android:icon="@drawable/ic_profile"  android:title="Profile" />
</menu>
```

### Visual style of bottom nav:
- Background: white with top shadow
- Selected item: primary orange icon + label + filled orange indicator pill (Material3 style)
- Unselected: `text_secondary` gray
- Center item (Workout) is slightly larger — use a custom `View` or `BottomNavigationView` with custom icon size
- Tab switch animation: fragments use `setCustomAnimations(fade_in, fade_out)` in NavGraph

### `nav_graph.xml` destinations:
```xml
<fragment android:id="@+id/nav_home"     android:name=".ui.home.HomeFragment" />
<fragment android:id="@+id/nav_workout"  android:name=".ui.workout.WorkoutFragment" />
<fragment android:id="@+id/nav_progress" android:name=".ui.progress.ProgressFragment" />
<fragment android:id="@+id/nav_ai"       android:name=".ui.ai.AICoachFragment" />
<fragment android:id="@+id/nav_profile"  android:name=".ui.profile.ProfileFragment" />
```

---

## 11. Home Tab — Dashboard

### `fragment_home.xml` — Layout structure (top to bottom, NestedScrollView)

**A. Top Bar (not a toolbar — custom layout):**
```
[Profile thumbnail 40dp circle] [greeting text]    [notification bell icon]
"Good morning, Muhammad 👋"       ← dynamic greeting
"Sunday, 1 June"                  ← current date
```
- Profile thumbnail: load from Firebase Storage with Glide, `CircleTransform`
- Notification bell: badge dot if unread notifications exist
- Background: `bg_primary` white

**B. Momentum Card (most important element):**
```
╔══════════════════════════════════════╗
║  YOUR MOMENTUM                       ║
║                                      ║
║  [CircularProgressBar 120dp]         ║
║       78%                            ║
║    "Strong momentum"                 ║
║                                      ║
║  [horizontal Momentum Bar — full]    ║
║  ██████████████░░░░░░   78%          ║
║  "Keep going — 2 more sessions       ║
║   this week to hit 90%"              ║
╚══════════════════════════════════════╝
```
- Card: `Card.Default` style, orange left border (4dp), white bg
- `CircularProgressBar` lib: `com.mikhaellopez:circularprogressbar`
  - Progress color: orange → green gradient (custom `GradientDrawable`)
  - Animate from 0 → current momentum on fragment resume
- Horizontal bar below: custom `View` drawn with `Canvas` — track is `bg_tertiary`, fill is a linear gradient orange→green, rounded ends, height 12dp
- Below bar: personality-mode-aware message (see Section 17)
- On tap: expand card to show full momentum history explanation

**C. Quick Stats Row (3 cards):**
```
┌──────────┐ ┌──────────┐ ┌──────────┐
│ 🔥  12   │ │  47      │ │  3.2h    │
│  Streak  │ │ Workouts │ │ This week│
└──────────┘ └──────────┘ └──────────┘
```
- Each: small `MaterialCardView`, rounded 12dp, white bg, border divider
- Numbers: 28sp bold orange
- Labels: 12sp gray
- Animate numbers counting up on first load (custom `CountUpTextView`)

**D. Today's Plan / Prompt Card:**
- If no workout logged today: orange gradient card
  - "Ready to move?" heading
  - Personality-mode roast/hype line (italic, 14sp)
  - "Start Workout" button → navigates to WorkoutFragment
- If workout logged today: success green card
  - "✓ Workout logged today"
  - Show brief summary: exercise count, duration

**E. Recent Activity (last 3 workouts):**
- Heading: "Recent Workouts"
- `RecyclerView` (horizontal=false, 3 items max, disabled scrolling)
- Each `WorkoutHistoryAdapter` item: date + exercise list + muscle group chip
- "View All →" text button → Progress tab

**F. Daily Tip / AI Insight:**
- Small card at bottom
- Gemini-generated tip: fetched once daily, cached in SharedPreferences
- Loading state: Shimmer placeholder
- Title: "Coach says:" + personality icon

### HomeViewModel.kt
- `loadUserData()` → `UserRepository.getUserData(uid)` → LiveData<User>
- `loadMomentum()` → calculate from last workout dates (see Section 16)
- `loadRecentWorkouts()` → `WorkoutRepository.getRecent(3)`
- `loadDailyTip()` → check SharedPrefs for today's tip, else call Gemini

---

## 12. Workout Tab — Log & Library

### WorkoutFragment (`fragment_workout.xml`)
**Layout — two sections with TabLayout + ViewPager2:**
- Tab 1: "Library" — Browse exercises
- Tab 2: "History" — Past workouts

**Sticky FAB at bottom:** orange "+" → starts `LogWorkoutActivity`

### Exercise Library Tab
- `SearchView` at top (expand animation on focus)
- Horizontal scrolling `ChipGroup` for body part filter
  - Chips: All / Chest / Back / Legs / Shoulders / Arms / Core / Cardio
  - Selected chip: orange fill, white text
  - Unselected: white bg, gray border
- `RecyclerView` — `ExerciseAdapter`
- **Loading:** Shimmer placeholder cards while API fetches
- **Each `item_exercise.xml` card:**
  ```
  ┌─────────────────────────────────────┐
  │ [GIF preview 80×80dp, rounded 8dp]  Exercise Name (bold)    │
  │                                     Chest • Compound        │
  │                                     [barbell chip]          │
  └─────────────────────────────────────┘
  ```
- GIF preview: load with `Glide.with(context).asGif().load(gifUrl).into(imageView)`
- Tapping card → `ExerciseDetailActivity` with `exerciseId` as Intent extra

### ExerciseDetailActivity (`activity_exercise_detail.xml`)
**Layout:**
- Top: Large GIF (full width, 260dp height, rounded bottom corners 20dp)
- Name (24sp bold) + body part chip + equipment chip
- "START EXERCISE" orange button — starts a guided set-by-set logger
- "QUICK LOG" outlined button — jumps to simple log form
- Scrollable instructions list (numbered, 14sp body text)
- "Secondary muscles" section with horizontal chips
- At bottom: horizontal scrollable row "Similar exercises" (RecyclerView)

**Timer during guided mode:**
- Full-screen overlay during rest period
- Circular countdown timer (custom `View` using Canvas arc) — counts down 90 seconds
- Rest timer plays `rest_timer_beep.mp3` at 3, 2, 1 seconds
- Shows current set number prominently

### LogWorkoutActivity (`activity_log_workout.xml`)
- Top bar: back arrow + "Log Workout" + "SAVE" text button (top right)
- **Exercise Name** `TextInputLayout` + autocomplete from exercise library
- **Muscle Group** `MaterialSpinner` (ExposedDropdownMenu pattern)
- **Sets builder** — dynamic: starts with 1 set row, "Add Set +" button adds rows
  - Each set row: set number label + `EditText` for reps + `EditText` for weight + checkmark button
  - Completed set row: turns green, plays `countdown_tick.mp3`
- **Notes** multiline `TextInputEditText`
- **"Life Happened"** button (text, amber color) → `dialog_recovery_log.xml` bottom sheet
- **SAVE:** validates → saves to Firestore → runs BadgeChecker → if badges unlocked, starts `WorkoutCompleteActivity`

---

## 13. Progress Tab — Charts & DNA

### ProgressFragment (`fragment_progress.xml`)

**A. Header:** "Your Progress" + date range picker (last 7 / 30 / 90 days)

**B. Momentum History Line Chart:**
- `LineChart` from MPAndroidChart
- X-axis: last 14 days (date labels)
- Y-axis: momentum value 0–100
- Line: smooth cubic bezier, orange color `#FF6B35`, filled area below with 20% opacity orange
- Points: circular orange dots, 6dp
- Animate chart drawing on fragment enter (`animateX(1000)`)
- Chart config: no legend, no description, custom `XAxisFormatter` for date labels

**C. Workout Frequency Bar Chart:**
- `BarChart`: workouts per week for last 6 weeks
- Bar color: orange, rounded top corners
- Y-axis: 0 to 7

**D. Muscle Group Balance Ring Chart:**
- `PieChart` from MPAndroidChart — but styled as a donut (hole = 60%)
- Each segment: one muscle group with distinct color slice
- Center text: "Balance" — if any group > 50% of total, show warning
- Warning: amber card below: "You're overtraining [muscle group]. Try adding [suggestion]."

**E. Workout DNA Card (Identity Card):**
- Calculated from Firestore workout history
- Format: bold stats callout card
```
╔══════════════════════════════════════╗
║  YOUR WORKOUT DNA                    ║
║  ────────────────────────────────    ║
║  You're a Push-Day Loyalist          ║  ← derived from top muscle group
║                                      ║
║  📅 Avg 3.2 workouts/week            ║
║  🎯 Chest dominates (42%)            ║
║  ⚡ Peak day: Saturday               ║
║  🔥 Best streak: 14 days             ║
║                                      ║
║  [SHARE CARD] button (outlined)      ║
╚══════════════════════════════════════╝
```
- "SHARE CARD" → renders card as `Bitmap` using `View.drawToBitmap()` → `ShareCompat.IntentBuilder`
- Identity labels: derive from data
  - Top muscle group > 40% → "Push/Pull/Legs [X] Loyalist"
  - Avg workouts < 2/week → "Weekend Warrior"
  - Avg workouts > 5/week → "Daily Grinder"
  - Most consistent day → "Peak day: X"

**F. Badge Collection:**
- `RecyclerView` with `GridLayoutManager(context, 3)` — 3 columns
- Each `item_badge.xml`: circular bg, emoji `TextView` (32sp), name below (11sp)
- Locked: grayscale tint on bg, lock icon overlay
- Unlocked: full color + subtle glow border (orange shadow using `CardView` elevation)
- Tapping unlocked badge → `AlertDialog` with full name + when unlocked

---

## 14. AI Coach Tab — Gemini

### AICoachFragment (`fragment_ai_coach.xml`)
**Layout (from top to bottom):**
- Header card: personality mode icon + "Your AI Coach" + mode name chip
- Suggestion chips row (horizontal scroll): "Plan my week", "Why am I not progressing?", "Rest day advice", "Motivate me", "What should I eat?"
- `RecyclerView` for chat messages (fills middle) — reverse layout so newest at bottom
- Bottom input row: `EditText` + Send `ImageButton` (orange)

### Chat message items:
- **User (`item_chat_message_user.xml`):** right-aligned bubble, orange bg, white text, rounded corners (18dp, smaller bottom-right)
- **AI (`item_chat_message_ai.xml`):** left-aligned bubble, `bg_secondary` light gray, `text_primary`, rounded corners (18dp, smaller bottom-left), with small "FF" logo avatar to left

### AICoachViewModel.kt

**System prompt (send as first message in every new chat session):**
```
You are the FitForge AI Coach. The user's personality mode is [personalityMode].
Personality behavior:
- "hype": enthusiastic, lots of energy, use motivational language, occasional caps for emphasis
- "drill": direct, no fluff, military-style brevity, no excuses accepted
- "chill": calm, supportive, no pressure, understanding tone
- "chaos": funny, meme-aware, Gen Z language, roast them lightly when appropriate

User context:
- Name: [userName]
- Current momentum: [momentum]%
- Total workouts: [totalWorkouts]
- Recent muscle groups: [lastMuscleGroups]
- Fitness goal: [fitnessGoal]
- Current streak: [currentStreak] days

Keep responses concise (under 120 words). Be practical. No medical advice.
If asked about exercises, recommend from common gym movements.
Always end with a small actionable suggestion.
```

**Gemini SDK usage:**
```kotlin
val model = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GMINI_AEPI_KEY
)
val chat = model.startChat(history = previousMessages)
val response = chat.sendMessage(userMessage)
```

**Context injection:** Before each session, inject user's last 5 workout summaries from Firestore so the AI responds with real data, not generic advice.

**Save chat history:** Persist to Firestore under `/users/{uid}/aiChatHistory/` — last 10 sessions kept.

**Loading state:** Show a typing indicator bubble (3 animated dots Lottie) while awaiting Gemini response.

**Offline handling:** If no internet, show "Your coach is offline. Check your connection." in a warning card. Do not crash.

---

## 15. Profile Tab — User Management

### ProfileFragment (`fragment_profile.xml`)
**Layout:**
- Top: large profile header (bg `primary_container` orange tint)
  - Profile photo: 88dp circle, tap → photo picker via `ImagePicker` library
  - Display name (22sp bold)
  - "Edit Profile" outlined button
  - Member since date
- Stats summary row: total workouts / total hours / best streak (same 3-card pattern as Home)
- Personality Mode selector: 4 cards in a 2×2 grid
  - Each card: color accent, mode name, short descriptor, radio selection
  - Currently selected: highlighted border + filled bg
  - On change: update Firestore + SharedPreferences + immediately affect app tone
- Badge Wall: `RecyclerView` GridLayoutManager 3 columns
- Settings row: gear icon → `SettingsActivity`
- Sign out button: outlined red

### EditProfileActivity (`activity_edit_profile.xml`)
**Fields:**
- Profile photo (ImagePicker → upload to Firebase Storage → save URL to Firestore)
- Full Name
- Age (NumberPicker style)
- Weight (kg) — slider + manual input
- Height (cm) — slider + manual input
- Gender — 3-chip single select
- Fitness Goal — 3 cards: Lose Weight / Build Muscle / Stay Active

**On save:** batch Firestore update → Toast "Profile updated" → finish with result

### SettingsActivity (`activity_settings.xml`)
- **Notifications section:** roast notifications switch, daily reminder switch, reminder time picker (`TimePickerDialog`)
- **Account section:** Change password (re-auth flow), Delete account (confirmation dialog)
- **About section:** version number, "Our Story" card (1 paragraph about FitForge's mission — the UCL research story)

---

## 16. Momentum System Implementation

### Logic — `MomentumCalculator.kt`

```
MOMENTUM RULES:
- Starting value: 50.0
- Max value: 100.0
- Min value: 0.0 (only after 7+ consecutive missed days)

GAIN:
+12 points → workout logged
+5  points → momentum ≥ 85% (streak milestone bonus)
+3  points → recovery day logged ("Life Happened")

DECAY:
-0  points → same day (multiple logs don't decay)
-12 points → 1 missed day
-20 points → 2 consecutive missed days
-30 points → 3+ consecutive missed days
-50 points → 7+ missed days (floor is 0)

STREAK:
Streak increments only on workout days (not recovery days)
Streak does NOT reset on single missed day if momentum > 30
Streak resets to 0 only if momentum hits 0 OR 7+ consecutive missed days
```

**MomentumData model:**
```kotlin
data class MomentumData(
    val value: Float,           // 0.0 to 100.0
    val label: String,          // "Critical" / "Low" / "Building" / "Strong" / "Peak"
    val colorRes: Int,          // error / warning / primary / success
    val streak: Int,
    val lastUpdated: String     // yyyy-MM-dd
)

fun getMomentumLabel(value: Float) = when {
    value >= 85 -> "Peak Momentum"
    value >= 65 -> "Strong Momentum"
    value >= 45 -> "Building Up"
    value >= 25 -> "Low Momentum"
    else        -> "Critical — Come Back"
}
```

**Calculate on app open:**
- Load last momentum value from Firestore
- Count days since last workout
- Apply decay rules
- Save updated value back to Firestore

---

## 17. Personality Mode System

### `PersonalityStrings.kt`

Four modes. Each has string lists for every message context.

```kotlin
object PersonalityStrings {

    enum class Mode { HYPE, DRILL, CHILL, CHAOS }

    fun getMomentumHighMessage(mode: Mode) = when(mode) {
        Mode.HYPE  -> listOf("LET'S GOOO! You're absolutely BUILT DIFFERENT right now! 🔥", "PEAK FORM! Keep this energy and nobody can stop you!")
        Mode.DRILL -> listOf("Strong performance. Maintain discipline.", "Numbers look good. Do not get comfortable.")
        Mode.CHILL -> listOf("You're doing really well. Keep that good energy going.", "Things are clicking. Stay consistent, no pressure.")
        Mode.CHAOS -> listOf("Bro you're actually insane right now??", "The gym is starting to recognize you. It's scared.")
    }

    fun getMomentumLowMessage(mode: Mode) = when(mode) {
        Mode.HYPE  -> listOf("Hey! We're getting back up RIGHT NOW. One workout changes everything!", "You've been HERE before and bounced back. TODAY is the day!")
        Mode.DRILL -> listOf("Momentum is down. You know what to do. Do it.", "No excuses. Get in there.")
        Mode.CHILL -> listOf("Hey, no worries. Just one session and you're back on track. Easy.", "Take it one day at a time. You've got this.")
        Mode.CHAOS -> listOf("Bro your couch is starting to recognize your body shape 💀", "The gym filed a missing persons report. Turn yourself in.")
    }

    fun getPostWorkoutMessage(mode: Mode) = when(mode) {
        Mode.HYPE  -> listOf("YESSS!! WORKOUT LOGGED! YOU ARE UNSTOPPABLE!", "THAT JUST HAPPENED! You are BUILT DIFFERENT!")
        Mode.DRILL -> listOf("Logged. Good work. Rest. Repeat.", "Done. That is what discipline looks like.")
        Mode.CHILL -> listOf("Nicely done. Your body thanks you.", "That was solid. Rest up and you'll feel great tomorrow.")
        Mode.CHAOS -> listOf("Bro you actually showed up?? I'm shook.", "Different breed. Logged. Your couch is disappointed.")
    }

    fun getMissedDayMessage(mode: Mode) = when(mode) {
        Mode.HYPE  -> listOf("HEY! TODAY IS YOUR DAY! LET'S BOUNCE BACK RIGHT NOW!", "You're ONE session away from momentum! DO THIS!")
        Mode.DRILL -> listOf("You missed. Don't miss again.", "That is unacceptable. Fix it today.")
        Mode.CHILL -> listOf("Hey, rest days happen. Come back when you're ready.", "No big deal. Just pick it back up today, nice and easy.")
        Mode.CHAOS -> listOf("Day 2 of ghosting the gym. It's moving on.", "Your protein shake expired waiting for you.")
    }

    fun getRecoveryLogMessage(mode: Mode) = when(mode) {
        Mode.HYPE  -> listOf("Smart! Recovery is PART of the gains!", "Resting to come back STRONGER! That's elite athlete thinking!")
        Mode.DRILL -> listOf("Recovery logged. Return at full capacity.", "Acceptable. Come back ready.")
        Mode.CHILL -> listOf("Good call logging your rest day. Listening to your body is important.", "Rest well. You've earned it.")
        Mode.CHAOS -> listOf("Rest day logged. The gym respects it. Kinda.", "Strategic absence. We see you.")
    }
}
```

**Apply personality throughout:** Every notification body, every home screen banner, every post-workout message, every AI Coach system prompt — all read from `PersonalityStrings` using the user's stored mode.

---

## 18. Recovery Log — Life Happened

### Triggered by: "Life Happened" button in `LogWorkoutActivity`

### `dialog_recovery_log.xml` — Bottom Sheet Dialog
**Layout:**
- Handle bar at top (8dp rounded pill)
- Title: "Life Happened 💛" (18sp bold)
- Subtitle: "Log a protected day — your momentum stays safe"
- Grid of 5 reason chips (2-column):
  - 😴 Rest Day
  - 🤒 Not Feeling Well
  - 📚 Exams / Study
  - ✈️ Travelling
  - 📝 Other
- "PROTECT MY DAY" orange button — full width

### Behavior on confirm:
1. Create a `Workout` object with `isRecoveryDay = true`, `recoveryReason = selected`
2. Save to Firestore (still a document in `/workouts/` collection)
3. Apply momentum gain: `+3 points` (see Section 16)
4. Show small Lottie "shield" animation — protection confirmed
5. Personality-mode message in a Toast (see Section 17 `getRecoveryLogMessage`)
6. Does NOT increment workout streak counter, but DOES protect it from reset

### Visual indicator on home:
- If today is a recovery day: replace "Start Workout" card with amber "Rest Day Logged" card
- Show a `🛡️` shield icon next to date

---

## 19. Lottie Animations Guide

### Library: `com.airbnb.android:lottie:6.5.2`

### How to use in XML:
```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/lottieView"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:lottie_fileName="workout_complete.json"
    app:lottie_autoPlay="true"
    app:lottie_loop="false" />
```

### In Kotlin:
```kotlin
lottieView.setAnimation("celebration.json")
lottieView.playAnimation()
lottieView.addAnimatorListener(object : Animator.AnimatorListener {
    override fun onAnimationEnd(animation: Animator) { /* next action */ }
    ...
})
```

### Required Lottie animations — download from LottieFiles.com (free):

| File name | Search query on LottieFiles | Used where |
|---|---|---|
| `celebration.json` | "confetti celebration workout" | WorkoutCompleteActivity — main celebration |
| `badge_unlock.json` | "trophy achievement unlock" | Badge unlock dialog |
| `momentum_up.json` | "progress bar filling" | Momentum gain animation |
| `rest_shield.json` | "shield protection checkmark" | Recovery day confirmed |
| `ai_thinking.json` | "typing dots loading" | AI Coach response loading |
| `splash_logo.json` | "fitness dumbbell" | Splash screen (optional, as fallback for AnimatedVector) |
| `empty_state.json` | "empty box search nothing" | Empty history state |
| `streak_fire.json` | "fire flame burn" | Streak milestone celebrations |

### Download instructions:
1. Go to https://lottiefiles.com/free-animations/[query]
2. Filter by Free
3. Download JSON format
4. Place in `app/src/main/assets/` folder (create assets folder if not exists)

### Lottie color override (to match FitForge orange theme):
```kotlin
val orangeFilter = SimpleColorFilter(Color.parseColor("#FF6B35"))
val keyPath = KeyPath("**")
lottieView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER) { orangeFilter }
```

---

## 20. Exercise GIF Animations

### Source: ExerciseDB API — `https://exercisedb.dev/api/v2`
Free, no API key for basic endpoints.

### Retrofit setup:
```kotlin
// data/remote/ExerciseApiService.kt
interface ExerciseApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<Exercise>

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getByBodyPart(
        @Path("bodyPart") bodyPart: String,
        @Query("limit") limit: Int = 20
    ): List<Exercise>

    @GET("exercises/{id}")
    suspend fun getExerciseById(@Path("id") id: String): Exercise

    @GET("exercises/bodyPartList")
    suspend fun getBodyPartList(): List<String>
}

// data/remote/RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "https://exercisedb.dev/api/v2/"
    val exerciseApi: ExerciseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BASIC })
                .cache(Cache(File(context.cacheDir, "exercise_cache"), 50L * 1024 * 1024)) // 50MB cache
                .build())
            .build()
            .create(ExerciseApiService::class.java)
    }
}
```

### GIF loading with Glide (critical — GIFs are large, must be optimized):
```kotlin
// In ExerciseAdapter or ExerciseDetailActivity:
Glide.with(context)
    .asGif()
    .load(exercise.gifUrl)
    .diskCacheStrategy(DiskCacheStrategy.DATA)  // Cache decoded frames
    .placeholder(R.drawable.ic_exercise_placeholder)
    .error(R.drawable.ic_exercise_error)
    .override(300, 300)   // Limit size
    .into(ivExerciseGif)
```

### In ExerciseDetailActivity — full-size GIF:
```kotlin
Glide.with(this)
    .asGif()
    .load(exercise.gifUrl)
    .diskCacheStrategy(DiskCacheStrategy.DATA)
    .placeholder(shimmerDrawable)
    .transition(DrawableTransitionOptions.withCrossFade(300))
    .into(ivDetailGif)
```

### Preloading for smooth UX:
```kotlin
// Preload next 3 exercises in background when user is viewing list:
Glide.with(context).asGif().load(nextExercise.gifUrl).preload()
```

---

## 21. Audio System

### `utils/AudioManager.kt` — FitForgeAudioManager

Use `MediaPlayer` for longer sounds and `SoundPool` for short sounds.

```kotlin
object FitForgeAudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var soundPool: SoundPool
    private var soundIds = mutableMapOf<String, Int>()

    fun init(context: Context) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()

        // Load short sounds into SoundPool
        soundIds["tick"]    = soundPool.load(context, R.raw.countdown_tick, 1)
        soundIds["beep"]    = soundPool.load(context, R.raw.rest_timer_beep, 1)
        soundIds["badge"]   = soundPool.load(context, R.raw.badge_unlock, 1)
        soundIds["up"]      = soundPool.load(context, R.raw.momentum_up, 1)
    }

    fun playShort(key: String) {
        soundIds[key]?.let { soundPool.play(it, 1f, 1f, 0, 0, 1f) }
    }

    fun playWorkoutComplete(context: Context) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.workout_complete)
        mediaPlayer?.start()
    }

    fun release() {
        mediaPlayer?.release()
        soundPool.release()
    }
}
```

### Audio files needed — place in `res/raw/`:
| File | Source | Description |
|---|---|---|
| `workout_complete.mp3` | Mixkit.co (free) — search "success fanfare" or "achievement" | 2–3 sec triumphant sound |
| `badge_unlock.mp3` | Mixkit.co — search "game level up" | Short chime |
| `rest_timer_beep.mp3` | Zapsplat.com (free) — search "timer beep" | 0.5s beep |
| `countdown_tick.mp3` | Zapsplat.com — search "click tick" | 0.1s tick |
| `momentum_up.mp3` | Mixkit.co — search "positive notification" | Short positive tone |

**Free sound sources:**
- https://mixkit.co/free-sound-effects/
- https://zapsplat.com (free with account)
- https://freesound.org

### Where to play sounds:
- Set completed → `playShort("tick")`
- Rest timer last 3 seconds → `playShort("beep")`
- Badge unlock → `playShort("badge")`
- Workout complete → `playWorkoutComplete()`
- Momentum goes up → `playShort("up")`

### Respect user preference:
- Check `AudioManager.getRingerMode()` — if silent/vibrate, skip sounds
- Add mute toggle in Settings Activity

---

## 22. Notifications

### Channel setup in `FitForgeApplication.kt`:
```kotlin
class FitForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FitForgeAudioManager.init(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel("ROAST", "Motivation Notifications", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Personality-mode messages when you skip" },
            NotificationChannel("REMINDER", "Daily Reminder", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Gentle daily nudge" },
            NotificationChannel("BADGE", "Achievement Alerts", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Badge unlock alerts" },
            NotificationChannel("STREAK", "Streak Alerts", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Streak milestone alerts" },
        )
        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
```

### Notification triggers:
| Trigger | Channel | Message source |
|---|---|---|
| Missed day (BroadcastReceiver daily at 8pm) | ROAST | `PersonalityStrings.getMissedDayMessage(mode)` |
| Streak milestone (3, 7, 14, 30 days) | STREAK | Hardcoded milestone strings |
| Badge unlock | BADGE | Badge name + description |
| Daily reminder (if enabled, 7pm) | REMINDER | "Ready to train?" + personality-mode variant |

### Rich notification style (badge unlock):
```kotlin
val notification = NotificationCompat.Builder(context, "BADGE")
    .setSmallIcon(R.drawable.ic_logo_fitforge)
    .setContentTitle("🏆 Badge Unlocked!")
    .setContentText("${badge.name} — ${badge.description}")
    .setStyle(NotificationCompat.BigTextStyle().bigText("${badge.emoji} You unlocked '${badge.name}'. ${badge.description}"))
    .setColor(ContextCompat.getColor(context, R.color.primary))
    .setAutoCancel(true)
    .build()
```

---

## 23. Celebration Screen — Post Workout

### `WorkoutCompleteActivity` (`activity_workout_complete.xml`)

**This screen is the emotional payoff. Make it extraordinary.**

**Layout — full screen, white bg:**

1. **Background:** confetti Lottie animation fills entire screen (looped 3 times then stops)

2. **Center content (z-order above Lottie):**
   - Large Lottie character animation (search LottieFiles: "workout celebration character" or "fitness victory") — a human character figure doing a victory pose/jump — centered at 280×280dp
   - "WORKOUT COMPLETE!" in 32sp ExtraBold orange
   - Date + duration below (14sp gray)

3. **Stats row:** Exercises / Sets / Total Weight Moved — 3 mini stat cards

4. **Personality message:** Large italic quote from `PersonalityStrings.getPostWorkoutMessage(mode)` — in orange-tinted card

5. **Badges earned (if any):** horizontal scroll row of badge cards with glow effect + `badge_unlock.json` Lottie playing on each

6. **Momentum update:** mini momentum bar showing the +12 gain with arrow animation

7. **"SHARE MY WORKOUT"** outlined button → ShareCompat with formatted summary text

8. **"BACK TO HOME"** filled orange button → `Intent(this, MainActivity::class.java)` with `CLEAR_TASK | NEW_TASK` flags

**Entry animation sequence (play in order using `Handler.postDelayed`):**
- 0ms: screen appears, confetti starts
- 0ms: character Lottie starts playing
- 500ms: "WORKOUT COMPLETE!" text fades + scales in from 0.5
- 800ms: stats row slides up from bottom
- 1100ms: personality message fades in
- 1400ms: badge row slides in (if any)
- 1700ms: buttons fade in
- Sound: `playWorkoutComplete()` at 0ms

---

## 24. Build Order

Execute strictly in sequence. App must build without errors before each next step.

**Phase 1 — Foundation (Day 1)**
1. Create new project with package `com.fitforge.app`
2. Add all dependencies to `build.gradle.kts`
3. Set up Firebase project + add `google-services.json`
4. Create `colors.xml`, `themes.xml`, `strings.xml`, `dimens.xml`
5. Create font folder, add Inter font files
6. Create `FitForgeApplication.kt` — register in Manifest
7. Build. Must compile cleanly.

**Phase 2 — Auth (Day 1–2)**
8. Create data models: `User.kt`, `Workout.kt`, `WorkoutSet.kt`, `Exercise.kt`, `Badge.kt`
9. Create `AuthRepository.kt` + `PrefsManager.kt`
10. Build `SplashActivity` with animated vector logo
11. Build `LoginActivity` — Firebase Email auth working
12. Build `SignupActivity` — creates Firestore user document
13. Build `OnboardingActivity` — 3 pages, personality selection saved
14. Test: full auth flow works end to end

**Phase 3 — Shell (Day 2)**
15. Create `MainActivity` with BottomNavigationView + NavHostFragment
16. Create empty Fragment classes for all 5 tabs (just show a TextView "Tab Name")
17. Navigation between tabs works. Back stack correct.

**Phase 4 — Data Layer (Day 2–3)**
18. Create `WorkoutRepository.kt` — save/get workouts from Firestore
19. Create `UserRepository.kt` — get/update user document
20. Create `ExerciseApiService.kt` + `RetrofitClient.kt`
21. Create `ExerciseRepository.kt` — fetches from ExerciseDB API
22. Create `MomentumCalculator.kt`
23. Create `PersonalityStrings.kt`
24. Create `BadgeChecker.kt`

**Phase 5 — Core Screens (Day 3–4)**
25. `HomeFragment` — full layout, momentum card, stats, loads from ViewModel
26. `WorkoutFragment` — TabLayout with Library and History
27. `ExerciseAdapter` — list with GIF loading via Glide
28. `ExerciseDetailActivity` — GIF full size + instructions
29. `LogWorkoutActivity` — full form with dynamic sets, Spinner, save to Firestore
30. `WorkoutCompleteActivity` — Lottie celebration + all animations

**Phase 6 — Progress + Profile (Day 4)**
31. `ProgressFragment` — MPAndroidChart momentum line chart + bar chart + DNA card
32. `ProfileFragment` — user stats, personality mode selector, badge grid
33. `EditProfileActivity` — photo picker + Firestore update

**Phase 7 — AI Coach (Day 5)**
34. Set up Gemini API key in `local.properties`
35. `AICoachFragment` — chat UI with RecyclerView
36. `AICoachViewModel` — Gemini SDK integration with system prompt injection
37. Test: send message, receive response, history persists

**Phase 8 — Audio, Notifications, Polish (Day 5–6)**
38. Add sound files to `res/raw/`
39. `FitForgeAudioManager.kt` — init in Application class
40. Wire all sound triggers
41. `FitNotificationManager.kt` + `RoastReceiver.kt`
42. Register receivers in Manifest
43. `SettingsActivity` — notification toggles

**Phase 9 — Recovery Log (Day 6)**
44. Add "Life Happened" button to LogWorkoutActivity
45. `dialog_recovery_log.xml` + `RecoveryLog` model
46. Wire momentum protection logic

**Phase 10 — Final Polish (Day 6–7)**
47. Add all Lottie animations to `assets/`
48. Wire Lottie to all trigger points
49. Add entry animations to all fragments (fade + slide)
50. Shimmer loading states on all network operations
51. Test offline behavior — all critical paths must not crash without internet
52. Final end-to-end test: signup → onboard → log workout → see celebration → check progress → chat with AI → recover day → see momentum → check badges

---

## 25. Agent Rules

### ✅ ALWAYS
- Use `AppCompatActivity` + XML layouts. No Compose.
- Use `ViewModel` + `LiveData`. Activities/Fragments only observe — never do business logic.
- Use `lifecycleScope.launch` or `viewModelScope.launch` for all coroutines — never `GlobalScope`.
- Handle loading states with shimmer and handle error states with a visible error message — never silently fail.
- Apply `PersonalityStrings` to every user-facing dynamic message.
- Load GIFs with Glide's `asGif()` + `diskCacheStrategy(DiskCacheStrategy.DATA)`.
- Store Gemini API key only in `local.properties` — never hardcode in source.

### 🚫 NEVER
- Never block the main thread — all Firestore, Retrofit, and Gemini calls must be in coroutines.
- Never crash on empty list — always show `EmptyState` view.
- Never show raw Firebase exception messages to users — catch and show friendly messages.
- Never auto-play GIF animations in list scrolling — load only in detail view.
- Never use `AsyncTask` — it is deprecated.
- Never hardcode UID or user data — always read from `FirebaseAuth.currentUser.uid`.
- Never write to Firestore without the authenticated UID as the document path prefix.

### ⚠️ PERFORMANCE RULES
- RecyclerView: always use `DiffUtil` in adapters, not `notifyDataSetChanged()`
- Firestore: use `get()` for one-time reads, `addSnapshotListener()` only for real-time data (momentum, profile)
- Lottie: `cancelAnimation()` in `onDestroyView()` to prevent memory leaks
- MediaPlayer: always `release()` in `onDestroy()`
- Images: never load full-resolution photos into small ImageViews — use Glide `override()` to size appropriately

### DEMO SCRIPT (for evaluator)
When demoing to instructor:
1. Open app → show splash animation
2. Sign in with test account
3. Home → point out Momentum Bar and explain the story
4. Tap "Start Workout" → log a bench press with 3 sets
5. Workout Complete → show Lottie celebration + sound
6. Progress tab → show MPAndroidChart momentum line + DNA card
7. AI Coach tab → type "What should I train tomorrow?" → show Gemini response adapting to personality mode
8. Profile → change Personality Mode from Hype to Chaos → return to Home → show message changed
9. Log Workout → tap "Life Happened" → show recovery log dialog + shield animation
10. Show notifications (trigger manually from Settings if needed)

---

*End of FitForge — Complete Agent Build Guide*
*Built for humans, not habit streaks.*
