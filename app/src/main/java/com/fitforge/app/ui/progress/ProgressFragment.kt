package com.fitforge.app.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fitforge.app.R
import com.fitforge.app.databinding.FragmentProgressBinding

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // MPAndroidChart logic goes here. Needs data from WorkoutRepository.
        binding.momentumLineChart.setNoDataText("Loading Momentum Data...")
        binding.dnaBarChart.setNoDataText("Loading DNA Data...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}