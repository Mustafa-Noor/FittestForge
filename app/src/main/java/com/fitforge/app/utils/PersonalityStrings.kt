package com.fitforge.app.utils

import com.fitforge.app.data.models.PersonalityMode

object PersonalityStrings {
    fun getPostWorkoutMessage(mode: PersonalityMode): String {
        return when (mode) {
            PersonalityMode.HYPE -> "LET'S GO! You absolutely crushed that! Another W in the books!"
            PersonalityMode.DRILL -> "Acceptable. But I want 10% more next time. Dismissed."
            PersonalityMode.CHILL -> "Awesome work today. Take it easy, hydrate, and recover well."
            PersonalityMode.CHAOS -> "You lifted the heavy things! The metal gods are appeased! MUAHAHA!"
        }
    }
    
    fun getMissedWorkoutMessage(mode: PersonalityMode): String {
        return when (mode) {
            PersonalityMode.HYPE -> "Hey, it's all good! Rest up and we bounce back stronger tomorrow!"
            PersonalityMode.DRILL -> "You missed a day. Do not make this a habit. We work tomorrow."
            PersonalityMode.CHILL -> "No worries at all. Your body needed rest. See you when you're ready."
            PersonalityMode.CHAOS -> "Oh no! The gravity is winning! Quick, lift something before it squishes us!"
        }
    }
}
