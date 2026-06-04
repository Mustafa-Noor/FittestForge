package com.fitforge.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.MomentumData
import com.fitforge.app.data.models.PersonalityMode
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
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

            // Momentum logic based on real data or default for new users
            val currentMomentumValue = user?.momentum ?: 0f
            
            val momentumLabel = when {
                currentMomentumValue >= 80 -> "Elite Momentum"
                currentMomentumValue >= 60 -> "Strong Momentum"
                currentMomentumValue >= 40 -> "Building Up"
                currentMomentumValue > 0 -> "Getting Started"
                else -> "New Journey"
            }

            val currentMomentum = MomentumData(
                value = currentMomentumValue, 
                label = momentumLabel, 
                colorRes = android.R.color.holo_blue_dark, 
                streak = user?.currentStreak ?: 0,
                lastUpdated = ""
            )
            _momentumData.value = currentMomentum

            _homeMessage.value = when {
                workouts.isEmpty() -> "Welcome to FitForge! Start your first workout to build momentum. ✨"
                currentMomentumValue > 50 -> PersonalityStrings.getMomentumHighMessage(mode)
                else -> PersonalityStrings.getMomentumLowMessage(mode)
            }
        }
    }
}