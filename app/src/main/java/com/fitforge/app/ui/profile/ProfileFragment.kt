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
        setupPersonalitySelection()
        setupImagePicker()

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

    private fun setupPersonalitySelection() {
        binding.cardHype.setOnClickListener { updatePersonality("hype") }
        binding.cardDrill.setOnClickListener { updatePersonality("drill") }
        binding.cardChill.setOnClickListener { updatePersonality("chill") }
        binding.cardChaos.setOnClickListener { updatePersonality("chaos") }
    }

    private fun updatePersonality(mode: String) {
        highlightSelectedPersonality(mode)
        lifecycleScope.launch {
            userRepository.updatePersonalityMode(mode)
        }
    }

    private fun highlightSelectedPersonality(mode: String) {
        // Reset all cards
        val cards = listOf(binding.cardHype, binding.cardDrill, binding.cardChill, binding.cardChaos)
        cards.forEach {
            it.strokeWidth = 0
            it.setCardBackgroundColor(resources.getColor(com.fitforge.app.R.color.bg_secondary, null))
        }

        // Highlight selected
        when (mode) {
            "hype" -> {
                binding.cardHype.strokeWidth = 4
                binding.cardHype.setCardBackgroundColor(resources.getColor(com.fitforge.app.R.color.primary_light, null))
            }
            "drill" -> {
                binding.cardDrill.strokeWidth = 4
                binding.cardDrill.setCardBackgroundColor(resources.getColor(com.fitforge.app.R.color.bg_tertiary, null))
            }
            "chill" -> {
                binding.cardChill.strokeWidth = 4
                binding.cardChill.setCardBackgroundColor(resources.getColor(com.fitforge.app.R.color.success_light, null))
            }
            "chaos" -> {
                binding.cardChaos.strokeWidth = 4
                binding.cardChaos.setCardBackgroundColor(resources.getColor(com.fitforge.app.R.color.accent_light, null))
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { user ->
                user?.let {
                    binding.tvName.text = it.displayName
                    binding.tvEmail.text = it.email
                    highlightSelectedPersonality(it.personalityMode)
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

                    // Setup Badges
                    val adapter = com.fitforge.app.adapters.BadgeAdapter {}
                    binding.rvBadges.adapter = adapter
                    val badgesList = buildBadges(it.badges)
                    adapter.submitList(badgesList)
                }
            }
        }
    }

    private fun buildBadges(unlockedMap: Map<String, Boolean>): List<com.fitforge.app.data.models.Badge> {
        return listOf(
            com.fitforge.app.data.models.Badge("first_workout", "First Workout", "FW", "Completed your first workout", unlockConditionText = "Log any workout"),
            com.fitforge.app.data.models.Badge("workouts_5", "High Five", "5", "Completed 5 workouts", unlockConditionText = "Log 5 workouts"),
            com.fitforge.app.data.models.Badge("workouts_10", "Ten Strong", "10", "Completed 10 workouts", unlockConditionText = "Log 10 workouts"),
            com.fitforge.app.data.models.Badge("workouts_30", "Iron Habit", "30", "Completed 30 workouts", unlockConditionText = "Log 30 workouts"),
            com.fitforge.app.data.models.Badge("streak_3", "Three-Day Spark", "3", "Built a 3-day streak", unlockConditionText = "Work out 3 days in a row"),
            com.fitforge.app.data.models.Badge("streak_7", "Weekly Warrior", "7", "Built a 7-day streak", unlockConditionText = "Work out 7 days in a row"),
            com.fitforge.app.data.models.Badge("streak_14", "Fortnight Fire", "14", "Built a 14-day streak", unlockConditionText = "Work out 14 days in a row"),
            com.fitforge.app.data.models.Badge("streak_30", "Monthly Machine", "30", "Built a 30-day streak", unlockConditionText = "Work out 30 days in a row"),
            com.fitforge.app.data.models.Badge("momentum_peak", "Peak Momentum", "PK", "Reached 85 momentum", unlockConditionText = "Raise momentum to 85"),
            com.fitforge.app.data.models.Badge("leg_day_respect", "Leg Day Respect", "LG", "Logged legs twice in a row", unlockConditionText = "Log two leg workouts in a row"),
            com.fitforge.app.data.models.Badge("early_bird", "Early Bird", "AM", "Logged a workout before 8 AM", unlockConditionText = "Finish a workout before 8 AM"),
            com.fitforge.app.data.models.Badge("recovery_smart", "Recovery Smart", "RS", "Logged a recovery day", unlockConditionText = "Use Life Happened once")
        ).map { badge ->
            badge.copy(isUnlocked = unlockedMap[badge.id] == true)
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