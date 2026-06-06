package com.fitforge.app.ui.food

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fitforge.app.databinding.ActivityFoodResultBinding

class FoodResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalCalories = intent.getIntExtra("total_calories", 0)
        val maintenanceCalories = intent.getIntExtra("maintenance_calories", 2000)
        val userGoal = intent.getStringExtra("user_goal") ?: ""

        displayResult(totalCalories, maintenanceCalories, userGoal)

        binding.btnDone.setOnClickListener { finish() }
    }

    private fun displayResult(total: Int, maintenance: Int, goal: String) {
        val delta = total - maintenance
        val isGainGoal = goal.contains("gain", ignoreCase = true) || goal.contains("bulk", ignoreCase = true)
        val isLoseGoal = goal.contains("lose", ignoreCase = true) || goal.contains("cut", ignoreCase = true) || goal.contains("weight loss", ignoreCase = true)

        binding.tvStatIntake.text = "$total kcal"
        binding.tvStatMaintenance.text = "$maintenance kcal"

        val deltaText = if (delta >= 0) "+$delta kcal surplus" else "${delta} kcal deficit"
        binding.tvStatDelta.text = deltaText

        val (emoji, title, message) = when {
            isGainGoal && delta > 0 -> Triple(
                "💪",
                "Perfect for your goal!",
                "You're in a ${delta} kcal surplus — great for muscle growth! Keep fueling those gains with quality protein and carbs."
            )
            isGainGoal && delta == 0 -> Triple(
                "⚖️",
                "Right at maintenance",
                "You hit maintenance calories exactly. To gain weight, aim for a 300–500 kcal daily surplus. Add an extra meal or shake!"
            )
            isGainGoal && delta < 0 -> Triple(
                "⚠️",
                "Eat more!",
                "You're ${-delta} kcal below maintenance. For muscle gain, you need a surplus! Try adding a protein-rich meal or healthy snacks."
            )
            isLoseGoal && delta < 0 -> Triple(
                "🔥",
                "Fat loss mode activated!",
                "You're in a ${-delta} kcal deficit — perfect for fat loss! Stay consistent and pair it with your workouts for best results."
            )
            isLoseGoal && delta == 0 -> Triple(
                "⚖️",
                "At maintenance",
                "You're eating at maintenance. For weight loss, aim for a 300–500 kcal daily deficit. Cut a snack or increase workout intensity!"
            )
            isLoseGoal && delta > 0 -> Triple(
                "⚠️",
                "Watch the surplus",
                "You ate $delta kcal over maintenance. Your goal is to lose weight — try smaller portions or cutting high-calorie snacks tomorrow."
            )
            delta > 200 -> Triple(
                "🍽️",
                "Surplus day!",
                "You ate $delta kcal over maintenance. If you're not specifically trying to gain, balance it with an active workout day!"
            )
            delta < -200 -> Triple(
                "💧",
                "Light eating day",
                "You're ${-delta} kcal under maintenance. Make sure you're getting enough nutrients to fuel your workouts and recovery!"
            )
            else -> Triple(
                "✅",
                "Great balance!",
                "You're right around your maintenance calories — perfect for staying active and consistent with your fitness journey!"
            )
        }

        binding.tvResultEmoji.text = emoji
        binding.tvResultTitle.text = title
        binding.tvResultMessage.text = message

        // Color code the delta
        val deltaColor = when {
            isGainGoal && delta > 0 -> getColor(com.fitforge.app.R.color.success)
            isLoseGoal && delta < 0 -> getColor(com.fitforge.app.R.color.success)
            else -> getColor(com.fitforge.app.R.color.warning)
        }
        binding.tvStatDelta.setTextColor(deltaColor)
    }
}
