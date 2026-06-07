package com.fitforge.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.utils.FitForgeNotificationManager
import com.fitforge.app.utils.NotificationScheduler
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class FitNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            if (!NotificationScheduler.areNotificationsEnabled(context)) return Result.success()
            if (FirebaseAuth.getInstance().currentUser == null) return Result.success()

            val repo = UserRepository()
            val user = repo.getUserProfile().getOrNull()

            val name = user?.displayName?.split(" ")?.firstOrNull() ?: "Champion"
            val streak = user?.currentStreak ?: 0
            val totalWorkouts = user?.totalWorkouts ?: 0
            val lastWorkoutDate = user?.lastWorkoutDate ?: ""
            val goal = user?.fitnessGoal ?: ""

            val daysSinceWorkout = if (lastWorkoutDate.isNotEmpty()) {
                runCatching {
                    ChronoUnit.DAYS.between(LocalDate.parse(lastWorkoutDate), LocalDate.now()).toInt()
                }.getOrDefault(-1)
            } else {
                -1
            }

            val message = when {
                streak >= 7 -> "$name, you are on a $streak day streak. Keep the chain alive."
                streak > 0 && daysSinceWorkout == 0 -> "Great work today, $name. $streak days strong. See you tomorrow."
                streak > 0 -> "$name, protect your $streak day streak. A short session still counts."
                daysSinceWorkout > 3 -> "Hey $name, it has been $daysSinceWorkout days. Your $totalWorkouts workout record deserves another chapter."
                totalWorkouts == 0 -> "Welcome, $name. Your first workout is waiting, and even 10 minutes counts."
                goal.contains("lose", ignoreCase = true) -> "$name, every logged session moves your goal closer. Time to show up."
                goal.contains("gain", ignoreCase = true) -> "$name, muscles grow when you show up. Add another session to your $totalWorkouts total."
                else -> "$name, consistency beats intensity. Log a workout today to keep momentum alive."
            }

            FitForgeNotificationManager.showReminder(
                context = context,
                title = "FitForge Reminder",
                message = message
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
