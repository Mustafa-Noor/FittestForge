# FittestForge — Backend Fix & Continuation Guide
## Agent Continuation Document — Based on Repo Inspection

> **Repo:** https://github.com/Mustafa-Noor/FittestForge
> Package: `com.fitforge.app`
> This document is the result of a full structural review of the existing codebase.
> Do not start fresh. Fix what is broken. Complete what is missing.

---

## WHAT WAS FOUND IN THE REPO — HONEST ASSESSMENT

### ✅ What exists and is correctly structured
- File structure matches the planned architecture — all fragments, activities, adapters, and repository classes are present
- `build.gradle.kts` is correct — all dependencies properly declared
- `PrefsManager.kt` is clean and sufficient
- `AuthRepository.kt` is solid — `signIn`, `signUp`, `resetPassword` all return `Result<T>`
- `PersonalityStrings.kt` exists
- All XML layouts are present
- Audio files in `res/raw/` are present
- `AICoachViewModel.kt` — Gemini is wired, system prompt is injected, structure is correct

### ❌ What is broken or missing — the fragile backend

**Problem 1 — `BadgeChecker.kt` is a stub**
It only checks for `first_workout` and `momentum_50`. It does not check against which badges are already unlocked (no "already awarded" guard), so it will re-award badges on every call. It does not receive the existing unlocked badges map from the user's Firestore document. It is effectively non-functional.

**Problem 2 — `MomentumCalculator.kt` is never called on save**
`LogWorkoutActivity` calls `startActivity(WorkoutCompleteActivity)` directly — it never calls `WorkoutRepository.saveWorkout()`, never updates user stats, never calls `MomentumCalculator`, never calls `BadgeChecker`. The workout is never actually saved to Firestore.

**Problem 3 — `LogWorkoutActivity` adds only dummy exercises**
`btnAddExercise` calls `addDummyExercise()` which creates `WorkoutExercise(exerciseName = "Exercise 1")`. There is no actual exercise selection flow connecting to the exercise library or the ExerciseDB API. When the workout is "finished", nothing is saved.

**Problem 4 — `UserRepository.updateUserProfile()` overwrites the entire document**
It calls `.set(user)` which replaces all fields. After saving a workout, the correct pattern is `.update(mapOf(...))` with only the changed fields — otherwise a partial User object (missing fields) will silently wipe data.

**Problem 5 — Momentum is never updated on app open**
`HomeViewModel` reads `user.momentum` from Firestore but never recalculates decay since last visit. If the user was away for 3 days, they still see their old high momentum value.

**Problem 6 — `WorkoutRepository` has no delete, no update, no stats query**
Progress tab and Profile need stats by date range, stats by muscle group, and counts. The current repository only has `saveWorkout`, `getWorkouts`, and `getRecentWorkouts`. Progress charts will show nothing.

**Problem 7 — `AICoachViewModel` sends the system prompt as part of every user message**
The system prompt is concatenated with `userText` and sent as a single message. This means Gemini sees it as user content, not a system instruction, and the context grows endlessly. The correct pattern is to send the system prompt once at session start, then send only user messages after.

**Problem 8 — `User` model stores `badges: Map<String, Boolean>` but `BadgeChecker` returns `List<Badge>` objects**
These two are incompatible. The checker creates `Badge` objects with no `unlockedAt` timestamp, no persistence bridge between the Firestore document and the check result.

**Problem 9 — No streak update logic anywhere**
`currentStreak` and `bestStreak` in the user document are never incremented or reset by any code path. They stay at 0 forever.

**Problem 10 — `WorkoutHistoryAdapter` uses `submitList()` but the class is a `ListAdapter`**
Confirmed from `adapters/WorkoutHistoryAdapter.kt` — the adapter pattern is correct. But `ProgressViewModel` and `ProfileViewModel` need to call the repository and submit data. Both ViewModels currently appear empty based on the file size — they load nothing.

---

## FIRESTORE SCHEMA — AUTHORITATIVE DEFINITION

This is the exact schema the app must write and read. Every repository must match this exactly.

