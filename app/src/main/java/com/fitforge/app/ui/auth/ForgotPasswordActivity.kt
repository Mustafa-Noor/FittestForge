package com.fitforge.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.AuthRepository
import com.fitforge.app.databinding.ActivityForgotPasswordBinding
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
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnReset.isEnabled = false
            binding.btnReset.text = "Sending..."

            lifecycleScope.launch {
                val result = authRepository.resetPassword(email)
                if (result.isSuccess) {
                    Toast.makeText(this@ForgotPasswordActivity, "Reset link sent!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnReset.isEnabled = true
                    binding.btnReset.text = "SEND RESET LINK"
                    Toast.makeText(this@ForgotPasswordActivity, result.exceptionOrNull()?.localizedMessage ?: "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
