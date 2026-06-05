package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fitforge.app.adapters.WorkoutSetAdapter
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ActivityExerciseDetailBinding

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseDetailBinding
    private val viewModel: LogWorkoutViewModel by viewModels()
    private val setsList = mutableListOf<WorkoutSet>(WorkoutSet())
    private lateinit var adapter: WorkoutSetAdapter
    private var startTimeMillis: Long = 0
    private var timer: CountDownTimer? = null
    private var timeRemainingSeconds = 60 // default 60s rest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTimeMillis = System.currentTimeMillis()
        binding.toolbar.setNavigationOnClickListener { finish() }

        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "Exercise"
        val exerciseGif = intent.getStringExtra("EXERCISE_GIF") ?: ""
        val exerciseId = intent.getStringExtra("EXERCISE_ID") ?: ""
        val exerciseBodyPart = intent.getStringExtra("EXERCISE_BODY_PART") ?: "Full Body"
        
        binding.toolbar.title = exerciseName
        binding.tvExerciseName.text = exerciseName

        if (exerciseGif.isNotEmpty()) {
            Glide.with(this)
                .asGif()
                .load(exerciseGif)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.ivDetailGif)
        }

        adapter = WorkoutSetAdapter(setsList)
        binding.rvSets.layoutManager = LinearLayoutManager(this)
        binding.rvSets.adapter = adapter

        binding.btnAddSet.setOnClickListener {
            adapter.addSet()
        }

        binding.btnStartTimer.setOnClickListener {
            startTimer()
        }

        binding.btnStopTimer.setOnClickListener {
            stopTimer()
        }

        setupObservers()

        binding.btnFinishWorkout.setOnClickListener {
            val validSets = setsList.filter { it.completed && it.reps > 0 }
            if (validSets.isEmpty()) {
                Toast.makeText(this, "Complete at least one set", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnFinishWorkout.isEnabled = false

            val workoutExercise = WorkoutExercise(
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                muscleGroup = exerciseBodyPart,
                sets = validSets
            )

            viewModel.saveWorkout(listOf(workoutExercise), "", startTimeMillis)
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

    private fun startTimer() {
        timer?.cancel()
        timeRemainingSeconds = 60
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingSeconds = (millisUntilFinished / 1000).toInt()
                val min = timeRemainingSeconds / 60
                val sec = timeRemainingSeconds % 60
                binding.tvTimer.text = String.format("%02d:%02d", min, sec)
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                // Play sound or vibrate
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        binding.tvTimer.text = "00:00"
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}