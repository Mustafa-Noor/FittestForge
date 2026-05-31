package com.fitforge.app.ui.workout

import android.os.Bundle
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

        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "Exercise"
        val exerciseGif = intent.getStringExtra("EXERCISE_GIF") ?: ""
        
        binding.toolbar.title = exerciseName
        binding.tvExerciseName.text = exerciseName

        if (exerciseGif.isNotEmpty()) {
            Glide.with(this)
                .asGif()
                .load(exerciseGif)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.ivDetailGif)
        }
        
        // Populate instructions later
    }
}