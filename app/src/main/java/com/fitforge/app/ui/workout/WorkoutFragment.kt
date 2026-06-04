package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fitforge.app.databinding.FragmentWorkoutBinding
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.adapter.FragmentStateAdapter

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WorkoutPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Library"
                1 -> "History"
                else -> null
            }
        }.attach()
    }

    private inner class WorkoutPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ExerciseLibraryFragment()
                else -> WorkoutHistoryFragment()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}