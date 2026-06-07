package com.fitforge.app.utils

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fitforge.app.R
import com.fitforge.app.ui.splash.SplashActivity

object FitForgeNotificationManager {
    const val CHANNEL_REMINDER = "REMINDER"
    const val CHANNEL_BADGE = "BADGE"
    const val CHANNEL_STREAK = "STREAK"

    private const val BADGE_NOTIFICATION_ID = 2101
    private const val STREAK_NOTIFICATION_ID = 2201

    fun showBadgeUnlocked(context: Context, badgeIds: List<String>) {
        if (badgeIds.isEmpty() || !canNotify(context)) return

        val badgeText = badgeIds.joinToString { it.toBadgeTitle() }
        val message = if (badgeIds.size == 1) {
            "You unlocked $badgeText. That shelf is looking better already."
        } else {
            "You unlocked ${badgeIds.size} badges: $badgeText."
        }

        showNotification(
            context = context,
            channelId = CHANNEL_BADGE,
            notificationId = BADGE_NOTIFICATION_ID,
            title = "New Badge Unlocked",
            message = message,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    fun showStreakMilestone(context: Context, streak: Int) {
        if (!canNotify(context) || streak !in setOf(3, 7, 14, 30, 50, 100)) return

        showNotification(
            context = context,
            channelId = CHANNEL_STREAK,
            notificationId = STREAK_NOTIFICATION_ID + streak,
            title = "$streak Day Streak",
            message = "That is $streak days of showing up. Protect the streak tomorrow.",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun showReminder(context: Context, title: String, message: String) {
        if (!canNotify(context)) return

        showNotification(
            context = context,
            channelId = CHANNEL_REMINDER,
            notificationId = 1001,
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun canNotify(context: Context): Boolean {
        if (!NotificationScheduler.areNotificationsEnabled(context)) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        priority: Int
    ) {
        runCatching {
            val intent = Intent(context, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_workout)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }
    }

    private fun String.toBadgeTitle(): String {
        return split("_")
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
    }
}
