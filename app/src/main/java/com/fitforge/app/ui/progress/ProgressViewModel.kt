package com.fitforge.app.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.utils.MomentumCalculator
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max

data class MomentumPoint(val date: String, val value: Float)
data class WorkoutDNA(
    val archetype: String,
    val avgWorkoutsPerWeek: Float,
    val topMuscleGroup: String,
    val topMuscleGroupPercent: Int,
    val peakDay: String,
    val bestStreak: Int,
    val totalMinutes: Int
)

class ProgressViewModel : ViewModel() {

    private val workoutRepository = WorkoutRepository()
    private val userRepository = UserRepository()

    private val _momentumData = MutableLiveData<List<MomentumPoint>>()
    val momentumData: LiveData<List<MomentumPoint>> = _momentumData

    private val _muscleGroupData = MutableLiveData<Map<String, Int>>()
    val muscleGroupData: LiveData<Map<String, Int>> = _muscleGroupData

    private val _workoutDNA = MutableLiveData<WorkoutDNA>()
    val workoutDNA: LiveData<WorkoutDNA> = _workoutDNA

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProgressData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val workoutsResult = workoutRepository.getWorkoutsForProgress(90)
                val workouts = workoutsResult.getOrDefault(emptyList())

                val muscleResult = workoutRepository.getWorkoutsByMuscleGroup()
                _muscleGroupData.value = muscleResult.getOrDefault(emptyMap())

                val userResult = userRepository.getUserStats()
                val user = userResult.getOrNull()

                // Momentum History
                val momentumPoints = calculateMomentumHistory(workouts, user?.momentum ?: 50f)
                _momentumData.value = momentumPoints

                // Workout DNA
                if (user != null) {
                    _workoutDNA.value = calculateWorkoutDNA(workouts, user)
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load progress data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun calculateMomentumHistory(workouts: List<Workout>, currentMomentum: Float): List<MomentumPoint> {
        val points = mutableListOf<MomentumPoint>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        
        // Simplified: Start from current and work backwards for 14 days for the chart
        var runningMomentum = currentMomentum
        val workoutMap = workouts.associateBy { it.dateString }

        for (i in 0..13) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.format(formatter)
            points.add(0, MomentumPoint(dateStr, runningMomentum))
            
            // Reverse momentum for history is complex, so we just show the trend
            // In a real app, we might store momentum history in Firestore
            if (workoutMap.containsKey(dateStr)) {
                runningMomentum -= 12f
            } else {
                runningMomentum += 5f // Rough estimate of decay
            }
            runningMomentum = runningMomentum.coerceIn(0f, 100f)
        }
        return points
    }

    private fun calculateWorkoutDNA(workouts: List<Workout>, user: com.fitforge.app.data.models.User): WorkoutDNA {
        if (workouts.isEmpty()) {
            return WorkoutDNA("Beginner", 0f, "None", 0, "None", user.bestStreak, user.totalMinutes)
        }

        // Avg per week
        val firstWorkoutDate = workouts.minOfOrNull { LocalDate.parse(it.dateString) } ?: LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(firstWorkoutDate, LocalDate.now())
        val weeks = max(1f, daysDiff / 7f)
        val avgPerWeek = workouts.size / weeks

        // Top Muscle Group
        val muscleGroups = workouts.flatMap { it.exercises }
            .groupBy { it.muscleGroup }
            .mapValues { it.value.size }
        val topMuscle = muscleGroups.maxByOrNull { it.value }?.key ?: "Balanced"
        val totalExercises = muscleGroups.values.sum()
        val topMusclePercent = if (totalExercises > 0) (muscleGroups[topMuscle]!! * 100 / totalExercises) else 0

        // Peak Day
        val peakDayRaw = workouts.groupBy { 
            LocalDate.parse(it.dateString).dayOfWeek.name 
        }.maxByOrNull { it.value.size }?.key ?: "Unknown"

        val peakDay = peakDayRaw.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val archetype = when {
            topMusclePercent > 40 -> "$topMuscle Specialist"
            avgPerWeek > 4 -> "Daily Grinder"
            avgPerWeek < 2 -> "Weekend Warrior"
            else -> "Consistent Athlete"
        }

        return WorkoutDNA(
            archetype = archetype,
            avgWorkoutsPerWeek = avgPerWeek,
            topMuscleGroup = topMuscle,
            topMuscleGroupPercent = topMusclePercent,
            peakDay = peakDay,
            bestStreak = user.bestStreak,
            totalMinutes = user.totalMinutes
        )
    }
}
