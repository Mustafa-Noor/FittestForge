package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.adapters.WorkoutHistoryAdapter
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.databinding.ActivityWorkoutHistoryBinding
import kotlinx.coroutines.launch

class WorkoutHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutHistoryBinding
    private val workoutRepository = WorkoutRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = WorkoutHistoryAdapter { }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        lifecycleScope.launch {
            workoutRepository.getRecentWorkouts(100).onSuccess { workouts ->
                if (workouts.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvHistory.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvHistory.visibility = View.VISIBLE
                    adapter.submitList(workouts)
                }
            }
        }
    }
}
