package com.fitforge.app.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivitySettingsBinding
import com.fitforge.app.utils.NotificationScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val userRepository = UserRepository()
    private var currentPersonality = "hype"

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 501
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadCurrentPersonality()
        loadNotificationPreference()
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
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !hasNotificationPermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
                return@setOnCheckedChangeListener
            }

            NotificationScheduler.setNotificationsEnabled(this, isChecked)
            Toast.makeText(
                this,
                if (isChecked) "Daily reminders enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

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

    private fun loadNotificationPreference() {
        binding.switchNotifications.isChecked = NotificationScheduler.areNotificationsEnabled(this)
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST) return

        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        NotificationScheduler.setNotificationsEnabled(this, granted)
        binding.switchNotifications.isChecked = granted
        Toast.makeText(
            this,
            if (granted) "Daily reminders enabled" else "Notification permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }
}
