package com.fitforge.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupImagePicker()

        binding.btnSeeBadges.setOnClickListener {
            startActivity(Intent(requireContext(), com.fitforge.app.ui.profile.BadgeListActivity::class.java))
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), com.fitforge.app.ui.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }



    private fun loadUserData() {
        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { user ->
                user?.let {
                    binding.tvName.text = it.displayName
                    binding.tvEmail.text = it.email
                    val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    val localImage = prefs.getString("local_profile_image", null)
                    
                    if (localImage != null) {
                        com.bumptech.glide.Glide.with(this@ProfileFragment)
                            .load(android.net.Uri.parse(localImage))
                            .into(binding.ivProfile)
                    } else if (it.photoUrl.isNotEmpty()) {
                        com.bumptech.glide.Glide.with(this@ProfileFragment)
                            .load(it.photoUrl)
                            .into(binding.ivProfile)
                    }
                }
            }
        }
    }

    private fun setupImagePicker() {
        binding.ivProfile.setOnClickListener {
            com.github.dhaval2404.imagepicker.ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == android.app.Activity.RESULT_OK) {
                val fileUri = data?.data!!
                binding.ivProfile.setImageURI(fileUri)
                uploadImageToFirebase(fileUri)
            } else if (resultCode == com.github.dhaval2404.imagepicker.ImagePicker.RESULT_ERROR) {
                android.widget.Toast.makeText(requireContext(), com.github.dhaval2404.imagepicker.ImagePicker.getError(data), android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(requireContext(), "Task Cancelled", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

    private fun uploadImageToFirebase(fileUri: android.net.Uri) {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("local_profile_image", fileUri.toString()).apply()
        android.widget.Toast.makeText(requireContext(), "Profile image updated locally!", android.widget.Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            userRepository.updateProfileFields(mapOf("photoUrl" to fileUri.toString()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}