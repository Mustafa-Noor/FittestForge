package com.fitforge.app.utils

import com.fitforge.app.data.models.User
import com.fitforge.app.data.models.Workout
import java.util.Calendar

object BadgeChecker {
    fun checkAndUnlock(user: User, allWorkouts: List<Workout>): List<String> {
        val newBadgeIds = mutableListOf<String>()
        val existingBadges = user.badges

        val badgeConditions = mapOf(
            "first_workout" to { allWorkouts.count { !it.isRecoveryDay } >= 1 },
            "workouts_5" to { allWorkouts.count { !it.isRecoveryDay } >= 5 },
            "workouts_10" to { allWorkouts.count { !it.isRecoveryDay } >= 10 },
            "workouts_30" to { allWorkouts.count { !it.isRecoveryDay } >= 30 },
            "streak_3" to { user.currentStreak >= 3 },
            "streak_7" to { user.currentStreak >= 7 },
            "streak_14" to { user.currentStreak >= 14 },
            "streak_30" to { user.currentStreak >= 30 },
            "momentum_peak" to { user.momentum >= 85f },
            "leg_day_respect" to {
                val nonRecovery = allWorkouts.filter { !it.isRecoveryDay }
                    .sortedByDescending { it.date?.toDate() }
                if (nonRecovery.size >= 2) {
                    val last2 = nonRecovery.take(2)
                    last2.all { workout ->
                        workout.exercises.any { it.muscleGroup.lowercase() == "legs" }
                    }
                } else false
            },
            "early_bird" to {
                val lastWorkout = allWorkouts.filter { !it.isRecoveryDay }
                    .maxByOrNull { it.date?.toDate()?.time ?: 0L }
                lastWorkout?.date?.let { timestamp ->
                    val cal = Calendar.getInstance()
                    cal.time = timestamp.toDate()
                    cal.get(Calendar.HOUR_OF_DAY) < 8
                } ?: false
            },
            "recovery_smart" to { allWorkouts.any { it.isRecoveryDay } }
        )

        for ((badgeId, condition) in badgeConditions) {
            if (existingBadges[badgeId] != true && condition()) {
                newBadgeIds.add(badgeId)
            }
        }

        return newBadgeIds
    }
}
