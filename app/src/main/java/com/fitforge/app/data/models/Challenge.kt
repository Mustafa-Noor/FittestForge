package com.fitforge.app.data.models

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val type: String,           // "weekly" | "monthly"
    val durationDays: Int,
    val bannerColor: String,    // hex color for the banner background
    val bannerImageUrl: String,
    val days: List<ChallengeDay>
)

data class ChallengeDay(
    val dayNumber: Int,
    val isRestDay: Boolean = false,
    val exerciseIds: List<String> = emptyList(),
    val description: String = "",
    val focus: String = ""      // e.g. "Core", "Upper Body", "Rest"
)
