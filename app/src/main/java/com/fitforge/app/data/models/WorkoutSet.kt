package com.fitforge.app.data.models

data class WorkoutSet(
    val setNumber: Int = 0,
    val reps: Int = 0,
    val weightKg: Float = 0f,
    val completed: Boolean = false
)
