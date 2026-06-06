package com.fitforge.app.ui.profile

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivityEditProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val userRepository = UserRepository()

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!
                binding.ivProfileLarge.setImageURI(fileUri)
                uploadImageToFirebase(fileUri)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup Gender Dropdown
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.etGender.setAdapter(adapter)

        loadUserData()

        binding.btnSaveProfile.setOnClickListener {
            saveUserData()
        }

        binding.btnChangePhoto.setOnClickListener { openImagePicker() }
        binding.ivProfileLarge.setOnClickListener { openImagePicker() }
    }

    private fun openImagePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { user ->
                user?.let {
                    binding.etName.setText(it.displayName)
                    binding.etAge.setText(it.age.toString())
                    // For AutoCompleteTextView, setText(text, false) is needed to avoid dropdown popping up
                    binding.etGender.setText(it.gender, false)
                    binding.etWeight.setText(it.weight.toString())
                    binding.etHeight.setText(it.height.toString())

                    val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    val localImage = prefs.getString("local_profile_image", null)
                    
                    if (localImage != null) {
                        Glide.with(this@EditProfileActivity)
                            .load(android.net.Uri.parse(localImage))
                            .into(binding.ivProfileLarge)
                    } else if (it.photoUrl.isNotEmpty()) {
                        Glide.with(this@EditProfileActivity)
                            .load(it.photoUrl)
                            .into(binding.ivProfileLarge)
                    }
                }
            }.onFailure {
                Toast.makeText(this@EditProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("local_profile_image", fileUri.toString()).apply()
        Toast.makeText(this, "Profile image updated locally!", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            userRepository.updateProfileFields(mapOf("photoUrl" to fileUri.toString()))
        }
    }

    private fun saveUserData() {
        val name = binding.etName.text.toString()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val gender = binding.etGender.text.toString()
        val weight = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f

        val updates = mapOf(
            "displayName" to name,
            "age" to age,
            "gender" to gender,
            "weight" to weight,
            "height" to height
        )

        lifecycleScope.launch {
            userRepository.updateProfileFields(updates).onSuccess {
                Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(this@EditProfileActivity, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
