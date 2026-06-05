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

        return try {
            val lastUpdate = LocalDate.parse(momentumUpdatedAt)
            val today = LocalDate.now()
            val daysMissed = ChronoUnit.DAYS.between(lastUpdate, today).toInt()

            if (daysMissed <= 0) return storedMomentum

            calculateNewMomentum(
                currentValue = storedMomentum,
                daysMissed = daysMissed,
                workoutCompleted = false,
                lifeHappened = false
            )
        } catch (e: Exception) {
            storedMomentum
        }
    }

    fun getMomentumLabel(value: Float) = when {
        value >= 85 -> "Peak Momentum"
        value >= 65 -> "Strong Momentum"
        value >= 45 -> "Building Up"
        value >= 25 -> "Low Momentum"
        else        -> "Critical — Come Back"
    }
}
