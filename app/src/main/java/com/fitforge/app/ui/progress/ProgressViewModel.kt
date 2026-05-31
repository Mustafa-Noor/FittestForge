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
                val workoutResult = workoutRepository.getWorkouts()
                val workouts = workoutResult.getOrNull() ?: emptyList()

                val userResult = userRepository.getUserProfile()
                val user = userResult.getOrNull()

                // Calculate momentum history (last 14 days)
                val momentumPoints = mutableListOf<MomentumPoint>()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var currentValue = user?.momentum ?: 50f

                for (i in 13 downTo 0) {
                    val date = LocalDate.now().minusDays(i.toLong())
                    val dateStr = date.format(formatter)
                    momentumPoints.add(MomentumPoint(dateStr, currentValue))
                    
                    if (workouts.none { it.dateString == dateStr }) {
                        currentValue = MomentumCalculator.calculateNewMomentum(
                            currentValue, 1, false, false
                        )
                    }
                }
                _momentumData.value = momentumPoints

                // Calculate Workout DNA
                if (workouts.isNotEmpty()) {
                    val dna = calculateWorkoutDNA(workouts, user)
                    _workoutDNA.value = dna
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load progress data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun calculateWorkoutDNA(
        workouts: List<Workout>,
        user: com.fitforge.app.data.models.User?
    ): WorkoutDNA {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        // Calculate avg workouts per week
        val days = (workouts.maxOfOrNull { LocalDate.parse(it.dateString) } 
            ?: LocalDate.now()).let {
            java.time.temporal.ChronoUnit.DAYS.between(
                workouts.minOfOrNull { w -> LocalDate.parse(w.dateString) } ?: LocalDate.now(),
                it
            ) + 1
        }
        val weeks = days / 7f
        val avgPerWeek = if (weeks > 0) workouts.size / weeks else workouts.size.toFloat()

        // Find top muscle group
        val muscleGroups = workouts.flatMap { workout ->
            workout.exercises.map { it.muscleGroup }
        }.groupingBy { it }.eachCount()
        
        val topMuscle = muscleGroups.maxByOrNull { it.value }?.key ?: "Balanced"
        val topMusclePercent = if (muscleGroups.isNotEmpty()) {
            ((muscleGroups[topMuscle] ?: 0) * 100 / muscleGroups.values.sum())
        } else 0

        // Peak day
        val peakDay = workouts.groupBy { workout ->
            java.time.DayOfWeek.from(java.time.LocalDate.parse(workout.dateString)).name
        }.maxByOrNull { it.value.size }?.key ?: "Unknown"

        // Archetype
        val archetype = when {
            topMusclePercent > 40 -> "$topMuscle Loyalist"
            avgPerWeek < 2 -> "Weekend Warrior"
            avgPerWeek > 5 -> "Daily Grinder"
            else -> "Consistent Athlete"
        }

        return WorkoutDNA(
            archetype = archetype,
            avgWorkoutsPerWeek = avgPerWeek,
            topMuscleGroup = topMuscle,
            topMuscleGroupPercent = topMusclePercent,
            peakDay = peakDay.replace("_", " "),
            bestStreak = user?.bestStreak ?: 0,
            totalMinutes = user?.totalMinutes ?: 0
        )
    }
}