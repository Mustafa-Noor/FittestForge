package com.fitforge.app

import android.app.Application

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.fitforge.app.utils.FitForgeNotificationManager
import com.fitforge.app.utils.FitForgeAudioManager
import com.fitforge.app.utils.NotificationScheduler
import com.google.firebase.FirebaseApp

class FitForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FitForgeAudioManager.init(this)
        createNotificationChannels()
        NotificationScheduler.scheduleDailyReminder(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel("ROAST", "Motivation Notifications", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Personality-mode messages when you skip" },
                NotificationChannel(FitForgeNotificationManager.CHANNEL_REMINDER, "Daily Reminder", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Gentle daily nudge" },
                NotificationChannel(FitForgeNotificationManager.CHANNEL_BADGE, "Achievement Alerts", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Badge unlock alerts" },
                NotificationChannel(FitForgeNotificationManager.CHANNEL_STREAK, "Streak Alerts", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Streak milestone alerts" },
            )
            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
