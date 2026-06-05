package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fitforge.app.MainActivity
import com.fitforge.app.data.models.PersonalityMode
import com.fitforge.app.databinding.ActivityWorkoutCompleteBinding
import com.fitforge.app.utils.PersonalityStrings

class WorkoutCompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newBadges = intent.getStringArrayListExtra("new_badges") ?: arrayListOf()
        val newMomentum = intent.getFloatExtra("new_momentum", 50f)
        val newStreak = intent.getIntExtra("new_streak", 0)
        val duration = intent.getIntExtra("workout_duration", 0)
        val totalSets = intent.getIntExtra("total_sets", 0)

        // Set UI values
        binding.tvDuration.text = duration.toString()
        binding.tvTotalSets.text = totalSets.toString()
        binding.tvStreak.text = newStreak.toString()
        binding.tvMomentumValue.text = "${newMomentum.toInt()}%"
        binding.momentumProgress.setProgressWithAnimation(newMomentum, 1500)

        // Show badges if any
        if (newBadges.isNotEmpty()) {
            binding.rvNewBadges.visibility = View.VISIBLE
            // TODO: Setup a small horizontal adapter for new badges if needed
            // For now, maybe just play a sound or show a toast for simplicity if adapter is not ready
        } else {
            binding.rvNewBadges.visibility = View.GONE
        }

        // Set Personality text (Dummy read for now, usually from SharedPreferences or User object)
        val mode = PersonalityMode.HYPE
        binding.tvPersonalityQuote.text = PersonalityStrings.getPostWorkoutMessage(mode)
        
        // Sequence Animations
        setupAnimations()

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I just crushed a ${duration} min workout on FitForge! My streak is now ${newStreak} days! \uD83D\uDD25 #FitForge #BuiltForHumans")
            startActivity(Intent.createChooser(shareIntent, "Share Workout via"))
        }
    }

    private fun setupAnimations() {
        binding.tvTitle.alpha = 0f
        binding.tvTitle.scaleX = 0.5f
        binding.tvTitle.scaleY = 0.5f
        binding.cardStats.alpha = 0f
        binding.cardPersonality.alpha = 0f
        binding.btnShare.alpha = 0f
        binding.btnHome.alpha = 0f

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvTitle.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.cardStats.animate().alpha(1f).translationYBy(-20f).setDuration(300).start()
        }, 800)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.cardPersonality.animate().alpha(1f).translationYBy(-20f).setDuration(300).start()
        }, 1100)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnShare.animate().alpha(1f).setDuration(300).start()
            binding.btnHome.animate().alpha(1f).setDuration(300).start()
        }, 1400)
    }
}
