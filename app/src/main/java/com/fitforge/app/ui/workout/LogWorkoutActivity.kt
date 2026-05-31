package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fitforge.app.databinding.ActivityLogWorkoutBinding

class LogWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogWorkoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnLifeHappened.setOnClickListener {
            // Show RecoveryBottomSheetDialog
            RecoveryLogBottomSheet().show(supportFragmentManager, "recovery_log")
        }

        binding.btnFinishWorkout.setOnClickListener {
            // Validate form and save workout via repository then goto WorkoutComplete
            startActivity(Intent(this, WorkoutCompleteActivity::class.java))
            finish()
        }
    }
}