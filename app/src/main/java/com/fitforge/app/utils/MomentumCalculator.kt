package com.fitforge.app.utils

import com.fitforge.app.data.models.MomentumData

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

    fun getMomentumLabel(value: Float) = when {
        value >= 85 -> "Peak Momentum"
        value >= 65 -> "Strong Momentum"
        value >= 45 -> "Building Up"
        value >= 25 -> "Low Momentum"
        else        -> "Critical — Come Back"
    }
}