package com.fitforge.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.Badge
import com.fitforge.app.data.models.User
import com.fitforge.app.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _badges = MutableLiveData<List<Badge>>()
    val badges: LiveData<List<Badge>> = _badges

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProfileData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.getUserStats()
                    .onSuccess { userDoc ->
                        _user.value = userDoc
                        _badges.value = getBadgesWithStatus(userDoc.badges)
                    }
                    .onFailure {
                        _error.value = "Failed to load profile"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getBadgesWithStatus(unlockedMap: Map<String, Boolean>): List<Badge> {
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

    fun updatePersonalityMode(mode: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfileFields(mapOf("personalityMode" to mode))
                val currentUser = _user.value
                if (currentUser != null) {
                    _user.value = currentUser.copy(personalityMode = mode)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update personality mode"
            }
        }
    }
}
