package com.fitforge.app.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val userRepository = UserRepository()
    private var currentPersonality = "hype"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadCurrentPersonality()
        setupClickListeners()
    }

    private fun loadCurrentPersonality() {
        lifecycleScope.launch {
            userRepository.getUserProfile().getOrNull()?.let { user ->
                currentPersonality = user.personalityMode
                binding.tvCurrentPersonality.text = currentPersonality.replaceFirstChar { it.uppercase() }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Password Reset Sent")
                            .setMessage("A password reset link has been sent to:\n$email\n\nCheck your inbox and follow the instructions.")
                            .setPositiveButton("Got it", null)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "No email associated with this account.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCoachPersonality.setOnClickListener {
            val modes = arrayOf("🔥 Hype — Energetic & Motivational", "⚔️ Drill — Strict & No-Nonsense", "🌊 Chill — Calm & Supportive", "💀 Chaos — Funny & Unhinged")
            val modeKeys = arrayOf("hype", "drill", "chill", "chaos")
            val currentIndex = modeKeys.indexOf(currentPersonality).takeIf { it >= 0 } ?: 0

            MaterialAlertDialogBuilder(this)
                .setTitle("Choose Coach Personality")
                .setSingleChoiceItems(modes, currentIndex) { dialog, which ->
                    val selected = modeKeys[which]
                    currentPersonality = selected
                    binding.tvCurrentPersonality.text = selected.replaceFirstChar { it.uppercase() }
                    lifecycleScope.launch {
                        userRepository.updatePersonalityMode(selected)
                    }
                    Toast.makeText(this, "Coach personality updated!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}