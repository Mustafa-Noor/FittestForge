package com.fitforge.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.MomentumData
import com.fitforge.app.data.models.PersonalityMode
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.utils.MomentumCalculator
import com.fitforge.app.utils.PersonalityStrings
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val workoutRepository = WorkoutRepository()

    private val _momentumData = MutableLiveData<MomentumData>()
    val momentumData: LiveData<MomentumData> = _momentumData

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _personalityMode = MutableLiveData<PersonalityMode>()
    val personalityMode: LiveData<PersonalityMode> = _personalityMode

    private val _homeMessage = MutableLiveData<String>()
    val homeMessage: LiveData<String> = _homeMessage

    private val _recentWorkouts = MutableLiveData<List<com.fitforge.app.data.models.Workout>>()
    val recentWorkouts: LiveData<List<com.fitforge.app.data.models.Workout>> = _recentWorkouts

    private val _totalWorkouts = MutableLiveData<Int>()
    val totalWorkouts: LiveData<Int> = _totalWorkouts

    private val _totalHours = MutableLiveData<String>()
    val totalHours: LiveData<String> = _totalHours

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            val userResult = userRepository.getUserProfile()
            val user = userResult.getOrNull()
            _userName.value = user?.displayName ?: "Athlete"
            _totalWorkouts.value = user?.totalWorkouts ?: 0
            
            val hours = (user?.totalMinutes ?: 0) / 60f
            _totalHours.value = String.format("%.1fh", hours)
            
            val modeString = user?.personalityMode ?: "hype"
            val mode = PersonalityMode.values().find { it.value == modeString } ?: PersonalityMode.HYPE
            _personalityMode.value = mode

            // Load workouts
            val workoutResult = workoutRepository.getRecentWorkouts(3)
            val workouts = workoutResult.getOrDefault(emptyList())
            _recentWorkouts.value = workouts

            // Momentum logic with decay on open
            var currentMomentumValue = user?.momentum ?: 0f
            if (user != null && user.momentumUpdatedAt.isNotEmpty()) {
                val decayedMomentum = MomentumCalculator.calculateDecayOnOpen(
                    storedMomentum = user.momentum,
                    lastWorkoutDate = user.lastWorkoutDate,
                    momentumUpdatedAt = user.momentumUpdatedAt
                )
                
                if (decayedMomentum != user.momentum) {
                    currentMomentumValue = decayedMomentum
                    // Update Firestore silently
                    userRepository.updateProfileFields(mapOf(
                        "momentum" to currentMomentumValue,
                        "momentumUpdatedAt" to java.time.LocalDate.now().toString()
                    ))
                }
            }
            
            val momentumLabel = MomentumCalculator.getMomentumLabel(currentMomentumValue)

            val currentMomentum = MomentumData(
                value = currentMomentumValue, 
                label = momentumLabel, 
                colorRes = android.R.color.holo_blue_dark, 
                streak = user?.currentStreak ?: 0,
                lastUpdated = user?.momentumUpdatedAt ?: ""
            )
            _momentumData.value = currentMomentum

            val currentStreak = user?.currentStreak ?: 0
            val totalWorkouts = user?.totalWorkouts ?: 0
            _homeMessage.value = when {
                workouts.isEmpty() -> "Welcome to FitForge! Your first workout is waiting. Let's build something amazing ✨"
                currentStreak >= 3 -> PersonalityStrings.getStreakMessage(mode, currentStreak, totalWorkouts)
                currentMomentumValue > 60 -> PersonalityStrings.getMomentumHighMessage(mode)
                currentMomentumValue < 35 -> PersonalityStrings.getMissedDayMessage(mode)
                else -> PersonalityStrings.getMomentumLowMessage(mode)
            }
        }
    }
}