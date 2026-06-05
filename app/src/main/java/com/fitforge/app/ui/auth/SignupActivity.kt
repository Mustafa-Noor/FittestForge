package com.fitforge.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.models.User
import com.fitforge.app.data.repository.AuthRepository
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import android.content.Intent
import com.fitforge.app.ui.onboarding.OnboardingActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSignup.isEnabled = false
            binding.btnSignup.text = "Creating account..."

            lifecycleScope.launch {
                val result = authRepository.signUp(email, password)
                if (result.isSuccess) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val user = User(
                            uid = uid,
                            displayName = name,
                            email = email,
                            createdAt = Timestamp.now(),
                            lastActiveAt = Timestamp.now(),
                            badges = defaultBadgeMap()
                        )
                        val profileResult = userRepository.createUserProfile(user)
                        if (profileResult.isFailure) {
                            binding.btnSignup.isEnabled = true
                            binding.btnSignup.text = "SIGN UP"
                            Toast.makeText(
                                this@SignupActivity,
                                profileResult.exceptionOrNull()?.localizedMessage ?: "Failed to create profile",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }
                    }
                    Toast.makeText(this@SignupActivity, "Account created!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignupActivity, OnboardingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = "SIGN UP"
                    Toast.makeText(this@SignupActivity, result.exceptionOrNull()?.localizedMessage ?: "Signup failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun defaultBadgeMap(): Map<String, Boolean> {
        return listOf(
            "first_workout",
            "workouts_5",
            "workouts_10",
            "workouts_30",
            "streak_3",
            "streak_7",
            "streak_14",
            "streak_30",
            "momentum_peak",
            "leg_day_respect",
            "early_bird",
            "recovery_smart"
        ).associateWith { false }
    }
}
