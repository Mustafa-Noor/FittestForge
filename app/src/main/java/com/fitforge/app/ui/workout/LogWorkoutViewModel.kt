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
                val normalizedExercises = exercises.map { exercise ->
                    exercise.copy(
                        sets = exercise.sets.mapIndexed { index, set ->
                            set.copy(setNumber = index + 1)
                        }
                    )
                }
                val totalSets = normalizedExercises.sumOf { exercise ->
                    exercise.sets.count { it.completed }
                }.let { completedSets ->
                    if (completedSets > 0) completedSets else normalizedExercises.sumOf { it.sets.size }
                }
                val today = LocalDate.now().toString()
                val userBeforeSave = userRepository.getUserStats().getOrThrow()

                val workout = Workout(
                    date = Timestamp.now(),
                    dateString = today,
                    durationMinutes = durationMinutes,
                    totalSets = totalSets,
                    notes = notes,
                    isRecoveryDay = false,
                    exercises = normalizedExercises
                )

                // 1. Save workout and update base stats
                workoutRepository.saveWorkoutAndUpdateStats(workout).getOrThrow()

                // 2. Calculate momentum and streak from the pre-save user snapshot.
                val newMomentum = MomentumCalculator.calculateNewMomentum(
                    currentValue = userBeforeSave.momentum,
                    daysMissed = 0,
                    workoutCompleted = true,
                    lifeHappened = false
                )
                val newStreak = calculateNewStreak(userBeforeSave.currentStreak, userBeforeSave.lastWorkoutDate, today)
                val bestStreak = if (newStreak > userBeforeSave.bestStreak) newStreak else userBeforeSave.bestStreak

                userRepository.updateMomentumAndStreak(newMomentum, newStreak, bestStreak, today).getOrThrow()

                // 3. Fetch workouts and check badges with this workout's new momentum/streak included.
                val allWorkouts = workoutRepository.getWorkouts().getOrThrow()
                val userForBadges = userBeforeSave.copy(
                    momentum = newMomentum,
                    currentStreak = newStreak,
                    bestStreak = bestStreak,
                    lastWorkoutDate = today
                )

                val newBadges = BadgeChecker.checkAndUnlock(userForBadges, allWorkouts)
                newBadges.forEach { badgeId ->
                    userRepository.unlockBadge(badgeId).getOrThrow()
                }

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
