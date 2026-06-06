package com.fitforge.app.data.models

import com.google.firebase.Timestamp

data class FoodLog(
    val id: String = "",
    val dateString: String = "",        // "yyyy-MM-dd"
    val breakfast: Int = 0,
    val lunch: Int = 0,
    val dinner: Int = 0,
    val snacks: Int = 0,
    val totalCalories: Int = 0,
    val timestamp: Timestamp? = null
)
