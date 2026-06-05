package com.fitforge.app.data.models

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val gender: String = "",
    val weight: Float = 0f,
    val height: Float = 0f,
    val age: Int = 0,
    val fitnessGoal: String = "",
    val personalityMode: String = "hype",
    val createdAt: Timestamp? = null,
    val lastActiveAt: Timestamp? = null,
    val momentum: Float = 50f,
    val momentumUpdatedAt: String = "",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalMinutes: Int = 0,
    val lastWorkoutDate: String = "",
    val lastMuscleGroup: String = "",
    val momentumUpdatedAt: String = "",
    val fcmToken: String = "",
    val badges: Map<String, Boolean> = emptyMap(),
    val badgeUnlockedAt: Map<String, Timestamp> = emptyMap()
)
