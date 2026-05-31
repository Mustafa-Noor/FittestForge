package com.fitforge.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.MomentumData
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.utils.MomentumCalculator
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val workoutRepository = WorkoutRepository()

    private val _momentumData = MutableLiveData<MomentumData>()
    val momentumData: LiveData<MomentumData> = _momentumData

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val userResult = userRepository.getUserProfile()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                _userName.value = user?.displayName ?: "Athlete"
            }
            
            // For now, load default momentum. Typically this would be read from user doc.
            val currentMomentum = MomentumData(
                value = 50f, 
                label = "Building Up", 
                colorRes = android.R.color.holo_orange_dark, 
                streak = 0, 
                lastUpdated = ""
            )
            _momentumData.value = currentMomentum
        }
    }
}