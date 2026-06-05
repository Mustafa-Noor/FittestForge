package com.fitforge.app.ui.workout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.models.Exercise
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.repository.ExerciseRepository
import com.fitforge.app.data.repository.WorkoutRepository
import kotlinx.coroutines.launch

class WorkoutViewModel : ViewModel() {

    private val exerciseRepository = ExerciseRepository()
    private val workoutRepository = WorkoutRepository()

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises

    private val _isExerciseLoading = MutableLiveData(false)
    val isExerciseLoading: LiveData<Boolean> = _isExerciseLoading

    private val _exerciseError = MutableLiveData<String?>()
    val exerciseError: LiveData<String?> = _exerciseError

    private val _history = MutableLiveData<List<Workout>>()
    val history: LiveData<List<Workout>> = _history

    init {
        loadExercises()
        loadHistory()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _isExerciseLoading.value = true
            _exerciseError.value = null

            val result = exerciseRepository.getExercises(limit = 20)
            if (result.isSuccess) {
                _exercises.value = result.getOrDefault(emptyList())
            } else {
                _exercises.value = emptyList()
                _exerciseError.value = result.exceptionOrNull()?.localizedMessage
                    ?: "Unable to load exercises."
            }

            _isExerciseLoading.value = false
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val result = workoutRepository.getRecentWorkouts()
            if (result.isSuccess) {
                _history.value = result.getOrDefault(emptyList())
            }
        }
    }
}
