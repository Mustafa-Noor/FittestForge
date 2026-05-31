package com.fitforge.app.utils

import com.fitforge.app.data.models.PersonalityMode

object PersonalityStrings {

    fun getMomentumHighMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("LET'S GOOO! You're absolutely BUILT DIFFERENT right now! 🔥", "PEAK FORM! Keep this energy and nobody can stop you!")
        PersonalityMode.DRILL -> listOf("Strong performance. Maintain discipline.", "Numbers look good. Do not get comfortable.")
        PersonalityMode.CHILL -> listOf("You're doing really well. Keep that good energy going.", "Things are clicking. Stay consistent, no pressure.")
        PersonalityMode.CHAOS -> listOf("Bro you're actually insane right now??", "The gym is starting to recognize you. It's scared.")
    }.random()

    fun getMomentumLowMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("Hey! We're getting back up RIGHT NOW. One workout changes everything!", "You've been HERE before and bounced back. TODAY is the day!")
        PersonalityMode.DRILL -> listOf("Momentum is down. You know what to do. Do it.", "No excuses. Get in there.")
        PersonalityMode.CHILL -> listOf("Hey, no worries. Just one session and you're back on track. Easy.", "Take it one day at a time. You've got this.")
        PersonalityMode.CHAOS -> listOf("Bro your couch is starting to recognize your body shape 💀", "The gym filed a missing persons report. Turn yourself in.")
    }.random()

    fun getPostWorkoutMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("YESSS!! WORKOUT LOGGED! YOU ARE UNSTOPPABLE!", "THAT JUST HAPPENED! You are BUILT DIFFERENT!")
        PersonalityMode.DRILL -> listOf("Logged. Good work. Rest. Repeat.", "Done. That is what discipline looks like.")
        PersonalityMode.CHILL -> listOf("Nicely done. Your body thanks you.", "That was solid. Rest up and you'll feel great tomorrow.")
        PersonalityMode.CHAOS -> listOf("Bro you actually showed up?? I'm shook.", "Different breed. Logged. Your couch is disappointed.")
    }.random()

    fun getMissedDayMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("HEY! TODAY IS YOUR DAY! LET'S BOUNCE BACK RIGHT NOW!", "You're ONE session away from momentum! DO THIS!")
        PersonalityMode.DRILL -> listOf("You missed. Don't miss again.", "That is unacceptable. Fix it today.")
        PersonalityMode.CHILL -> listOf("Hey, rest days happen. Come back when you're ready.", "No big deal. Just pick it back up today, nice and easy.")
        PersonalityMode.CHAOS -> listOf("Day 2 of ghosting the gym. It's moving on.", "Your protein shake expired waiting for you.")
    }.random()

    fun getRecoveryLogMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("Smart! Recovery is PART of the gains!", "Resting to come back STRONGER! That's elite athlete thinking!")
        PersonalityMode.DRILL -> listOf("Recovery logged. Return at full capacity.", "Acceptable. Come back ready.")
        PersonalityMode.CHILL -> listOf("Good call logging your rest day. Listening to your body is important.", "Rest well. You've earned it.")
        PersonalityMode.CHAOS -> listOf("Rest day logged. The gym respects it. Kinda.", "Strategic absence. We see you.")
    }.random()
}
