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

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProfileData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = userRepository.getUserStats()
                result.onSuccess { userDoc ->
                    _user.value = userDoc
                    _badges.value = getBadgesWithStatus(userDoc.badges)
                }.onFailure {
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
        val allBadges = listOf(
            Badge("first_workout", "First Blood", "🔥", "Completed your first workout", unlockConditionText = "Log any workout"),
            Badge("workouts_5", "High Five", "🖐️", "Completed 5 workouts", unlockConditionText = "Log 5 workouts"),
            Badge("workouts_30", "Iron Veteran", "🛡️", "Completed 30 workouts", unlockConditionText = "Log 30 workouts"),
            Badge("streak_7", "Weekly Warrior", "📅", "7-day workout streak", unlockConditionText = "Work out 7 days in a row"),
            Badge("momentum_peak", "Peak Performance", "⚡", "Reach 85 momentum", unlockConditionText = "Get momentum above 85"),
            Badge("leg_day_respect", "Leg Day Hero", "🍗", "Logged legs twice in a row", unlockConditionText = "Don't skip leg day!")
        )
        
        return allBadges.map { badge ->
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
