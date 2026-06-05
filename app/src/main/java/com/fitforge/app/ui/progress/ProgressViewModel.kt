package com.fitforge.app.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.Badge
import com.fitforge.app.data.models.User
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    private val _badges = MutableLiveData<List<Badge>>()
    val badges: LiveData<List<Badge>> = _badges

    private val _hasWorkoutData = MutableLiveData(false)
    val hasWorkoutData: LiveData<Boolean> = _hasWorkoutData

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
                _hasWorkoutData.value = workouts.isNotEmpty()

                val momentumPoints = calculateMomentumHistory(workouts)
                _momentumData.value = momentumPoints

                if (user != null) {
                    _workoutDNA.value = calculateWorkoutDNA(workouts, user)
                    _badges.value = buildBadges(user.badges)
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load progress data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun calculateMomentumHistory(workouts: List<Workout>): List<MomentumPoint> {
        if (workouts.isEmpty()) return emptyList()

        val workoutsByDate = workouts.groupBy { it.dateString }
        return workoutsByDate.keys.sorted().map { date ->
            val dailyWorkouts = workoutsByDate[date].orEmpty()
            val completedSets = dailyWorkouts.sumOf { workout ->
                workout.exercises.sumOf { exercise -> exercise.sets.count { it.completed } }
            }
            val fallbackSets = dailyWorkouts.sumOf { it.totalSets }
            MomentumPoint(date, (completedSets.takeIf { it > 0 } ?: fallbackSets).toFloat())
        }
    }

    private fun calculateWorkoutDNA(workouts: List<Workout>, user: User): WorkoutDNA {
        if (workouts.isEmpty()) {
            return WorkoutDNA("No workout data yet", 0f, "None", 0, "None", user.bestStreak, user.totalMinutes)
        }

        // Avg per week
        val firstWorkoutDate = workouts.minOfOrNull { LocalDate.parse(it.dateString) } ?: LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(firstWorkoutDate, LocalDate.now())
        val weeks = max(1f, daysDiff.toFloat() / 7f)
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

    private fun buildBadges(unlockedMap: Map<String, Boolean>): List<Badge> {
        return listOf(
            Badge("first_workout", "First Workout", "FW", "Completed your first workout", unlockConditionText = "Log any workout"),
            Badge("workouts_5", "High Five", "5", "Completed 5 workouts", unlockConditionText = "Log 5 workouts"),
            Badge("workouts_10", "Ten Strong", "10", "Completed 10 workouts", unlockConditionText = "Log 10 workouts"),
            Badge("workouts_30", "Iron Habit", "30", "Completed 30 workouts", unlockConditionText = "Log 30 workouts"),
            Badge("streak_3", "Three-Day Spark", "3", "Built a 3-day streak", unlockConditionText = "Work out 3 days in a row"),
            Badge("streak_7", "Weekly Warrior", "7", "Built a 7-day streak", unlockConditionText = "Work out 7 days in a row"),
            Badge("streak_14", "Fortnight Fire", "14", "Built a 14-day streak", unlockConditionText = "Work out 14 days in a row"),
            Badge("streak_30", "Monthly Machine", "30", "Built a 30-day streak", unlockConditionText = "Work out 30 days in a row"),
            Badge("momentum_peak", "Peak Momentum", "PK", "Reached 85 momentum", unlockConditionText = "Raise momentum to 85"),
            Badge("leg_day_respect", "Leg Day Respect", "LG", "Logged legs twice in a row", unlockConditionText = "Log two leg workouts in a row"),
            Badge("early_bird", "Early Bird", "AM", "Logged a workout before 8 AM", unlockConditionText = "Finish a workout before 8 AM"),
            Badge("recovery_smart", "Recovery Smart", "RS", "Logged a recovery day", unlockConditionText = "Use Life Happened once")
        ).map { badge ->
            badge.copy(isUnlocked = unlockedMap[badge.id] == true)
        }
    }
}
