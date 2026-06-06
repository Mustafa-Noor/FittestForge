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
import java.time.LocalDate

class FitNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = UserRepository()
            val user = repo.getUserProfile().getOrNull()

            val name = user?.displayName?.split(" ")?.firstOrNull() ?: "Champion"
            val streak = user?.currentStreak ?: 0
            val totalWorkouts = user?.totalWorkouts ?: 0
            val lastWorkoutDate = user?.lastWorkoutDate ?: ""
            val goal = user?.fitnessGoal ?: ""

            val daysSinceWorkout = if (lastWorkoutDate.isNotEmpty()) {
                try {
                    val last = LocalDate.parse(lastWorkoutDate)
                    java.time.temporal.ChronoUnit.DAYS.between(last, LocalDate.now()).toInt()
                } catch (e: Exception) { -1 }
            } else -1

            val message = when {
                streak >= 7 -> "$name, you're on a $streak day streak! 🔥 You're an absolute machine. Keep it going!"
                streak > 0 && daysSinceWorkout == 0 -> "Great work today, $name! $streak days strong. See you tomorrow 💪"
                streak > 0 -> "$name, protect your $streak day streak! Don't let today be the day it breaks 🔥"
                daysSinceWorkout > 3 -> "Hey $name, it's been $daysSinceWorkout days. Your $totalWorkouts workout record deserves more chapters!"
                totalWorkouts == 0 -> "Welcome, $name! Your first workout is waiting. Start small — even 10 minutes counts 🏋️"
                goal.contains("lose", ignoreCase = true) -> "$name, every workout burns calories. Your goal is within reach — log a session now!"
                goal.contains("gain", ignoreCase = true) -> "$name, muscles grow when you show up. Time to add another session to your $totalWorkouts total!"
                else -> "$name, consistency beats intensity. Log a workout today to keep your momentum alive 💪"
            }

            showNotification(name, message)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(name: String, message: String) {
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
            .setContentTitle("Hey $name! 💪 FitForge")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
