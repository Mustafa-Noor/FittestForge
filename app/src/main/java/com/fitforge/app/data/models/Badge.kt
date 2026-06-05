package com.fitforge.app.data.models

data class Badge(
    val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val description: String = "",
    val isUnlocked: Boolean = false,
    val unlockConditionText: String = ""
)
