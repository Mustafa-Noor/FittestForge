package com.fitforge.app.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadUserData()

        binding.btnSaveProfile.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { user ->
                user?.let {
                    binding.etName.setText(it.displayName)
                    binding.etAge.setText(it.age.toString())
                    binding.etGender.setText(it.gender)
                    binding.etWeight.setText(it.weight.toString())
                    binding.etHeight.setText(it.height.toString())
                }
            }.onFailure {
                Toast.makeText(this@EditProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData() {
        val name = binding.etName.text.toString()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val gender = binding.etGender.text.toString()
        val weight = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f

        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { currentUser ->
                currentUser?.let {
                    val updatedUser = it.copy(
                        displayName = name,
                        age = age,
                        gender = gender,
                        weight = weight,
                        height = height
                    )
                    userRepository.updateUserProfile(updatedUser).onSuccess {
                        Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
}
