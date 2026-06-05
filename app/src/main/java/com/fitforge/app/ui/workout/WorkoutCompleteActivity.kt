package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fitforge.app.databinding.ActivityWorkoutCompleteBinding
import kotlin.random.Random

class WorkoutCompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutCompleteBinding

    private val quotes = listOf(
        "\"The only bad workout is the one that didn't happen.\"",
        "\"Sweat is just fat crying.\"",
        "\"What seems impossible today will one day become your warm-up.\"",
        "\"Don't stop when you're tired. Stop when you're done.\"",
        "\"It never gets easier, you just get stronger.\"",
        "\"A one-hour workout is 4% of your day. No excuses.\""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newMomentum = intent.getFloatExtra("NEW_MOMENTUM", 0f)
        val newStreak = intent.getIntExtra("NEW_STREAK", 0)

        binding.tvNewMomentum.text = "${newMomentum.toInt()}%"
        binding.tvNewStreak.text = "🔥 $newStreak"
        
        binding.tvMotivationalQuote.text = quotes[Random.nextInt(quotes.size)]

        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, com.fitforge.app.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}