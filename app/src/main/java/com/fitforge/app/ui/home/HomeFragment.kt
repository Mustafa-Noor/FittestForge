package com.fitforge.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitforge.app.databinding.FragmentHomeBinding
import com.fitforge.app.ui.workout.LogWorkoutActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Entry animation
        val slideUp = android.view.animation.AnimationUtils.loadAnimation(context, com.fitforge.app.R.anim.slide_up_fade)
        binding.cardMomentum.startAnimation(slideUp)
        binding.layoutStats.startAnimation(slideUp)
        binding.cardTodayPlan.startAnimation(slideUp)

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = "Welcome Back, $name 👋"
        }

        viewModel.totalWorkouts.observe(viewLifecycleOwner) { total ->
            binding.tvStatWorkouts.text = total.toString()
        }

        viewModel.totalHours.observe(viewLifecycleOwner) { hours ->
            binding.tvStatHours.text = hours
        }

        viewModel.momentumData.observe(viewLifecycleOwner) { momentum ->
            binding.momentumProgressBar.progressMax = 100f
            binding.momentumProgressBar.setProgressWithAnimation(momentum.value, 1000)
            binding.tvMomentumValue.text = "${momentum.value.toInt()}%"
            binding.tvMomentumLabel.text = momentum.label
            
            val params = binding.momentumFill.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.matchConstraintPercentWidth = if (momentum.value > 0) momentum.value / 100f else 0.01f
            binding.momentumFill.layoutParams = params

            binding.tvStatStreak.text = "🔥 ${momentum.streak}"
            binding.tvTopStreak.text = momentum.streak.toString()
            
            binding.layoutStreakTop.setOnClickListener {
                StreakOverlayDialog.newInstance(momentum.streak).show(childFragmentManager, "StreakOverlay")
            }
        }

        viewModel.homeMessage.observe(viewLifecycleOwner) { message ->
            binding.tvPlanMessage.text = message
            binding.tvDailyTip.text = message
        }

        viewModel.recentWorkouts.observe(viewLifecycleOwner) { workouts ->
            if (workouts.isEmpty()) {
                binding.rvRecentWorkouts.visibility = View.GONE
                binding.tvRecentTitle.text = "No Workouts Yet"
                binding.btnViewAll.visibility = View.GONE
                
                binding.tvPlanTitle.text = "Ready to move?"
                binding.tvPlanMessage.text = "Your couch is starting to recognize your body shape 💀"
                binding.btnStartWorkoutHome.text = "START"
                binding.btnStartWorkoutHome.isEnabled = true
            } else {
                binding.rvRecentWorkouts.visibility = View.VISIBLE
                binding.tvRecentTitle.text = "Recent Workouts"
                binding.btnViewAll.visibility = View.VISIBLE
                
                val adapter = com.fitforge.app.adapters.WorkoutHistoryAdapter { workout ->
                    // Handle click
                }
                binding.rvRecentWorkouts.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                binding.rvRecentWorkouts.adapter = adapter
                adapter.submitList(workouts.take(3)) // Only show top 3 in home

                val todayDateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                if (workouts.any { it.dateString == todayDateString }) {
                    binding.tvPlanTitle.text = "Great Job Today!"
                    binding.tvPlanMessage.text = "You've already crushed your workout. Rest up for tomorrow! 💪"
                    binding.btnStartWorkoutHome.text = "DONE"
                    binding.btnStartWorkoutHome.isEnabled = false
                } else {
                    binding.tvPlanTitle.text = "Ready to move?"
                    binding.tvPlanMessage.text = "Time to get after it! Let's build that streak! 🔥"
                    binding.btnStartWorkoutHome.text = "START"
                    binding.btnStartWorkoutHome.isEnabled = true
                }
            }
        }

        binding.btnStartWorkoutHome.setOnClickListener {
            findNavController().navigate(com.fitforge.app.R.id.nav_workout)
        }

        binding.btnViewAll.setOnClickListener {
            startActivity(Intent(requireContext(), com.fitforge.app.ui.workout.WorkoutHistoryActivity::class.java))
        }

        binding.ivCalendar.setOnClickListener {
            val dates = viewModel.recentWorkouts.value?.map { it.dateString } ?: emptyList()
            CalendarDialog(dates).show(childFragmentManager, "CalendarDialog")
        }

        // For demo/testing: set date
        binding.tvDate.text = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault()).format(java.util.Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}