```
/users/{uid}                                    ← one document per authenticated user

  IDENTITY FIELDS (set on signup, editable):
  displayName:       String
  email:             String
  photoUrl:          String                     ← Firebase Storage URL or ""
  gender:            String                     ← "male" | "female" | "other" | ""
  weight:            Number (float)             ← kg
  height:            Number (float)             ← cm
  age:               Number (int)
  fitnessGoal:       String                     ← "lose_weight" | "build_muscle" | "stay_active"
  personalityMode:   String                     ← "hype" | "drill" | "chill" | "chaos"
  fcmToken:          String                     ← updated each login
  createdAt:         Timestamp
  lastActiveAt:      Timestamp                  ← updated on every app open

  PROGRESS FIELDS (updated by app logic, NEVER by user directly):
  momentum:          Number (float)             ← 0.0 to 100.0
  momentumUpdatedAt: String                     ← "yyyy-MM-dd" — date of last momentum calculation
  currentStreak:     Number (int)               ← consecutive workout days (not recovery days)
  bestStreak:        Number (int)               ← all-time high streak
  totalWorkouts:     Number (int)               ← count of non-recovery workout docs
  totalMinutes:      Number (int)               ← sum of durationMinutes across all workout docs
  lastWorkoutDate:   String                     ← "yyyy-MM-dd" of last actual workout (not recovery)
  lastMuscleGroup:   String                     ← muscleGroup of last exercise in last workout

  BADGES (all start false, set to true when unlocked, NEVER set back to false):
  badges: {
    first_workout:   Boolean
    workouts_5:      Boolean
    workouts_10:     Boolean
    workouts_30:     Boolean
    streak_3:        Boolean
    streak_7:        Boolean
    streak_14:       Boolean
    streak_30:       Boolean
    momentum_peak:   Boolean                    ← momentum >= 85 for first time
    leg_day_respect: Boolean                    ← logged "legs" muscle group twice in a row
    early_bird:      Boolean                    ← workout logged before 8am
    recovery_smart:  Boolean                    ← first recovery day logged
  }

  badgeUnlockedAt: {                            ← parallel map — timestamps when each was unlocked
    first_workout:  Timestamp
    workouts_5:     Timestamp
    ...
  }


/users/{uid}/workouts/{workoutId}               ← subcollection, one document per session

  id:               String                      ← same as document ID, set on creation
  date:             Timestamp                   ← exact time workout was saved
  dateString:       String                      ← "yyyy-MM-dd" for easy date comparison
  durationMinutes:  Number (int)                ← elapsed time from start to finish
  totalSets:        Number (int)                ← sum of all completed sets
  notes:            String                      ← user text, may be ""
  isRecoveryDay:    Boolean                     ← true = "Life Happened" log
  recoveryReason:   String                      ← "rest"|"sick"|"exams"|"travel"|"other"|null
  exercises: [                                  ← Array of maps (not a subcollection)
    {
      exerciseId:   String                      ← from ExerciseDB API
      exerciseName: String
      muscleGroup:  String
      sets: [
        {
          setNumber:  Number (int)
          reps:       Number (int)
          weightKg:   Number (float)
          completed:  Boolean
        }
      ]
    }
  ]


/users/{uid}/aiSessions/{sessionId}             ← subcollection, one per chat session

  sessionDate:  Timestamp
  messages: [                                   ← Array, max 50 messages per doc then start new
    {
      role:      String                         ← "user" | "model"
      text:      String
      timestamp: Timestamp
    }
  ]
```

### Key Firestore Rules That Must Be Followed in Code

1. **Never call `.set(user)` on the user document after initial creation.** Use `.update(mapOf(...))` with only the fields you are changing. `.set()` will silently delete any field not included in the object.

2. **workout documents are append-only.** Never update or delete a workout document after it is created. If the user made a mistake, they can "delete" it (soft delete — add `deleted: true` field and filter it out on query).

3. **The `badges` and `badgeUnlockedAt` maps are append-only per key.** Once `badges.first_workout = true`, it must never be set back to `false`.

4. **User stat fields (`totalWorkouts`, `momentum`, `currentStreak`, etc.) must use `FieldValue.increment()` for counters, not read-then-write.** Read-then-write creates a race condition if two writes happen close together.

---

## BACKEND FILES TO REWRITE — WITH EXACT SPECIFICATIONS

### FILE 1: `WorkoutRepository.kt` — REWRITE COMPLETELY

**Remove:** Nothing from the existing file is wrong — add to it.
**Add these methods:**

