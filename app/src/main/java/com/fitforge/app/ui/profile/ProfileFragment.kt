package com.fitforge.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.fitforge.app.adapters.BadgeAdapter
import com.fitforge.app.databinding.FragmentProfileBinding
import com.fitforge.app.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var badgeAdapter: BadgeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBadgeRecyclerView()
        setupPersonalitySelection()
        observeViewModel()

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }

        viewModel.loadProfileData()
    }

    private fun setupBadgeRecyclerView() {
        badgeAdapter = BadgeAdapter { badge ->
            val message = if (badge.isUnlocked) {
                badge.description
            } else {
                "Locked: ${badge.unlockConditionText}"
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(badge.name)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show()
        }
        binding.rvBadges.layoutManager = GridLayoutManager(context, 3)
        binding.rvBadges.adapter = badgeAdapter
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvName.text = user.displayName
            binding.tvEmail.text = user.email
            highlightSelectedPersonality(user.personalityMode)
        }

        viewModel.badges.observe(viewLifecycleOwner) { badges ->
            badgeAdapter.submitList(badges)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupPersonalitySelection() {
        binding.cardHype.setOnClickListener { viewModel.updatePersonalityMode("hype") }
        binding.cardDrill.setOnClickListener { viewModel.updatePersonalityMode("drill") }
        binding.cardChill.setOnClickListener { viewModel.updatePersonalityMode("chill") }
        binding.cardChaos.setOnClickListener { viewModel.updatePersonalityMode("chaos") }
    }

    private fun highlightSelectedPersonality(mode: String) {
        val cards = listOf(binding.cardHype, binding.cardDrill, binding.cardChill, binding.cardChaos)
        cards.forEach {
            it.strokeWidth = 0
            it.setCardBackgroundColor(resources.getColor(com.fitforge.app.R.color.bg_secondary, null))
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
