package com.fitforge.app.utils

object MomentumCalculator {
    fun calculateNewMomentum(currentScore: Int, daysMissed: Int, workoutCompleted: Boolean, lifeHappened: Boolean): Int {
        if (workoutCompleted) {
            return (currentScore + 12).coerceAtMost(100)
        }
        
        if (lifeHappened) {
            return currentScore // Shield: No loss
        }
        
        // Decay system
        val penalty = when {
            daysMissed == 1 -> 5
            daysMissed == 2 -> 10
            daysMissed >= 3 -> 15
            else -> 0
        }
        return (currentScore - penalty).coerceAtLeast(0)
    }
}
