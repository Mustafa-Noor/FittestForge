package com.fitforge.app.data.models

data class Exercise(
    val id: String = "",
    val name: String = "",
    val bodyPart: String = "",
    val target: String = "",
    val equipment: String = "",
    val gifUrl: String = "",
    val instructions: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList()
)
