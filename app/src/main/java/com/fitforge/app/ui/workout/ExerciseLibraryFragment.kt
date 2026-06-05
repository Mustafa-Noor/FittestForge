package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fitforge.app.adapters.CategoryAdapter
import com.fitforge.app.data.local.ExerciseData
import com.fitforge.app.databinding.FragmentExerciseLibraryBinding

class ExerciseLibraryFragment : Fragment() {

    private var _binding: FragmentExerciseLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = CategoryAdapter(ExerciseData.categories) { category ->
            val intent = Intent(requireContext(), ExerciseCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category)
            startActivity(intent)
        }
        binding.rvCategories.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
