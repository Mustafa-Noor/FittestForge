package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ActivityLogWorkoutBinding

import androidx.activity.viewModels
import com.fitforge.app.ui.workout.LogWorkoutViewModel

class LogWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogWorkoutBinding
    private val viewModel: LogWorkoutViewModel by viewModels()
    private val exerciseList = mutableListOf<WorkoutExercise>()
    private lateinit var adapter: WorkoutExerciseAdapter
    private var startTimeMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTimeMillis = System.currentTimeMillis()
        setupRecyclerView()
        setupObservers()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnAddExercise.setOnClickListener {
            // For now, keep dummy but in a real app this would open ExerciseLibrary
            addDummyExercise()
        }

        binding.btnLifeHappened.setOnClickListener {
            RecoveryLogBottomSheet().show(supportFragmentManager, "recovery_log")
        }

        binding.btnFinishWorkout.setOnClickListener {
            val notes = binding.etNotes.text.toString()
            if (exerciseList.isEmpty()) {
                Toast.makeText(this, "Add at least one exercise", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnFinishWorkout.isEnabled = false
            viewModel.saveWorkout(exerciseList, notes, startTimeMillis)
        }
    }

    private fun setupObservers() {
        viewModel.saveResult.observe(this) { result ->
            binding.btnFinishWorkout.isEnabled = true
            if (result.success) {
                val intent = Intent(this, WorkoutCompleteActivity::class.java).apply {
                    putExtra("NEW_MOMENTUM", result.newMomentum)
                    putExtra("NEW_STREAK", result.newStreak)
                    putStringArrayListExtra("NEW_BADGES", ArrayList(result.newBadges))
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error: ${result.error}", Toast.LENGTH_LONG).show()
            }
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