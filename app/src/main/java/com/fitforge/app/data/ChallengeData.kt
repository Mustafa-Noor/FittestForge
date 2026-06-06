package com.fitforge.app.data

import com.fitforge.app.data.models.Challenge
import com.fitforge.app.data.models.ChallengeDay

object ChallengeData {

    val challenges = listOf(

        // ==================== WEEKLY CHALLENGES ====================

        Challenge(
            id = "7_day_core_blast",
            title = "7-Day Core Blast",
            description = "Build a stronger core in just one week with daily targeted abs and stability exercises.",
            type = "weekly",
            durationDays = 7,
            bannerColor = "#1565C0",
            bannerImageUrl = "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?auto=format&fit=crop&w=800&q=80",
            days = listOf(
                ChallengeDay(1, focus = "Core Activation", description = "Start with planks, crunches and leg raises to wake up your core.",
                    exerciseIds = listOf("plank", "crunch", "leg_raise")),
                ChallengeDay(2, focus = "Obliques", description = "Hit those side muscles with bicycle crunches and side planks.",
                    exerciseIds = listOf("bicycle_crunch", "side_plank", "russian_twist")),
                ChallengeDay(3, isRestDay = true, focus = "Rest", description = "Active recovery — light walk or stretching only."),
                ChallengeDay(4, focus = "Lower Core", description = "Focus on lower abs with hanging leg raises and reverse crunches.",
                    exerciseIds = listOf("hanging_leg_raise", "reverse_crunch", "flutter_kicks")),
                ChallengeDay(5, focus = "Upper Core", description = "Target your upper abs with crunches and sit-ups.",
                    exerciseIds = listOf("crunch", "cable_crunch", "sit_up")),
                ChallengeDay(6, focus = "Full Core Burn", description = "Combine all muscle groups for an intense full core session.",
                    exerciseIds = listOf("plank", "bicycle_crunch", "leg_raise", "russian_twist")),
                ChallengeDay(7, isRestDay = true, focus = "Rest & Reflect", description = "You did it! Rest and celebrate your new core strength. 🏆")
            )
        ),

        Challenge(
            id = "7_day_beginner_boost",
            title = "7-Day Beginner Boost",
            description = "The perfect starter challenge. Build your foundation with accessible bodyweight exercises.",
            type = "weekly",
            durationDays = 7,
            bannerColor = "#2E7D32",
            bannerImageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
            days = listOf(
                ChallengeDay(1, focus = "Full Body Intro", description = "Light full body movements to ease in.",
                    exerciseIds = listOf("squat", "push_up", "plank")),
                ChallengeDay(2, focus = "Lower Body", description = "Legs and glutes focus.",
                    exerciseIds = listOf("squat", "lunge", "glute_bridge")),
                ChallengeDay(3, isRestDay = true, focus = "Rest", description = "Rest and hydrate."),
                ChallengeDay(4, focus = "Upper Body", description = "Build your pushing and pulling strength.",
                    exerciseIds = listOf("push_up", "dumbbell_row", "shoulder_press")),
                ChallengeDay(5, focus = "Core + Cardio", description = "Light cardio and core work.",
                    exerciseIds = listOf("crunch", "plank", "jumping_jacks")),
                ChallengeDay(6, focus = "Full Body", description = "Put it all together.",
                    exerciseIds = listOf("squat", "push_up", "crunch", "lunge")),
                ChallengeDay(7, isRestDay = true, focus = "Rest", description = "You've built your foundation! 🌱")
            )
        ),

        Challenge(
            id = "7_day_upper_power",
            title = "7-Day Upper Power",
            description = "7 days of focused upper body training — chest, back, shoulders, and arms.",
            type = "weekly",
            durationDays = 7,
            bannerColor = "#B71C1C",
            bannerImageUrl = "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=800&q=80",
            days = listOf(
                ChallengeDay(1, focus = "Chest Day", description = "Push it with bench press and push-up variations.",
                    exerciseIds = listOf("bench_press", "incline_press", "push_up")),
                ChallengeDay(2, focus = "Back Day", description = "Pull movements for a strong back.",
                    exerciseIds = listOf("pull_up", "dumbbell_row", "lat_pulldown")),
                ChallengeDay(3, focus = "Shoulders", description = "Sculpt those shoulders.",
                    exerciseIds = listOf("shoulder_press", "lateral_raise", "front_raise")),
                ChallengeDay(4, isRestDay = true, focus = "Rest", description = "Let those muscles recover."),
                ChallengeDay(5, focus = "Arms", description = "Biceps and triceps isolation.",
                    exerciseIds = listOf("bicep_curl", "tricep_dip", "hammer_curl")),
                ChallengeDay(6, focus = "Full Upper", description = "Combine all upper body movements.",
                    exerciseIds = listOf("bench_press", "pull_up", "shoulder_press", "bicep_curl")),
                ChallengeDay(7, isRestDay = true, focus = "Rest", description = "Upper body is growing. Rest up! 💪")
            )
        ),

        // ==================== MONTHLY CHALLENGES ====================

        Challenge(
            id = "30_day_full_body",
            title = "30-Day Full Body Forge",
            description = "A complete one-month program rotating through all muscle groups. Build strength, endurance, and consistency.",
            type = "monthly",
            durationDays = 30,
            bannerColor = "#4A148C",
            bannerImageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80",
            days = run {
                val rotation = listOf(
                    ChallengeDay(0, focus = "Push (Chest & Shoulders)", description = "Chest, shoulders and triceps compound movements.",
                        exerciseIds = listOf("bench_press", "shoulder_press", "push_up", "tricep_dip")),
                    ChallengeDay(0, focus = "Pull (Back & Biceps)", description = "Rows, pulls and bicep curls.",
                        exerciseIds = listOf("pull_up", "dumbbell_row", "lat_pulldown", "bicep_curl")),
                    ChallengeDay(0, focus = "Legs & Glutes", description = "Squat, lunge and deadlift variations.",
                        exerciseIds = listOf("squat", "lunge", "deadlift", "leg_press")),
                    ChallengeDay(0, focus = "Core & Abs", description = "Full core circuit.",
                        exerciseIds = listOf("plank", "crunch", "leg_raise", "russian_twist")),
                    ChallengeDay(0, isRestDay = true, focus = "Rest", description = "Active recovery — walk or stretch."),
                    ChallengeDay(0, focus = "Full Body HIIT", description = "High intensity full body circuit.",
                        exerciseIds = listOf("burpee", "squat", "push_up", "jumping_jacks")),
                    ChallengeDay(0, isRestDay = true, focus = "Rest", description = "Weekly rest day. You earned it!")
                )
                (1..30).map { day ->
                    val template = rotation[(day - 1) % 7]
                    template.copy(dayNumber = day)
                }
            }
        ),

        Challenge(
            id = "30_day_strength_build",
            title = "30-Day Strength Builder",
            description = "Progressive overload every week. Build raw strength across all major lifts over 30 days.",
            type = "monthly",
            durationDays = 30,
            bannerColor = "#E65100",
            bannerImageUrl = "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?auto=format&fit=crop&w=800&q=80",
            days = run {
                val rotation = listOf(
                    ChallengeDay(0, focus = "Squat Focus", description = "Heavy squat day with accessory leg work.",
                        exerciseIds = listOf("squat", "leg_press", "lunge", "calf_raise")),
                    ChallengeDay(0, focus = "Bench Press Focus", description = "Chest power day.",
                        exerciseIds = listOf("bench_press", "incline_press", "push_up", "tricep_dip")),
                    ChallengeDay(0, focus = "Deadlift Focus", description = "Posterior chain strength.",
                        exerciseIds = listOf("deadlift", "romanian_deadlift", "dumbbell_row")),
                    ChallengeDay(0, isRestDay = true, focus = "Rest", description = "Recovery day."),
                    ChallengeDay(0, focus = "Overhead Press Focus", description = "Shoulder and overhead strength.",
                        exerciseIds = listOf("shoulder_press", "lateral_raise", "shrug")),
                    ChallengeDay(0, focus = "Pull Day", description = "Back and bicep strength.",
                        exerciseIds = listOf("pull_up", "lat_pulldown", "bicep_curl", "face_pull")),
                    ChallengeDay(0, isRestDay = true, focus = "Rest", description = "Weekly deload. Rest hard.")
                )
                (1..30).map { day ->
                    val template = rotation[(day - 1) % 7]
                    template.copy(dayNumber = day)
                }
            }
        )
    )

    fun getById(id: String) = challenges.find { it.id == id }
    fun getWeekly() = challenges.filter { it.type == "weekly" }
    fun getMonthly() = challenges.filter { it.type == "monthly" }
}
