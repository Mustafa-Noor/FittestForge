package com.fitforge.app.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.fitforge.app.data.models.Badge
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.databinding.ActivityBadgeListBinding
import kotlinx.coroutines.launch

class BadgeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBadgeListBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = com.fitforge.app.adapters.BadgeAdapter {}
        binding.rvBadges.layoutManager = GridLayoutManager(this, 3)
        binding.rvBadges.adapter = adapter

        lifecycleScope.launch {
            userRepository.getUserProfile().onSuccess { user ->
                user?.let {
                    val badgesList = buildBadges(it.badges)
                    adapter.submitList(badgesList)
                }
            }
        }
    }

    private fun buildBadges(unlockedMap: Map<String, Boolean>): List<Badge> {
        return listOf(
            Badge("first_workout", "First Workout", "🏋️", "Completed your first workout", unlockConditionText = "Log any workout"),
            Badge("workouts_5", "High Five", "🖐️", "Completed 5 workouts", unlockConditionText = "Log 5 workouts"),
            Badge("workouts_10", "Ten Strong", "💪", "Completed 10 workouts", unlockConditionText = "Log 10 workouts"),
            Badge("workouts_30", "Iron Habit", "🔩", "Completed 30 workouts", unlockConditionText = "Log 30 workouts"),
            Badge("streak_3", "Three-Day Spark", "⚡", "Built a 3-day streak", unlockConditionText = "Work out 3 days in a row"),
            Badge("streak_7", "Weekly Warrior", "🗡️", "Built a 7-day streak", unlockConditionText = "Work out 7 days in a row"),
            Badge("streak_14", "Fortnight Fire", "🔥", "Built a 14-day streak", unlockConditionText = "Work out 14 days in a row"),
            Badge("streak_30", "Monthly Machine", "🏆", "Built a 30-day streak", unlockConditionText = "Work out 30 days in a row"),
            Badge("momentum_peak", "Peak Momentum", "🚀", "Reached 85 momentum", unlockConditionText = "Raise momentum to 85"),
            Badge("leg_day_respect", "Leg Day Respect", "🦵", "Logged legs twice in a row", unlockConditionText = "Log two leg workouts in a row"),
            Badge("early_bird", "Early Bird", "🌅", "Logged a workout before 8 AM", unlockConditionText = "Finish a workout before 8 AM"),
            Badge("recovery_smart", "Recovery Smart", "🛌", "Logged a recovery day", unlockConditionText = "Use Life Happened once")
        ).map { badge ->
            badge.copy(isUnlocked = unlockedMap[badge.id] == true)
        }
    }
}
