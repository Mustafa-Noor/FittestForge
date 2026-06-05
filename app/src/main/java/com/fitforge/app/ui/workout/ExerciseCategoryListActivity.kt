package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fitforge.app.adapters.ExerciseAdapter
import com.fitforge.app.data.local.ExerciseData
import com.fitforge.app.databinding.ActivityExerciseCategoryListBinding

class ExerciseCategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseCategoryListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Exercises"
        binding.toolbar.title = categoryName
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = ExerciseAdapter { exercise ->
            val detailIntent = Intent(this, ExerciseDetailActivity::class.java)
            detailIntent.putExtra("EXERCISE_ID", exercise.id)
            detailIntent.putExtra("EXERCISE_NAME", exercise.name)
            detailIntent.putExtra("EXERCISE_GIF", exercise.gifUrl)
            detailIntent.putExtra("EXERCISE_BODY_PART", exercise.bodyPart)
            startActivity(detailIntent)
        }
        
        binding.rvExercises.adapter = adapter
        adapter.submitList(ExerciseData.getExercisesByCategory(categoryName))
    }
}
