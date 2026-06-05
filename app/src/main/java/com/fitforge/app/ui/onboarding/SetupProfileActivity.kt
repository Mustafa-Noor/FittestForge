package com.fitforge.app.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivitySetupProfileBinding
import kotlinx.coroutines.launch

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private val userRepository = UserRepository()
    
    private var step = 0
    private val totalSteps = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.etGender.setAdapter(adapter)

        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { user ->
                user?.let {
                    binding.tvWelcomeName.text = "Welcome, ${it.displayName.split(" ").first()}!"
                }
            }
        }

        binding.btnNext.setOnClickListener {
            handleNext()
        }
    }

    private fun handleNext() {
        when (step) {
            0 -> { 
                val age = binding.etAge.text.toString().toIntOrNull()
                if (age == null || age <= 0) {
                    Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                    return
                }
                nextStep()
            }
            1 -> { 
                val gender = binding.etGender.text.toString()
                if (gender != "Male" && gender != "Female") {
                    Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                    return
                }
                nextStep()
            }
            2 -> { 
                val weight = binding.etWeight.text.toString().toFloatOrNull()
                val height = binding.etHeight.text.toString().toFloatOrNull()

                if (weight == null || height == null || weight <= 0f || height <= 0f) {
                    Toast.makeText(this, "Please enter valid metrics", Toast.LENGTH_SHORT).show()
                    return
                }

                calculateAndShowBMI(weight, height)
                nextStep()
            }
            3 -> { 
                saveProfileAndFinish()
            }
        }
    }

    private fun nextStep() {
        step++
        binding.viewFlipper.displayedChild = step
        binding.progressBar.progress = ((step + 1) * 100) / totalSteps
        
        if (step == totalSteps - 1) {
            binding.btnNext.text = "Let's Go!"
        }
    }

    private fun calculateAndShowBMI(weight: Float, heightCm: Float) {
        val heightM = heightCm / 100f
        val bmi = weight / (heightM * heightM)
        
        binding.tvBmiResult.text = String.format("BMI: %.1f", bmi)
        
        val goal = when {
            bmi < 18.5 -> "Gain Muscle / Weight"
            bmi > 25.0 -> "Lose Weight / Fat"
            else -> "Maintain Healthy Lifestyle"
        }
        binding.tvGoalRecommendation.text = "Goal: $goal"
        binding.tvGoalRecommendation.tag = goal
    }

    private fun saveProfileAndFinish() {
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "Saving..."

        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val gender = binding.etGender.text.toString()
        val weight = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f
        val goal = binding.tvGoalRecommendation.tag?.toString() ?: ""

        val updates = mapOf(
            "age" to age,
            "gender" to gender,
            "weight" to weight,
            "height" to height,
            "fitnessGoal" to goal
        )

        lifecycleScope.launch {
            userRepository.updateProfileFields(updates).onSuccess {
                val intent = Intent(this@SetupProfileActivity, OnboardingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure {
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "Let's Go!"
                Toast.makeText(this@SetupProfileActivity, "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
