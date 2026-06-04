package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.content.Intent
import com.fitforge.app.adapters.ExerciseAdapter
import com.fitforge.app.databinding.FragmentExerciseLibraryBinding
import com.fitforge.app.ui.workout.ExerciseDetailActivity

class ExerciseLibraryFragment : Fragment() {

    private var _binding: FragmentExerciseLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = ExerciseAdapter { exercise ->
            val intent = Intent(requireContext(), ExerciseDetailActivity::class.java)
            intent.putExtra("EXERCISE_ID", exercise.id)
            startActivity(intent)
        }
        binding.rvExercises.adapter = adapter

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            adapter.submitList(exercises)
            binding.progressBar.visibility = View.GONE
        }
        
        if (viewModel.exercises.value == null) {
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
