package com.fitforge.app.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fitforge.app.R
import com.fitforge.app.ui.splash.SplashActivity
import com.fitforge.app.data.repository.UserRepository
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FitNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val messages = listOf(
        "Your streak is in danger! Don't let your gains disappear.",
        "Your couch misses you, but your muscles miss you more. Time to lift!",
        "Even 10 minutes counts. Keep the momentum going!",
        "Did you forget about us? Log a workout now to save your streak."
    )

    override suspend fun doWork(): Result {
        return try {
            val repo = UserRepository()
            val userOpt = repo.getUserProfile()
            
            // Just assume if we successfully wake up, we should notify
            // Duolingo style: "These reminders don't seem to be working. We'll stop sending them for now."
            val streak = userOpt.getOrNull()?.currentStreak ?: 0
            val message = if (streak > 0) {
                "You have a $streak day streak! Don't lose it now 🔥"
            } else {
                messages[Random.nextInt(messages.size)]
            }

            showNotification(message)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(message: String) {
        val channelId = "fitforge_daily_reminders"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_workout)
            .setContentTitle("FitForge")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
