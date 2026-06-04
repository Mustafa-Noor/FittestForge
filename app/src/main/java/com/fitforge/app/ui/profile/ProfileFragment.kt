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
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}