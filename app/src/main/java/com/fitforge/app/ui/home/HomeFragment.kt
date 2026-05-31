package com.fitforge.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fitforge.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = name
        }

        viewModel.momentumData.observe(viewLifecycleOwner) { momentum ->
            binding.momentumProgressBar.progressMax = 100f
            binding.momentumProgressBar.setProgressWithAnimation(momentum.value, 1000)
            binding.tvMomentumValue.text = "${momentum.value.toInt()}%"
        }

        binding.btnStartWorkout.setOnClickListener {
            // Intent to LogWorkoutActivity
            // startActivity(Intent(requireContext(), LogWorkoutActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}