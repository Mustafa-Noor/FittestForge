package com.fitforge.app.ui.workout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fitforge.app.databinding.ActivityExerciseDetailBinding

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val exerciseId = intent.getStringExtra("EXERCISE_ID") ?: ""
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "Exercise"
        val exerciseGif = intent.getStringExtra("EXERCISE_GIF") ?: ""
        val muscleGroup = intent.getStringExtra("MUSCLE_GROUP") ?: ""
        val isSelectMode = intent.getBooleanExtra("select_mode", false)
        
        binding.toolbar.title = exerciseName
        binding.tvExerciseName.text = exerciseName

        if (exerciseGif.isNotEmpty()) {
            Glide.with(this)
                .asGif()
                .load(exerciseGif)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.ivDetailGif)
        }

        if (isSelectMode) {
            binding.btnAddToWorkout.visibility = View.VISIBLE
            binding.btnAddToWorkout.setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra("EXERCISE_ID", exerciseId)
                    putExtra("EXERCISE_NAME", exerciseName)
                    putExtra("MUSCLE_GROUP", muscleGroup)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        } else {
            binding.btnAddToWorkout.visibility = View.GONE
        }
        
        // Populate instructions later
    }
}