`saveWorkoutAndUpdateStats(workout: Workout)` — This is the master save function. It must:
1. Write the workout document to `/users/{uid}/workouts/` using `.add()` (auto-ID)
2. Set `workout.id` to the new document ID using `.set()` with the ID included
3. In a **Firestore batch write** (`db.batch()`), update the user document with:
    - `totalWorkouts`: `FieldValue.increment(1)` (only if `isRecoveryDay == false`)
    - `totalMinutes`: `FieldValue.increment(workout.durationMinutes)`
    - `lastWorkoutDate`: `workout.dateString` (only if `isRecoveryDay == false`)
    - `lastMuscleGroup`: last exercise's muscleGroup (only if exercises not empty)
    - `lastActiveAt`: `FieldValue.serverTimestamp()`
4. Commit the batch
5. Return `Result<String>` where the String is the new workout document ID

`getWorkoutsByDateRange(startDate: String, endDate: String)` — queries with `.whereGreaterThanOrEqualTo("dateString", startDate).whereLessThanOrEqualTo("dateString", endDate)`

`getWorkoutsByMuscleGroup()` — returns a `Map<String, Int>` of muscleGroup → count, derived from all workouts. Fetch all, process in memory.

`deleteWorkout(workoutId: String)` — soft delete: `.update("deleted", true)`. All query methods must add `.whereEqualTo("deleted", false)` or `.whereNotEqualTo("deleted", true)`.

`getWorkoutsForProgress(limitDays: Int)` — returns workouts from the last `limitDays` days ordered by date descending, excluding recovery days.

### FILE 2: `UserRepository.kt` — REWRITE THE UPDATE METHOD

Replace `updateUserProfile(user: User)` with two separate methods:

`createUserProfile(user: User)` — used ONLY on first signup. Calls `.set(user)` to create the initial document. This is the only place `.set()` is allowed.

`updateProfileFields(fields: Map<String, Any>)` — used for all subsequent updates. Calls `.update(fields)`. The caller builds the map with only the keys they want to change. Never pass an entire User object here.

Add:

`updateMomentumAndStreak(newMomentum: Float, newStreak: Int, bestStreak: Int, lastWorkoutDate: String)` — atomic update of all momentum-related fields in one `.update()` call. Called by `MomentumCalculator` flow after every workout save and on app open.

`unlockBadge(badgeId: String)` — calls `.update(mapOf("badges.$badgeId" to true, "badgeUnlockedAt.$badgeId" to FieldValue.serverTimestamp()))`. The dot-notation key `"badges.$badgeId"` updates only that one field in the map without touching others.

`getUserStats()` — returns the full user document as `Result<User>`. Used by Progress and Profile screens.

### FILE 3: `MomentumCalculator.kt` — ADD THE ON-OPEN CALCULATION

The existing `calculateNewMomentum()` method is correct in its math. Add:

`calculateDecayOnOpen(storedMomentum: Float, lastWorkoutDate: String, momentumUpdatedAt: String)` → `Float`

Logic:
- Parse `momentumUpdatedAt` to LocalDate. If empty or null, return `storedMomentum` unchanged.
- Calculate days between `momentumUpdatedAt` and today's date.
- If `daysMissed == 0`, return `storedMomentum` unchanged (already calculated today).
- Otherwise apply the decay formula from `calculateNewMomentum()` with `workoutCompleted=false, lifeHappened=false`.
- Return the decayed value, clamped to `[0f, 100f]`.

This method is called from `HomeViewModel.loadHomeData()` immediately after reading the user document from Firestore, before setting `_momentumData.value`. If the result differs from `storedMomentum`, call `UserRepository.updateMomentumAndStreak()` to persist the decayed value.

### FILE 4: `BadgeChecker.kt` — REWRITE COMPLETELY

The current implementation is a stub. Replace with:

`checkAndUnlock(user: User, allWorkouts: List<Workout>)` → `List<String>` (returns list of newly unlocked badge IDs)

The function must:
1. Receive the full `user` object (which contains `user.badges: Map<String, Boolean>`)
2. For each badge ID, check `user.badges[badgeId] == true` first — if already unlocked, skip entirely
3. Evaluate the unlock condition using `user` and `allWorkouts`
4. Return only the IDs of badges newly unlocked this call

Badge unlock conditions (implement exactly these):

