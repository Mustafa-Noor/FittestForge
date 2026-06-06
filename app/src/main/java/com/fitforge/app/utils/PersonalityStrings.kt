package com.fitforge.app.utils

import com.fitforge.app.data.models.PersonalityMode

object PersonalityStrings {

    fun getMomentumHighMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("LET'S GOOO! You're absolutely BUILT DIFFERENT right now! 🔥", "PEAK FORM! Keep this energy and nobody can stop you!", "You are on FIRE! Every session makes you stronger! 💪", "This streak is REAL and so is your dedication! Keep PUSHING!")
        PersonalityMode.DRILL -> listOf("Strong performance. Maintain discipline.", "Numbers look good. Do not get comfortable.", "Consistency noted. No excuses now.", "Keep the pace. Results require persistence.")
        PersonalityMode.CHILL -> listOf("You're doing really well. Keep that good energy going.", "Things are clicking. Stay consistent, no pressure.", "Your effort is showing. Keep flowing.", "Solid momentum. Trust the process.")
        PersonalityMode.CHAOS -> listOf("Bro you're actually insane right now??", "The gym is starting to recognize you. It's scared.", "You're built different and the data agrees 💀", "Did you just... actually stay consistent? Wild.")
    }.random()

    fun getMomentumLowMessage(mode: PersonalityMode) = when(mode) {
        PersonalityMode.HYPE  -> listOf("Hey! We're getting back up RIGHT NOW. One workout changes everything!", "You've been HERE before and bounced back. TODAY is the day!", "Every champion has a rough patch. This is YOURS to beat!", "The comeback starts NOW! Log that workout!")
        PersonalityMode.DRILL -> listOf("Momentum is down. You know what to do. Do it.", "No excuses. Get in there.", "This is not acceptable. You have the capacity. Use it.", "Drop the distractions. Train.")
        PersonalityMode.CHILL -> listOf("Hey, no worries. Just one session and you're back on track. Easy.", "Take it one day at a time. You've got this.", "Even a short workout counts. Start somewhere.", "No pressure — just show up when you're ready.")
        PersonalityMode.CHAOS -> listOf("Bro your couch is starting to recognize your body shape 💀", "The gym filed a missing persons report. Turn yourself in.", "Your protein powder expired waiting for you.", "Your fitness tracker is questioning its own purpose rn.")
    }.random()

    fun getStreakMessage(mode: PersonalityMode, streak: Int, totalWorkouts: Int) = when(mode) {
        PersonalityMode.HYPE  -> listOf(
            "🔥 $streak day streak! You're building something LEGENDARY right now!",
            "Day $streak and STILL going! $totalWorkouts workouts total — you are CONSISTENCY personified!",
            "The streak is REAL, the gains are REAL! $streak days strong! 💪"
        )
        PersonalityMode.DRILL -> listOf(
            "$streak day streak. Expected. Continue.",
            "Streak: $streak. Workouts: $totalWorkouts. Keep the numbers rising.",
            "$streak consecutive days. Acceptable. Now add more."
        )
        PersonalityMode.CHILL -> listOf(
            "$streak days in a row — that's a beautiful habit you're building.",
            "Look at that — $streak days! You're proving that slow and steady really works.",
            "$totalWorkouts workouts and counting. You're creating a lifestyle, not just a routine."
        )
        PersonalityMode.CHAOS -> listOf(
            "$streak day streak?? Who ARE you right now??",
            "Bro $totalWorkouts workouts logged. Your future self is sending thank you notes.",
            "$streak days consistent. The gym has adopted you at this point."
        )
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
