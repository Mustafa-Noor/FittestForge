package com.fitforge.app.data.models

import com.google.firebase.Timestamp

data class Workout(
    var id: String = "",
    val date: Timestamp? = null,
    val dateString: String = "",
    val durationMinutes: Int = 0,
    val totalSets: Int = 0,
    val notes: String = "",
    val isRecoveryDay: Boolean = false,
    val recoveryReason: String? = null,
    val deleted: Boolean = false,
    val exercises: List<WorkoutExercise> = emptyList()
)

data class WorkoutExercise(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val sets: List<WorkoutSet> = emptyList()
)
