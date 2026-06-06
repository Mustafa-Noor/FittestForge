package com.fitforge.app.ui.food

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.models.FoodLog
import com.fitforge.app.data.repository.FoodRepository
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivityFoodIntakeDetailBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FoodIntakeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodIntakeDetailBinding
    private val foodRepository = FoodRepository()
    private val userRepository = UserRepository()
    private var maintenanceCalories = 2000
    private var userGoal = ""
    private var dateString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodIntakeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        dateString = intent.getStringExtra("date") ?: LocalDate.now().toString()
        val displayDate = try {
            LocalDate.parse(dateString).format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
        } catch (e: Exception) { dateString }
        binding.tvDateHeader.text = displayDate

        loadUserAndPrefillData()
        setupCalorieWatchers()

        binding.btnSaveFood.setOnClickListener {
            saveFoodLog()
        }
    }

    private fun loadUserAndPrefillData() {
        lifecycleScope.launch {
            val user = userRepository.getUserProfile().getOrNull()
            if (user != null) {
                userGoal = user.fitnessGoal
                val w = user.weight
                val h = user.height
                val a = user.age
                if (w > 0 && h > 0 && a > 0) {
                    val bmr = if (user.gender == "Male") {
                        10 * w + 6.25 * h - 5 * a + 5
                    } else {
                        10 * w + 6.25 * h - 5 * a - 161
                    }
                    maintenanceCalories = (bmr * 1.375).toInt()
                }
                binding.tvMaintenanceInfo.text = "Maintenance: $maintenanceCalories kcal/day | Goal: ${userGoal.ifEmpty { "Stay active" }}"
            }

            // Pre-fill with existing log if any
            val existing = foodRepository.getFoodLogForDate(dateString).getOrNull()
            if (existing != null) {
                binding.etBreakfast.setText(if (existing.breakfast > 0) existing.breakfast.toString() else "")
                binding.etLunch.setText(if (existing.lunch > 0) existing.lunch.toString() else "")
                binding.etDinner.setText(if (existing.dinner > 0) existing.dinner.toString() else "")
                binding.etSnacks.setText(if (existing.snacks > 0) existing.snacks.toString() else "")
                updateTotal()
            }
        }
    }

    private fun setupCalorieWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateTotal() }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etBreakfast.addTextChangedListener(watcher)
        binding.etLunch.addTextChangedListener(watcher)
        binding.etDinner.addTextChangedListener(watcher)
        binding.etSnacks.addTextChangedListener(watcher)
    }

    private fun updateTotal() {
        val b = binding.etBreakfast.text.toString().toIntOrNull() ?: 0
        val l = binding.etLunch.text.toString().toIntOrNull() ?: 0
        val d = binding.etDinner.text.toString().toIntOrNull() ?: 0
        val s = binding.etSnacks.text.toString().toIntOrNull() ?: 0
        val total = b + l + d + s
        binding.tvTotalCalories.text = "$total kcal"
    }

    private fun saveFoodLog() {
        val b = binding.etBreakfast.text.toString().toIntOrNull() ?: 0
        val l = binding.etLunch.text.toString().toIntOrNull() ?: 0
        val d = binding.etDinner.text.toString().toIntOrNull() ?: 0
        val s = binding.etSnacks.text.toString().toIntOrNull() ?: 0
        val total = b + l + d + s

        if (total == 0) {
            Toast.makeText(this, "Please enter at least one calorie value!", Toast.LENGTH_SHORT).show()
            return
        }

        val log = FoodLog(
            dateString = dateString,
            breakfast = b,
            lunch = l,
            dinner = d,
            snacks = s,
            totalCalories = total
        )

        lifecycleScope.launch {
            val result = foodRepository.saveFoodLog(log)
            if (result.isSuccess) {
                val intent = Intent(this@FoodIntakeDetailActivity, FoodResultActivity::class.java).apply {
                    putExtra("total_calories", total)
                    putExtra("maintenance_calories", maintenanceCalories)
                    putExtra("user_goal", userGoal)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@FoodIntakeDetailActivity, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
