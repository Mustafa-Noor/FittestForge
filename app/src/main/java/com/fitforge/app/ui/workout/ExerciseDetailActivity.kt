package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fitforge.app.R
import com.fitforge.app.adapters.WorkoutSetAdapter
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ActivityExerciseDetailBinding
import com.fitforge.app.utils.showToast

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseDetailBinding
    private val viewModel: LogWorkoutViewModel by viewModels()
    private val setsList = mutableListOf<WorkoutSet>(WorkoutSet())
    private lateinit var adapter: WorkoutSetAdapter
    private var startTimeMillis: Long = 0
    private var timer: CountDownTimer? = null
    private var timeRemainingSeconds = 60 // default 60s rest
    private var isTimerRunning = false

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
                .load(exerciseGif)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivDetailGif)
        }

        adapter = WorkoutSetAdapter(setsList)
        adapter.onRemoveClick = { position ->
            adapter.removeSet(position)
        }
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

        binding.btnTimerMinus.setOnClickListener {
            timeRemainingSeconds = maxOf(0, timeRemainingSeconds - 10)
            updateTimerText()
        }

        binding.btnTimerPlus.setOnClickListener {
            timeRemainingSeconds += 10
            updateTimerText()
        }

        setupObservers()

        binding.btnFinishWorkout.setOnClickListener {
            val validSets = setsList.filter { it.completed && it.reps > 0 }
            if (validSets.isEmpty()) {
                showToast("Complete at least one set")
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
                showToast("Exercise saved to your workout log! You can go back now.")
                binding.btnFinishWorkout.text = "SAVED"

                val isChallengeMode = intent.getBooleanExtra("IS_CHALLENGE_MODE", false)
                if (isChallengeMode) {
                    val exerciseId = intent.getStringExtra("EXERCISE_ID")
                    val resultIntent = Intent().apply {
                        putExtra("COMPLETED_EXERCISE_ID", exerciseId)
                    }
                    setResult(android.app.Activity.RESULT_OK, resultIntent)
                    finish()
                }
            } else {
                showToast("Error: ${result.error}")
            }
        }
    }

    private fun startTimer() {
        if (isTimerRunning) {
            timer?.cancel()
            isTimerRunning = false
            binding.btnStartTimer.text = "Start"
        } else {
            timer?.cancel()
            timer = object : CountDownTimer(timeRemainingSeconds * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeRemainingSeconds = (millisUntilFinished / 1000).toInt()
                    updateTimerText()
                }

                override fun onFinish() {
                    timeRemainingSeconds = 0
                    isTimerRunning = false
                    binding.btnStartTimer.text = "Start"
                    updateTimerText()
                }
            }.start()
            isTimerRunning = true
            binding.btnStartTimer.text = "Pause"
        }
    }

    private fun updateTimerText() {
        val min = timeRemainingSeconds / 60
        val sec = timeRemainingSeconds % 60
        binding.tvTimer.text = String.format("%02d:%02d", min, sec)
    }

    private fun stopTimer() {
        timer?.cancel()
        isTimerRunning = false
        timeRemainingSeconds = 60
        binding.btnStartTimer.text = "Start"
        updateTimerText()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}