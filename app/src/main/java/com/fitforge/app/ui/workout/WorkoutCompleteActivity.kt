package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        // Set Personality text (Dummy read for now, usually from SharedPreferences)
        val mode = PersonalityMode.HYPE
        binding.tvPersonalityQuote.text = PersonalityStrings.getPostWorkoutMessage(mode)
        
        // Sequence Animations defined in FitForge.md
        binding.tvTitle.alpha = 0f
        binding.tvTitle.scaleX = 0.5f
        binding.tvTitle.scaleY = 0.5f
        binding.cardPersonality.alpha = 0f
        binding.btnShare.alpha = 0f
        binding.btnHome.alpha = 0f

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvTitle.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.cardPersonality.animate().alpha(1f).translationYBy(-20f).setDuration(300).start()
        }, 1100)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnShare.animate().alpha(1f).setDuration(300).start()
            binding.btnHome.animate().alpha(1f).setDuration(300).start()
        }, 1700)

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I just crushed a workout on FitForge! \uD83D\uDD25 #FitForge #BuiltForHumans")
            startActivity(Intent.createChooser(shareIntent, "Share Workout via"))
        }

        // playWorkoutComplete() audio should go here from FitForgeAudioManager
    }
}