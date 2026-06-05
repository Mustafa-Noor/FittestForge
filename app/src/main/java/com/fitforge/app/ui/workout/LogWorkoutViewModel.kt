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
        val error: String? = null
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

                // 1. Save workout and update base stats
                workoutRepository.saveWorkoutAndUpdateStats(workout).onSuccess {
                    
                    // 2. Fetch updated user to check badges and momentum
                    val user = userRepository.getUserProfile().getOrNull() ?: return@onSuccess
                    val allWorkouts = workoutRepository.getWorkouts().getOrNull() ?: emptyList()

                    // 3. Check for new badges
                    val newlyUnlocked = BadgeChecker.checkAndUnlock(user, allWorkouts)
                    newlyUnlocked.forEach { badgeId ->
                        userRepository.unlockBadge(badgeId)
                    }

                    // 4. Calculate Momentum
                    val newMomentum = MomentumCalculator.calculateNewMomentum(
                        currentValue = user.momentum,
                        daysMissed = 0,
                        workoutCompleted = true,
                        lifeHappened = false
                    )

                    // 5. Calculate Streak
                    var newStreak = user.currentStreak
                    val lastWorkoutDateStr = user.lastWorkoutDate
                    
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

                    val bestStreak = if (newStreak > user.bestStreak) newStreak else user.bestStreak

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
                        newStreak = newStreak
                    )
                }.onFailure {
                    _saveResult.value = SaveResult(success = false, error = it.message)
                }
            } catch (e: Exception) {
                _saveResult.value = SaveResult(success = false, error = e.message)
            }
        }
    }
}
