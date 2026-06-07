package com.fitforge.app.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fitforge.app.workers.FitNotificationWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val DAILY_REMINDER_WORK = "fitforge_daily_reminder"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"

    fun scheduleDailyReminder(context: Context) {
        if (!areNotificationsEnabled(context)) return

        val request = PeriodicWorkRequestBuilder<FitNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNextReminder(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled)
            .apply()

        if (enabled) {
            scheduleDailyReminder(context)
        } else {
            cancelDailyReminder(context)
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getBoolean(PREF_NOTIFICATIONS_ENABLED, true)
    }

    private fun delayUntilNextReminder(): Long {
        val now = LocalDateTime.now()
        var next = now.with(LocalTime.of(20, 0))
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return Duration.between(now, next).toMillis()
    }
}
