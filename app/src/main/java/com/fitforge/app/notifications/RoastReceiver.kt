package com.fitforge.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fitforge.app.utils.FitForgeNotificationManager

class RoastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val message = intent?.getStringExtra(EXTRA_MESSAGE)
            ?: "Your momentum is waiting. Log one clean session today."

        FitForgeNotificationManager.showReminder(
            context = context,
            title = "FitForge Motivation",
            message = message
        )
    }

    companion object {
        const val EXTRA_MESSAGE = "extra_message"
    }
}
