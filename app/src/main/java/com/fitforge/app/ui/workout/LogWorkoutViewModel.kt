package com.fitforge.app.ui.workout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.User
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.utils.BadgeChecker
import com.fitforge.app.utils.MomentumCalculator
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SaveResult(
    val newBadges: List<String>,
    val newMomentum: Float,
    val newStreak: Int,
    val durationMinutes: Int,
    val totalSets: Int
)

class LogWorkoutViewModel : ViewModel() {
    private val workoutRepository = WorkoutRepository()
    private val userRepository = UserRepository()

    private val _saveResult = MutableLiveData<Result<SaveResult>>()
    val saveResult: LiveData<Result<SaveResult>> = _saveResult

    fun saveWorkout(exercises: List<WorkoutExercise>, notes: String, startTimeMillis: Long) {
        viewModelScope.launch {
            try {
                val durationMinutes = ((System.currentTimeMillis() - startTimeMillis) / 60000).toInt()
                val totalSets = exercises.sumOf { it.sets.size }
                val today = LocalDate.now().toString()

                val workout = Workout(
                    date = Timestamp.now(),
                    dateString = today,
                    durationMinutes = durationMinutes,
                    totalSets = totalSets,
                    notes = notes,
                    isRecoveryDay = false,
                    exercises = exercises
                )

                // 1. Save workout and update base stats
                workoutRepository.saveWorkoutAndUpdateStats(workout).getOrThrow()

                // 2. Fetch updated user and all workouts for badge check
                val user = userRepository.getUserStats().getOrThrow()
                val allWorkouts = workoutRepository.getWorkouts().getOrThrow()

                // 3. Check and unlock badges
                val newBadges = BadgeChecker.checkAndUnlock(user, allWorkouts)
                newBadges.forEach { badgeId ->
                    userRepository.unlockBadge(badgeId).getOrThrow()
                }

                // 4. Calculate new momentum
                val newMomentum = MomentumCalculator.calculateNewMomentum(
                    currentValue = user.momentum,
                    daysMissed = 0, // Just completed a workout
                    workoutCompleted = true,
                    lifeHappened = false
                )

                // 5. Calculate new streak
                val newStreak = calculateNewStreak(user.currentStreak, user.lastWorkoutDate, today)
                val bestStreak = if (newStreak > user.bestStreak) newStreak else user.bestStreak

                // 6. Update momentum and streak
                userRepository.updateMomentumAndStreak(newMomentum, newStreak, bestStreak, today).getOrThrow()

                _saveResult.value = Result.success(SaveResult(newBadges, newMomentum, newStreak, durationMinutes, totalSets))
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    private fun calculateNewStreak(currentStreak: Int, lastWorkoutDate: String, today: String): Int {
        if (lastWorkoutDate.isEmpty()) return 1
        
        val lastDate = LocalDate.parse(lastWorkoutDate)
        val todayDate = LocalDate.parse(today)
        val daysBetween = ChronoUnit.DAYS.between(lastDate, todayDate)

        return when {
            daysBetween == 0L -> currentStreak // Already worked out today
            daysBetween == 1L -> currentStreak + 1
            else -> 1 // Streak broken
        }
    }

    fun logRecovery(reason: String) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                val workout = Workout(
                    date = Timestamp.now(),
                    dateString = today,
                    isRecoveryDay = true,
                    recoveryReason = reason
                )

                workoutRepository.saveWorkoutAndUpdateStats(workout).getOrThrow()
                
                val user = userRepository.getUserStats().getOrThrow()
                val allWorkouts = workoutRepository.getWorkouts().getOrThrow()

                // Recovery can also unlock badges (e.g., recovery_smart)
                val newBadges = BadgeChecker.checkAndUnlock(user, allWorkouts)
                newBadges.forEach { badgeId ->
                    userRepository.unlockBadge(badgeId).getOrThrow()
                }

                val newMomentum = MomentumCalculator.calculateNewMomentum(
                    currentValue = user.momentum,
                    daysMissed = 0,
                    workoutCompleted = false,
                    lifeHappened = true
                )

                // Recovery day doesn't increment streak, but doesn't necessarily break it in some systems.
                // Guide says "streak does NOT increment". We'll keep it as is.
                userRepository.updateMomentumAndStreak(newMomentum, user.currentStreak, user.bestStreak, user.lastWorkoutDate).getOrThrow()

                _saveResult.value = Result.success(SaveResult(newBadges, newMomentum, user.currentStreak, 0, 0))
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }
}
