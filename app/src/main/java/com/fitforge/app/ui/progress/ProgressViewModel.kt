package com.fitforge.app.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.repository.WorkoutRepository
import kotlinx.coroutines.launch

class ProgressViewModel : ViewModel() {

    private val workoutRepository = WorkoutRepository()

    private val _workouts = MutableLiveData<List<Workout>>()
    val workouts: LiveData<List<Workout>> = _workouts

    private val _momentumHistory = MutableLiveData<List<Pair<String, Float>>>()
    val momentumHistory: LiveData<List<Pair<String, Float>>> = _momentumHistory

    fun loadProgressData() {
        viewModelScope.launch {
            val result = workoutRepository.getWorkouts()
            val workoutList = result.getOrDefault(emptyList())
            _workouts.value = workoutList
            
            // Simplified momentum history based on duration for visualization
            val history = workoutList.map { it.dateString to it.durationMinutes.toFloat() }
            _momentumHistory.value = history
        }
    }
}