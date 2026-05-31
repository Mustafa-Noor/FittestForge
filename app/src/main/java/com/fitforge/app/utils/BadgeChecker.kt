package com.fitforge.app.utils

import com.fitforge.app.data.models.Badge
import com.fitforge.app.data.models.Workout

object BadgeChecker {
    fun checkNewBadges(workouts: List<Workout>, currentMomentum: Int): List<Badge> {
        val newBadges = mutableListOf<Badge>()
        
        // Example logic
        if (workouts.size == 1) {
            newBadges.add(Badge("first_workout", "First Blood", "Completed your very first workout!"))
        }
        
        if (currentMomentum >= 50) {
            newBadges.add(Badge("momentum_50", "Unstoppable", "Reached 50 Momentum!"))
        }

        return newBadges
    }
}
