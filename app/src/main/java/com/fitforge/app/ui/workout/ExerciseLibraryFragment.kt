package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.content.Intent
import android.widget.Toast
import com.fitforge.app.adapters.ExerciseAdapter
import com.fitforge.app.databinding.FragmentExerciseLibraryBinding

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
            val intent = Intent(requireContext(), LogWorkoutActivity::class.java)
            intent.putExtra("EXERCISE_ID", exercise.id)
            intent.putExtra("EXERCISE_NAME", exercise.name)
            intent.putExtra("MUSCLE_GROUP", exercise.bodyPart)
            startActivity(intent)
        }
        binding.rvExercises.adapter = adapter

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            adapter.submitList(exercises)
        }

        viewModel.isExerciseLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvExercises.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.exerciseError.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
