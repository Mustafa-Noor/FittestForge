package com.fitforge.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.AuthRepository
import com.fitforge.app.databinding.ActivityForgotPasswordBinding
import com.fitforge.app.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            binding.tilEmail.error = null

            if (!isValidEmail(email)) {
                binding.tilEmail.error = "Enter a valid email address"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            setLoading(true)

            lifecycleScope.launch {
                val result = authRepository.resetPassword(email)
                setLoading(false)

                if (result.isSuccess) {
                    showResetSentDialog(email)
                } else {
                    showToast(result.exceptionOrNull()?.localizedMessage ?: "Could not send reset link")
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.etEmail.isEnabled = !isLoading
        binding.btnReset.isEnabled = !isLoading
        binding.btnReset.text = if (isLoading) "Sending..." else "SEND RESET LINK"
    }

    private fun showResetSentDialog(email: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Link Sent")
            .setMessage("If an account exists for $email, Firebase will email a password reset link. Check your inbox and spam folder.")
            .setPositiveButton("Back to sign in") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }
}