```
"first_workout"   → allWorkouts.count { !it.isRecoveryDay } >= 1
"workouts_5"      → allWorkouts.count { !it.isRecoveryDay } >= 5
"workouts_10"     → allWorkouts.count { !it.isRecoveryDay } >= 10
"workouts_30"     → allWorkouts.count { !it.isRecoveryDay } >= 30
"streak_3"        → user.currentStreak >= 3
"streak_7"        → user.currentStreak >= 7
"streak_14"       → user.currentStreak >= 14
"streak_30"       → user.currentStreak >= 30
"momentum_peak"   → user.momentum >= 85f
"leg_day_respect" → last 2 non-recovery workouts both had any exercise with muscleGroup == "legs"
"early_bird"      → the most recent workout's date Timestamp has hour < 8
"recovery_smart"  → allWorkouts.any { it.isRecoveryDay }
```

After `checkAndUnlock()` returns a non-empty list, the caller (ViewModel) must call `UserRepository.unlockBadge(id)` for each returned ID.

### FILE 5: `LogWorkoutActivity.kt` — THE MOST CRITICAL FIX

The current `btnFinishWorkout` click handler does nothing except navigate to `WorkoutCompleteActivity`. This must be replaced with a full save flow.

The activity needs a `LogWorkoutViewModel` (create this ViewModel). Do not put the save logic in the Activity directly.

`LogWorkoutViewModel` must have:

`saveWorkout(exercises: List<WorkoutExercise>, notes: String, startTimeMillis: Long)` — coroutine function that:
1. Builds a `Workout` object from parameters. `durationMinutes = (System.currentTimeMillis() - startTimeMillis) / 60000`. `dateString = today's date as "yyyy-MM-dd"`. `date = Timestamp.now()`. `totalSets = exercises.sumOf { it.sets.size }`.
2. Calls `WorkoutRepository.saveWorkoutAndUpdateStats(workout)`.
3. On success: fetches updated user document, fetches all workouts (or passes already-loaded list), calls `BadgeChecker.checkAndUnlock()`.
4. For each newly unlocked badge: calls `UserRepository.unlockBadge(id)`.
5. Calls `MomentumCalculator.calculateNewMomentum(currentMomentum, 0, workoutCompleted=true, lifeHappened=false)`.
6. Calculates new streak: if `lastWorkoutDate == yesterday`, `newStreak = currentStreak + 1`. If `lastWorkoutDate == today`, `newStreak = currentStreak` (already counted). Otherwise `newStreak = 1`.
7. Calls `UserRepository.updateMomentumAndStreak(newMomentum, newStreak, bestStreak, today)`.
8. Posts a `SaveResult` LiveData with `newBadges: List<String>`, `newMomentum: Float`, `newStreak: Int`.

The Activity observes `SaveResult`. On success, it passes the result as `Intent` extras to `WorkoutCompleteActivity` and calls `finish()`.

The exercise selection problem (dummy exercises): Add a button "Add Exercise from Library". This starts `ExerciseLibraryFragment` (or a dialog version of it) with `startActivityForResult`. When an exercise is selected, it returns the `Exercise` object via Intent extra. The activity adds it to `exerciseList` as a `WorkoutExercise`. This connects the library to the logger.

### FILE 6: `AICoachViewModel.kt` — FIX THE SYSTEM PROMPT

The current code sends the system prompt + user message merged as one. This is wrong.

The correct pattern with the Gemini SDK is:

Create a `buildHistory()` function that returns a `List<content>` containing a single `content { role = "user"; text(systemPrompt) }` and a single `content { role = "model"; text("Understood. I'm ready to coach.") }`. Pass this as `history = buildHistory()` to `generativeModel.startChat()`.

This way the system context is established once. Every subsequent `chat.sendMessage(userText)` sends only the user's actual message, and Gemini maintains the session context.

Also: currently the system prompt is rebuilt on every `sendMessage()` call, which means a new Firestore read on every message. Build the system prompt once in `init {}` (or on first message) and cache it as a `var systemPromptText: String` in the ViewModel. Only rebuild if `personalityMode` changes.

Add `saveSessionToFirestore()` — called `onCleared()` in the ViewModel. Saves `_messages.value` to `/users/{uid}/aiSessions/` as a new document. This is fire-and-forget (launch without await in `onCleared`).

### FILE 7: `ProgressViewModel.kt` — IMPLEMENT IT

