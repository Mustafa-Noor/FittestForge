package com.fitforge.app.ui.workout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.utils.BadgeChecker
import com.fitforge.app.utils.MomentumCalculator
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class LogWorkoutViewModel : ViewModel() {

    private val workoutRepository = WorkoutRepository()
    private val userRepository = UserRepository()

    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    data class SaveResult(
        val success: Boolean,
        val newBadges: List<String> = emptyList(),
        val newMomentum: Float = 0f,
        val newStreak: Int = 0,
        val error: String? = null,
        val personalityMode: String = "hype"
    )

    fun saveWorkout(exercises: List<WorkoutExercise>, notes: String, startTimeMillis: Long) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                val durationMinutes = ((System.currentTimeMillis() - startTimeMillis) / 60000).toInt()
                val totalSets = exercises.sumOf { it.sets.size }

                val workout = Workout(
                    date = Timestamp.now(),
                    dateString = todayString,
                    durationMinutes = durationMinutes,
                    totalSets = totalSets,
                    notes = notes,
                    exercises = exercises
                )

                // 0. Fetch user to get previous stats for streak calculation
                val initialUserResult = userRepository.getUserProfile()
                if (initialUserResult.isFailure) {
                    _saveResult.value = SaveResult(success = false, error = "Failed to fetch user profile")
                    return@launch
                }
                val userBeforeWorkout = initialUserResult.getOrNull() ?: return@launch

                // 1. Calculate Streak using previous lastWorkoutDate
                var newStreak = userBeforeWorkout.currentStreak
                val lastWorkoutDateStr = userBeforeWorkout.lastWorkoutDate
                
                if (lastWorkoutDateStr.isNotEmpty()) {
                    val lastDate = LocalDate.parse(lastWorkoutDateStr)
                    val daysBetween = ChronoUnit.DAYS.between(lastDate, today)
                    
                    if (daysBetween == 1L) {
                        newStreak += 1
                    } else if (daysBetween > 1L) {
                        newStreak = 1
                    }
                    // if daysBetween == 0, streak remains same (already logged today)
                } else {
                    newStreak = 1
                }
                val bestStreak = if (newStreak > userBeforeWorkout.bestStreak) newStreak else userBeforeWorkout.bestStreak

                // 2. Save workout and update base stats
                val saveResult = workoutRepository.saveWorkoutAndUpdateStats(workout)
                if (saveResult.isSuccess) {
                    // 3. Fetch updated user to check badges and momentum
                    val userResult = userRepository.getUserProfile()
                    val user = userResult.getOrNull()
                    if (user == null) {
                        _saveResult.value = SaveResult(success = false, error = "Failed to fetch user profile")
                        return@launch
                    }
                    val workoutsResult = workoutRepository.getWorkouts()
                    val allWorkouts = workoutsResult.getOrNull() ?: emptyList()

                    // 4. Check for new badges
                    val newlyUnlocked = BadgeChecker.checkAndUnlock(user, allWorkouts)
                    newlyUnlocked.forEach { badgeId ->
                        userRepository.unlockBadge(badgeId)
                    }

                    // 5. Calculate Momentum
                    val newMomentum = MomentumCalculator.calculateNewMomentum(
                        currentValue = user.momentum,
                        daysMissed = 0,
                        workoutCompleted = true,
                        lifeHappened = false
                    )

                    // 6. Update user with new momentum and streak
                    userRepository.updateMomentumAndStreak(
                        newMomentum = newMomentum,
                        newStreak = newStreak,
                        bestStreak = bestStreak,
                        lastWorkoutDate = todayString
                    )

                    _saveResult.value = SaveResult(
                        success = true,
                        newBadges = newlyUnlocked,
                        newMomentum = newMomentum,
                        newStreak = newStreak,
                        personalityMode = user.personalityMode
                    )
                } else {
                    _saveResult.value = SaveResult(success = false, error = saveResult.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _saveResult.value = SaveResult(success = false, error = e.message)
            }
        }
    }

    fun logRecovery(reason: String) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val workout = Workout(
                    date = Timestamp.now(),
                    dateString = todayString,
                    durationMinutes = 0,
                    totalSets = 0,
                    notes = "Recovery day: $reason",
                    isRecoveryDay = true,
                    recoveryReason = reason,
                    exercises = emptyList()
                )

                // 1. Save workout (recovery day)
                val saveResult = workoutRepository.saveWorkoutAndUpdateStats(workout)
                if (saveResult.isSuccess) {
                    // 2. Fetch updated user to check momentum and badges
                    val userResult = userRepository.getUserProfile()
                    val user = userResult.getOrNull()
                    if (user == null) {
                        _saveResult.value = SaveResult(success = false, error = "Failed to fetch user profile")
                        return@launch
                    }
                    val workoutsResult = workoutRepository.getWorkouts()
                    val allWorkouts = workoutsResult.getOrNull() ?: emptyList()

                    // 3. Check for new badges (recovery_smart unlocks here)
                    val newlyUnlocked = BadgeChecker.checkAndUnlock(user, allWorkouts)
                    newlyUnlocked.forEach { badgeId ->
                        userRepository.unlockBadge(badgeId)
                    }

                    // 4. Calculate Momentum
                    val newMomentum = MomentumCalculator.calculateNewMomentum(
                        currentValue = user.momentum,
                        daysMissed = 0,
                        workoutCompleted = false,
                        lifeHappened = true
                    )

                    // 5. Update user with new momentum and streak
                    userRepository.updateMomentumAndStreak(
                        newMomentum = newMomentum,
                        newStreak = user.currentStreak, // Streak does NOT increment/change on recovery day
                        bestStreak = user.bestStreak,
                        lastWorkoutDate = user.lastWorkoutDate // Last workout date remains unchanged
                    )

                    _saveResult.value = SaveResult(
                        success = true,
                        newBadges = newlyUnlocked,
                        newMomentum = newMomentum,
                        newStreak = user.currentStreak,
                        personalityMode = user.personalityMode
                    )
                } else {
                    _saveResult.value = SaveResult(success = false, error = saveResult.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _saveResult.value = SaveResult(success = false, error = e.message)
            }
        }
    }
}
