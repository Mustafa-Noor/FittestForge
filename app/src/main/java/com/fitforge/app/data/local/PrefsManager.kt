package com.fitforge.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var uid: String?
        get() = prefs.getString("uid", null)
        set(value) = prefs.edit().putString("uid", value).apply()

    var personalityMode: String
        get() = prefs.getString("personality_mode", "hype") ?: "hype"
        set(value) = prefs.edit().putString("personality_mode", value).apply()

    var momentum: Float
        get() = prefs.getFloat("momentum", 50f)
        set(value) = prefs.edit().putFloat("momentum", value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)
        set(value) = prefs.edit().putBoolean("is_first_launch", value).apply()

    var onboardingDone: Boolean
        get() = prefs.getBoolean("onboarding_done", false)
        set(value) = prefs.edit().putBoolean("onboarding_done", value).apply()

    var roastEnabled: Boolean
        get() = prefs.getBoolean("roast_enabled", true)
        set(value) = prefs.edit().putBoolean("roast_enabled", value).apply()

    var reminderEnabled: Boolean
        get() = prefs.getBoolean("reminder_enabled", true)
        set(value) = prefs.edit().putBoolean("reminder_enabled", value).apply()

    var reminderHour: Int
        get() = prefs.getInt("reminder_hour", 19) // default 7 PM
        set(value) = prefs.edit().putInt("reminder_hour", value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
