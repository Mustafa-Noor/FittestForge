package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.utils.MomentumCalculator
import com.fitforge.app.utils.PersonalityStrings
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.fitforge.app.databinding.DialogRecoveryLogBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecoveryLogBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogRecoveryLogBinding? = null
    private val binding get() = _binding!!

    private val workoutRepository = WorkoutRepository()
    private val userRepository = UserRepository()
    private var selectedReason = "rest"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecoveryLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up chip click listeners
        binding.chipRest.setOnClickListener { selectedReason = "rest" }
        binding.chipSick.setOnClickListener { selectedReason = "sick" }
        binding.chipExams.setOnClickListener { selectedReason = "exams" }
        binding.chipTravel.setOnClickListener { selectedReason = "travel" }
        binding.chipOther.setOnClickListener { selectedReason = "other" }

        binding.btnProtectDay.setOnClickListener {
            protectDay()
        }
    }

    private fun protectDay() {
        lifecycleScope.launch {
            try {
                // Create a recovery day workout entry
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val today = LocalDate.now().format(formatter)

                val recoveryWorkout = Workout(
                    id = "",
                    date = Timestamp.now(),
                    dateString = today,
                    durationMinutes = 0,
                    totalSets = 0,
                    notes = "Recovery day",
                    isRecoveryDay = true,
                    recoveryReason = selectedReason,
                    exercises = emptyList()
                )

                // Save to Firestore
                val saveResult = workoutRepository.saveWorkout(recoveryWorkout)

                if (saveResult.isSuccess) {
                    // Update momentum in user document
                    val userResult = userRepository.getUserProfile()
                    val user = userResult.getOrNull()
                    if (user != null) {
                        val newMomentum = MomentumCalculator.calculateNewMomentum(
                            user.momentum,
                            0,
                            false,
                            true  // lifeHappened = true
                        )
                        userRepository.updateMomentum(newMomentum)

                        // Show personality-mode message
                        val mode = user.personalityMode
                        val personalities = listOf("hype", "drill", "chill", "chaos")
                        val personalityMode = when (mode) {
                            in personalities -> mode
                            else -> "hype"
                        }
                        
                        val message = PersonalityStrings.getRecoveryLogMessage(
                            when (personalityMode) {
                                "hype" -> com.fitforge.app.data.models.PersonalityMode.HYPE
                                "drill" -> com.fitforge.app.data.models.PersonalityMode.DRILL
                                "chill" -> com.fitforge.app.data.models.PersonalityMode.CHILL
                                "chaos" -> com.fitforge.app.data.models.PersonalityMode.CHAOS
                                else -> com.fitforge.app.data.models.PersonalityMode.HYPE
                            }
                        )
                        
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                    dismiss()
                } else {
                    Toast.makeText(context, "Failed to protect day", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
