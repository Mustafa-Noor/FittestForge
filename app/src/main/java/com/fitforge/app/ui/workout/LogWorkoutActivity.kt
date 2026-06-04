package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ActivityLogWorkoutBinding

class LogWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogWorkoutBinding
    private val exerciseList = mutableListOf<WorkoutExercise>()
    private lateinit var adapter: WorkoutExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnAddExercise.setOnClickListener {
            // In a real app, show exercise picker. For now, add a dummy.
            addDummyExercise()
        }

        binding.btnLifeHappened.setOnClickListener {
            RecoveryLogBottomSheet().show(supportFragmentManager, "recovery_log")
        }

        binding.btnFinishWorkout.setOnClickListener {
            startActivity(Intent(this, WorkoutCompleteActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutExerciseAdapter(exerciseList, 
            onAddSet = { pos ->
                val sets = exerciseList[pos].sets.toMutableList()
                sets.add(WorkoutSet())
                exerciseList[pos] = exerciseList[pos].copy(sets = sets)
                adapter.notifyItemChanged(pos)
            },
            onRemoveExercise = { pos ->
                exerciseList.removeAt(pos)
                adapter.notifyItemRemoved(pos)
            }
        )
        binding.rvWorkoutSets.layoutManager = LinearLayoutManager(this)
        binding.rvWorkoutSets.adapter = adapter
    }

    private fun addDummyExercise() {
        val dummy = WorkoutExercise(
            exerciseName = "Exercise ${exerciseList.size + 1}",
            sets = listOf(WorkoutSet())
        )
        exerciseList.add(dummy)
        adapter.notifyItemInserted(exerciseList.size - 1)
    }
}