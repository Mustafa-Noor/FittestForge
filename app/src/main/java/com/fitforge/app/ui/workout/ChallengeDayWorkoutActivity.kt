package com.fitforge.app.ui.workout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fitforge.app.R
import com.fitforge.app.data.ChallengeData
import com.fitforge.app.data.local.ExerciseData
import com.fitforge.app.data.models.Exercise
import com.fitforge.app.databinding.ActivityChallengeDayWorkoutBinding
import com.fitforge.app.databinding.ItemChallengeExerciseBinding
import java.time.LocalDate
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet

class ChallengeDayWorkoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChallengeDayWorkoutBinding
    private val viewModel: LogWorkoutViewModel by viewModels()
    private val userRepository = UserRepository()
    private lateinit var challengeId: String
    private var dayNumber: Int = 0
    private var exercises = mutableListOf<ExerciseItem>()
    private lateinit var adapter: ChallengeDayExerciseAdapter

    data class ExerciseItem(val exercise: Exercise, var isCompleted: Boolean = false)

    private val startExerciseForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val completedExerciseId = result.data?.getStringExtra("COMPLETED_EXERCISE_ID")
            completedExerciseId?.let { id ->
                val index = exercises.indexOfFirst { it.exercise.id == id }
                if (index != -1) {
                    exercises[index].isCompleted = true
                    adapter.notifyItemChanged(index)
                    checkAllCompleted()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDayWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        challengeId = intent.getStringExtra("challenge_id") ?: return
        dayNumber = intent.getIntExtra("challenge_day_number", 1)

        val challenge = ChallengeData.getById(challengeId) ?: return
        val day = challenge.days.find { it.dayNumber == dayNumber } ?: return

        supportActionBar?.title = "Day $dayNumber"
        binding.tvFocus.text = day.focus
        binding.tvDesc.text = day.description

        // Map exercise IDs to Exercise objects
        val allExercises = ExerciseData.exercises
        exercises = day.exerciseIds.mapNotNull { exId ->
            val ex = allExercises.find { it.id == exId } ?: allExercises.find { it.name.lowercase().replace(" ", "_") == exId }
            if (ex != null) ExerciseItem(ex, false) else null
        }.toMutableList()

        adapter = ChallengeDayExerciseAdapter(exercises) { exerciseItem ->
            val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
                putExtra("EXERCISE_ID", exerciseItem.exercise.id)
                putExtra("EXERCISE_NAME", exerciseItem.exercise.name)
                putExtra("EXERCISE_GIF", exerciseItem.exercise.gifUrl)
                putExtra("EXERCISE_BODY_PART", exerciseItem.exercise.bodyPart)
                putExtra("IS_CHALLENGE_MODE", true)
            }
            startExerciseForResult.launch(intent)
        }

        binding.rvExercises.layoutManager = LinearLayoutManager(this)
        binding.rvExercises.adapter = adapter

        setupObservers()

        binding.btnCompleteDay.setOnClickListener {
            completeDay()
        }
    }

    private fun setupObservers() {
        viewModel.saveResult.observe(this) { result ->
            if (isFinishing || isDestroyed) return@observe
            binding.btnCompleteDay.isEnabled = true
            if (result.success) {
                Toast.makeText(applicationContext, "Day $dayNumber Completed! Great job!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, WorkoutCompleteActivity::class.java).apply {
                    putExtra("NEW_MOMENTUM", result.newMomentum)
                    putExtra("NEW_STREAK", result.newStreak)
                    putStringArrayListExtra("NEW_BADGES", ArrayList(result.newBadges))
                }
                startActivity(intent)
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(applicationContext, "Error saving workout: ${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAllCompleted() {
        if (exercises.all { it.isCompleted }) {
            binding.btnCompleteDay.isEnabled = true
        }
    }

    private fun completeDay() {
        binding.btnCompleteDay.isEnabled = false
        
        lifecycleScope.launch {
            val user = userRepository.getUserProfile().getOrNull()
            val completedDaysStr = user?.completedChallengeDays?.get(challengeId) ?: emptyList()
            val completedDays = completedDaysStr.toMutableSet()
            completedDays.add(dayNumber.toString())
            
            userRepository.updateChallengeProgress(
                challengeId = challengeId,
                completedDays = completedDays.toList(),
                lastCompletedDate = LocalDate.now().toString()
            )

            val workoutExercises = exercises.map {
                WorkoutExercise(
                    exerciseId = it.exercise.id,
                    exerciseName = it.exercise.name,
                    muscleGroup = it.exercise.bodyPart,
                    sets = listOf(WorkoutSet(reps = 10, weightKg = 0f, completed = true)) // Save dummy completion sets
                )
            }
            
            // Save globally
            viewModel.saveWorkout(workoutExercises, "Challenge: Day $dayNumber", System.currentTimeMillis() - 30 * 60000)
        }
    }
}

class ChallengeDayExerciseAdapter(
    private val items: List<ChallengeDayWorkoutActivity.ExerciseItem>,
    private val onItemClick: (ChallengeDayWorkoutActivity.ExerciseItem) -> Unit
) : RecyclerView.Adapter<ChallengeDayExerciseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemChallengeExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChallengeDayWorkoutActivity.ExerciseItem) {
            binding.tvExerciseName.text = item.exercise.name
            binding.tvExerciseTarget.text = "Target: ${item.exercise.target}"

            if (item.isCompleted) {
                binding.ivStatus.setImageResource(R.drawable.ic_check)
                binding.ivStatus.setColorFilter(android.graphics.Color.parseColor("#1B5E20")) // Green
                binding.root.alpha = 0.6f
                binding.root.isClickable = false
            } else {
                binding.ivStatus.setImageResource(R.drawable.ic_play)
                binding.ivStatus.setColorFilter(android.graphics.Color.parseColor("#1565C0")) // Blue
                binding.root.alpha = 1.0f
                binding.root.isClickable = true
                binding.root.setOnClickListener { onItemClick(item) }
            }
        }
    }
}
