package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ActivityLogWorkoutBinding

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
        addExerciseFromIntentIfPresent()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnAddExercise.setOnClickListener {
            addBlankExercise()
        }

        binding.btnLifeHappened.setOnClickListener {
            RecoveryLogBottomSheet().show(supportFragmentManager, "recovery_log")
        }

        binding.btnFinishWorkout.setOnClickListener {
            if (exerciseList.isEmpty()) {
                Toast.makeText(this, "Add at least one exercise", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (exerciseList.any { it.exerciseName.isBlank() }) {
                Toast.makeText(this, "Add a name for each exercise", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnFinishWorkout.isEnabled = false
            viewModel.saveWorkout(exerciseList, binding.etNotes.text.toString(), startTimeMillis)
        }

        viewModel.saveResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnFinishWorkout.isEnabled = true
            
            result.onSuccess { saveResult ->
                val intent = Intent(this, WorkoutCompleteActivity::class.java).apply {
                    putStringArrayListExtra("new_badges", ArrayList(saveResult.newBadges))
                    putExtra("new_momentum", saveResult.newMomentum)
                    putExtra("new_streak", saveResult.newStreak)
                    putExtra("workout_duration", saveResult.durationMinutes)
                    putExtra("total_sets", saveResult.totalSets)
                }
                startActivity(intent)
                finish()
            }.onFailure { e ->
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
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
                adapter.notifyItemRangeChanged(pos, exerciseList.size - pos)
            }
        )
        binding.rvWorkoutSets.layoutManager = LinearLayoutManager(this)
        binding.rvWorkoutSets.adapter = adapter
    }

    private fun addExerciseFromIntentIfPresent() {
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: return
        addExercise(
            WorkoutExercise(
                exerciseId = intent.getStringExtra("EXERCISE_ID").orEmpty(),
                exerciseName = exerciseName,
                muscleGroup = intent.getStringExtra("MUSCLE_GROUP").orEmpty(),
                sets = listOf(WorkoutSet())
            )
        )
    }

    private fun addBlankExercise() {
        addExercise(
            WorkoutExercise(
                exerciseName = "",
                sets = listOf(WorkoutSet())
            )
        )
    }

    private fun addExercise(exercise: WorkoutExercise) {
        exerciseList.add(exercise)
        adapter.notifyItemInserted(exerciseList.size - 1)
        binding.rvWorkoutSets.post {
            binding.rvWorkoutSets.smoothScrollToPosition(exerciseList.lastIndex)
        }
    }
}