Based on file size, this ViewModel is empty. It must:

`loadProgressData()`:
- Call `WorkoutRepository.getWorkoutsForProgress(limitDays = 90)`
- Map workouts to `List<Entry>` for MPAndroidChart (x = day index, y = momentum value or workout count)
- Call `WorkoutRepository.getWorkoutsByMuscleGroup()` for pie chart data
- Call `UserRepository.getUserStats()` for DNA card data
- Post all results as LiveData

`getWorkoutDNAText(user: User, workouts: List<Workout>)` → `String`
- Top muscle group: `workouts.flatMap { it.exercises }.groupBy { it.muscleGroup }.maxByOrNull { it.value.size }?.key`
- Avg per week: `workouts.size / max(1, daySpan / 7f)`
- Peak day: `workouts.groupBy { parseDayOfWeek(it.dateString) }.maxByOrNull { it.value.size }?.key`
- Build identity string from these values (same logic as in previous guide)

### FILE 8: `ProfileViewModel.kt` — IMPLEMENT IT

Must load: `UserRepository.getUserStats()` → posts user data. All 6 badge states with unlock status from `user.badges` map. Posts a `List<Badge>` with `isUnlocked` set from the map.

The `Badge` data class must add `unlockConditionText: String` field — shown in the AlertDialog when user taps a locked badge.

---

## RELATION MAP — HOW DATA FLOWS

```
SIGNUP FLOW:
AuthRepository.signUp()
  → UserRepository.createUserProfile(User with defaults)
  → PrefsManager.uid = uid
  → PrefsManager.onboardingDone = false
  → navigate to OnboardingActivity

ONBOARDING COMPLETE FLOW:
  → UserRepository.updateProfileFields(mapOf("fitnessGoal" to ..., "personalityMode" to ...))
  → PrefsManager.onboardingDone = true
  → navigate to MainActivity

APP OPEN FLOW (SplashActivity → MainActivity → HomeFragment):
  UserRepository.getUserStats()
    ↓
  MomentumCalculator.calculateDecayOnOpen(stored, lastDate, updatedAt)
    ↓ (if value changed)
  UserRepository.updateMomentumAndStreak(decayed, streak, best, lastDate)
    ↓
  HomeViewModel posts updated MomentumData to UI

WORKOUT SAVE FLOW (LogWorkoutActivity btnFinish):
  LogWorkoutViewModel.saveWorkout()
    ↓
  WorkoutRepository.saveWorkoutAndUpdateStats(workout)   ← batch write
    ↓
  [fetch updated user + all workouts]
    ↓
  BadgeChecker.checkAndUnlock(user, workouts)
    ↓ (for each new badge)
  UserRepository.unlockBadge(id)
    ↓
  MomentumCalculator.calculateNewMomentum(+12 for workout)
    ↓
  calculate new streak (yesterday = +1, today = same, else = 1)
    ↓
  UserRepository.updateMomentumAndStreak(newMom, newStreak, best, today)
    ↓
  LogWorkoutViewModel posts SaveResult
    ↓
  Activity starts WorkoutCompleteActivity with extras:
    { newBadges: List<String>, newMomentum: Float, newStreak: Int }

RECOVERY LOG FLOW (RecoveryLogBottomSheet confirm):
  Same as Workout Save Flow but:
    workout.isRecoveryDay = true
    momentum gain = +3 (not +12)
    streak does NOT increment
    totalWorkouts does NOT increment
    lastWorkoutDate does NOT update

PERSONALITY CHANGE FLOW (ProfileFragment selector):
  UserRepository.updateProfileFields(mapOf("personalityMode" to newMode))
    ↓
  PrefsManager.personalityMode = newMode
    ↓
  All LiveData observers that depend on PersonalityStrings refresh automatically
  (HomeViewModel.loadHomeData() must be called again, or observe a personality LiveData)

BADGE UNLOCK FLOW (called from within Workout Save Flow):
  For each badgeId in BadgeChecker result:
    UserRepository.unlockBadge(badgeId)  ← dot-notation Firestore update
    FitForgeAudioManager.playShort("badge")
    show badge unlock dialog in WorkoutCompleteActivity
```

---

## ADDITIONAL MISSING PIECES TO IMPLEMENT

