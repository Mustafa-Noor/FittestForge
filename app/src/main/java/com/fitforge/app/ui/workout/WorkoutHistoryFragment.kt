package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.adapters.WorkoutHistoryAdapter
import com.fitforge.app.databinding.FragmentWorkoutHistoryBinding

class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: WorkoutHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WorkoutHistoryAdapter { workout ->
            // Handle workout click if needed
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = adapter

        viewModel.history.observe(viewLifecycleOwner) { history ->
            adapter.submitList(history)
            if (history.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.tvEmpty.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
