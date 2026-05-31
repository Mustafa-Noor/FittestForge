package com.fitforge.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.User
import com.fitforge.app.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = userRepository.getUserProfile()
                _user.value = result.getOrNull()
                if (result.isFailure) {
                    _error.value = "Failed to load profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePersonalityMode(mode: String) {
        viewModelScope.launch {
            try {
                userRepository.updatePersonalityMode(mode)
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
