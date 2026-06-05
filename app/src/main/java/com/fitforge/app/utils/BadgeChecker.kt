package com.fitforge.app.utils

import com.fitforge.app.data.models.User
import com.fitforge.app.data.models.Workout
import java.util.Calendar

object BadgeChecker {

    fun checkAndUnlock(user: User, allWorkouts: List<Workout>): List<String> {
        val newlyUnlocked = mutableListOf<String>()
        val existingBadges = user.badges

        val nonRecoveryWorkouts = allWorkouts.filter { !it.isRecoveryDay && !it.deleted }
        val workoutCount = nonRecoveryWorkouts.size

        // 1. first_workout
        if (existingBadges["first_workout"] != true) {
            if (workoutCount >= 1) newlyUnlocked.add("first_workout")
        }

        // 2. workouts_5
        if (existingBadges["workouts_5"] != true) {
            if (workoutCount >= 5) newlyUnlocked.add("workouts_5")
        }

        // 3. workouts_10
        if (existingBadges["workouts_10"] != true) {
            if (workoutCount >= 10) newlyUnlocked.add("workouts_10")
        }

        // 4. workouts_30
        if (existingBadges["workouts_30"] != true) {
            if (workoutCount >= 30) newlyUnlocked.add("workouts_30")
        }

        // 5. streak_3
        if (existingBadges["streak_3"] != true) {
            if (user.currentStreak >= 3) newlyUnlocked.add("streak_3")
        }

        // 6. streak_7
        if (existingBadges["streak_7"] != true) {
            if (user.currentStreak >= 7) newlyUnlocked.add("streak_7")
        }

        // 7. streak_14
        if (existingBadges["streak_14"] != true) {
            if (user.currentStreak >= 14) newlyUnlocked.add("streak_14")
        }

        // 8. streak_30
        if (existingBadges["streak_30"] != true) {
            if (user.currentStreak >= 30) newlyUnlocked.add("streak_30")
        }

        // 9. momentum_peak
        if (existingBadges["momentum_peak"] != true) {
            if (user.momentum >= 85f) newlyUnlocked.add("momentum_peak")
        }

        // 10. leg_day_respect
        if (existingBadges["leg_day_respect"] != true) {
            if (nonRecoveryWorkouts.size >= 2) {
                val lastTwo = nonRecoveryWorkouts.take(2)
                val bothHaveLegs = lastTwo.all { workout ->
                    workout.exercises.any { it.muscleGroup.lowercase() == "legs" }
                }
                if (bothHaveLegs) newlyUnlocked.add("leg_day_respect")
            }
        }

        // 11. early_bird
        if (existingBadges["early_bird"] != true) {
            nonRecoveryWorkouts.firstOrNull()?.date?.let { timestamp ->
                val cal = Calendar.getInstance()
                cal.time = timestamp.toDate()
                if (cal.get(Calendar.HOUR_OF_DAY) < 8) {
                    newlyUnlocked.add("early_bird")
                }
            }
        }

        // 12. recovery_smart
        if (existingBadges["recovery_smart"] != true) {
            if (allWorkouts.any { it.isRecoveryDay && !it.deleted }) {
                newlyUnlocked.add("recovery_smart")
            }
        }

        return newlyUnlocked
    }
}
