# FitForge

FitForge is a modern Android fitness companion built around momentum, consistency, and a little personality. It helps users log workouts, protect their streaks, track progress, follow challenges, review exercise animations, monitor food intake, and chat with an AI coach that understands their fitness profile.

## Highlights

- **Momentum-based home dashboard** with streaks, workout stats, rotating motivation, and a calendar of logged sessions.
- **Exercise library** with categorized exercises, animated GIF previews, and detailed workout logging.
- **Challenge mode** with day-by-day plans, exercise previews, and completion tracking.
- **Workout logging** with sets, reps, weight, rest timer controls, and workout history.
- **Progress tracking** with momentum charts, muscle balance, badges, and workout DNA insights.
- **AI Coach** powered by Gemini, using profile, workout, and food history for personalized coaching.
- **Food intake logging** for calorie tracking against user goals.
- **Profile and badges** with editable profile image, user stats, streaks, momentum, and unlocked achievements.
- **Firebase authentication** with signup, login, forgot password, and account settings support.

## Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **UI:** XML layouts, Material Components, ConstraintLayout, RecyclerView
- **Architecture:** Fragment and Activity based screens with ViewModels and repositories
- **Backend services:** Firebase Auth, Firestore, Storage, Messaging, Analytics
- **AI:** Google Gemini Android SDK
- **Charts:** MPAndroidChart
- **Calendar:** Kizitonwose Calendar
- **Images and GIFs:** Glide
- **Profile image picking:** ImagePicker
- **Async:** Kotlin Coroutines

## Project Structure

```text
FittestForge/
├── app/
│   ├── src/main/java/com/fitforge/app/
│   │   ├── adapters/          # RecyclerView adapters
│   │   ├── data/              # Models, local data, repositories, remote APIs
│   │   ├── notifications/     # Notification receivers
│   │   ├── ui/                # App screens grouped by feature
│   │   ├── utils/             # Momentum, badges, audio, helpers
│   │   └── workers/           # Background work
│   └── src/main/res/          # Layouts, drawables, colors, themes, assets
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## Getting Started

### Prerequisites

- Android Studio
- JDK 11 or newer
- Firebase project configured for Android
- Gemini API key if you want AI Coach enabled

### Setup

1. Clone or open the project in Android Studio.
2. Add your Firebase Android config file:

   ```text
   app/google-services.json
   ```

3. Add your Gemini API key to `local.properties`:

   ```properties
   GEMINI_API_KEY=your_api_key_here
   ```

4. Sync Gradle.
5. Build and run the app:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

## Firebase Notes

FitForge uses Firebase for:

- Authentication
- Password reset emails
- User profiles
- Workout history
- Food logs
- AI chat session history
- Profile images
- Notifications and analytics

Make sure Email/Password sign-in is enabled in Firebase Authentication. Password reset emails should be configured from the Firebase console, not through SMTP credentials inside the Android app.

## Security Notes

Do not commit secrets into this repository.

Keep these out of source control:

- SMTP usernames and passwords
- Gemini API keys
- Private Firebase service account files
- Any production-only backend credentials

Android apps can be decompiled, so mail-sending secrets should live on a backend or cloud function, never inside the APK.

## Key Screens

- **Home:** Momentum, streaks, daily plan, recent workouts, workout calendar, and coaching tip.
- **Workout:** Exercise categories, exercise detail pages, set logging, and history.
- **Challenges:** Multi-day fitness challenges with exercise previews and day completion.
- **Progress:** Momentum chart, badges, muscle balance, and workout DNA.
- **AI Coach:** Personalized conversational coaching based on user activity.
- **Food:** Food intake logging and calorie feedback.
- **Profile:** User stats, profile image, badges, progress access, settings, and sign out.

## Development Notes

- Exercise GIFs are loaded with Glide and cached for smoother previews.
- Momentum is recalculated on app open and after workout/recovery logging.
- Badge unlock logic lives in the app utilities and updates user profile state.
- `local.properties` is used for the Gemini API key through `BuildConfig.GEMINI_API_KEY`.

## Roadmap Ideas

- Backend service for custom branded email templates.
- Offline-first workout drafts.
- More challenge templates and adaptive plans.
- Exercise search and filters.
- Richer nutrition history and macro tracking.
- In-app preview for newly unlocked badges.

## License

This project is currently private/internal. Add a license before distributing publicly.