### Missing: Exercise Selection in LogWorkoutActivity
`ExerciseLibraryFragment` exists but is never launched from `LogWorkoutActivity`. Add a floating button "Add Exercise" in `activity_log_workout.xml`. Clicking it starts `ExerciseDetailActivity` with an `Intent` extra `"select_mode" = true`. In `ExerciseDetailActivity`, when `select_mode` is true, show a "Add to Workout" button instead of "Start Exercise". This button calls `setResult(RESULT_OK, intent with exercise data)` and finishes. `LogWorkoutActivity` receives it in `onActivityResult` or the modern `ActivityResultLauncher` pattern and adds it to the exercise list.

### Missing: WorkoutCompleteActivity receives no data
Currently it shows static content. It must receive from Intent extras:
- `extra "new_badges"` → `ArrayList<String>` of badge IDs
- `extra "new_momentum"` → `Float`
- `extra "new_streak"` → `Int`
- `extra "workout_duration"` → `Int` (minutes)
- `extra "total_sets"` → `Int`

Use these to populate the stats row, momentum bar animation, and badge row.

### Missing: `ProgressFragment` is not connected to ViewModel
`ProgressFragment` must call `viewModel.loadProgressData()` in `onViewCreated` and observe the LiveData to populate:
- `LineChart` (MPAndroidChart) with momentum over time
- `BarChart` with workouts per week
- `PieChart` with muscle group distribution
- DNA card TextViews

### Missing: Momentum decay on login
`SplashActivity` must trigger the decay calculation before navigating to `MainActivity`. Not in `HomeViewModel` — at the splash level, so it's done once and the home screen always shows fresh data.

Call pattern in `SplashActivity`:
```
launch {
  val user = UserRepository().getUserStats().getOrNull()
  if (user != null) {
    val decayed = MomentumCalculator.calculateDecayOnOpen(user.momentum, user.lastWorkoutDate, user.momentumUpdatedAt)
    if (decayed != user.momentum) {
      UserRepository().updateMomentumAndStreak(decayed, user.currentStreak, user.bestStreak, user.lastWorkoutDate)
    }
  }
  // then navigate
}
```

### Missing: `momentumUpdatedAt` field in User model
Add `momentumUpdatedAt: String = ""` to `User.kt`. Set it to today's date string whenever momentum is updated. This is how `calculateDecayOnOpen` knows if decay has already been applied today.

### Missing: Firestore index for workout queries
The query `.orderBy("date").whereEqualTo("deleted", false)` requires a composite index. Add this to your Firebase Console → Firestore → Indexes:
- Collection: `workouts`
- Fields: `deleted` (Ascending) + `date` (Descending)
  Without this index, the query will throw an exception in production.

---

## AGENT RULES FOR THIS CONTINUATION

### ✅ DO
- Use `FieldValue.increment()` for all counter fields (`totalWorkouts`, `totalMinutes`, streak)
- Use `.update(mapOf(...))` for all user document updates after initial creation
- Use `db.batch()` when writing to multiple documents in one operation (workout doc + user stats)
- Always check `FirebaseAuth.currentUser?.uid` and return a clear `Result.failure` if null — never throw or crash
- Use `lifecycleScope.launch` in Activities and `viewModelScope.launch` in ViewModels
- Always post LiveData updates on the main thread — use `withContext(Dispatchers.Main)` if needed
- The `BadgeChecker` must always receive the existing `user.badges` map and check it before evaluating conditions

### 🚫 DO NOT
- Do not call `.set(user)` on the user document after signup — only `.update(mapOf(...))`
- Do not call `BadgeChecker` without passing the existing badge map — it will re-award badges endlessly
- Do not run Firestore operations on the main thread — always in coroutines
- Do not add fields to the `User` model without also updating `createUserProfile()` to include them in the initial document creation
- Do not send the Gemini system prompt on every message — set it once in `startChat(history = ...)`
- Do not read a counter value from Firestore and then write `value + 1` — this is a race condition; use `FieldValue.increment(1)`

---

## FIRESTORE SECURITY RULES — SET THESE BEFORE DEMO

In Firebase Console → Firestore → Rules, replace everything with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /workouts/{workoutId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }

      match /aiSessions/{sessionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

This ensures no user can read or write another user's data. Without this, the database is in test mode and completely open.

---

*End of FittestForge Backend Fix & Continuation Guide*
*Based on full structural review of https://github.com/Mustafa-Noor/FittestForge*
