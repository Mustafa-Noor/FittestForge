package com.fitforge.app.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object MomentumCalculator {
    fun calculateNewMomentum(
        currentValue: Float,
        daysMissed: Int,
        workoutCompleted: Boolean,
        lifeHappened: Boolean
    ): Float {
        var newValue = currentValue

        if (workoutCompleted) {
            newValue += 12f
            if (newValue >= 85f) {
                newValue += 5f // Milestone bonus
            }
        } else if (lifeHappened) {
            newValue += 3f // Recovery day bonus
        } else {
            // Decay rules
            val penalty = when {
                daysMissed >= 7 -> 50f
                daysMissed >= 3 -> 30f
                daysMissed == 2 -> 20f
                daysMissed == 1 -> 12f
                else -> 0f
            }
            newValue -= penalty
        }

        return newValue.coerceIn(0f, 100f)
    }

    fun calculateDecayOnOpen(
        storedMomentum: Float,
        lastWorkoutDate: String,
        momentumUpdatedAt: String
    ): Float {
        if (momentumUpdatedAt.isEmpty()) return storedMomentum
        
        val lastUpdate = try {
            LocalDate.parse(momentumUpdatedAt)
        } catch (e: Exception) {
            return storedMomentum
        }
        
        val today = LocalDate.now()
        val daysSinceUpdate = ChronoUnit.DAYS.between(lastUpdate, today).toInt()
        
        if (daysSinceUpdate <= 0) return storedMomentum

        // Calculate days missed since last actual workout
        val lastWorkout = try {
            if (lastWorkoutDate.isNotEmpty()) LocalDate.parse(lastWorkoutDate) else lastUpdate
        } catch (e: Exception) {
            lastUpdate
        }
        val totalDaysMissed = ChronoUnit.DAYS.between(lastWorkout, today).toInt()

        return calculateNewMomentum(
            currentValue = storedMomentum,
            daysMissed = totalDaysMissed,
            workoutCompleted = false,
            lifeHappened = false
        )
    }

    fun getMomentumLabel(value: Float) = when {
        value >= 85 -> "Peak Momentum"
        value >= 65 -> "Strong Momentum"
        value >= 45 -> "Building Up"
        value >= 25 -> "Low Momentum"
        else        -> "Critical — Come Back"
    }
}
