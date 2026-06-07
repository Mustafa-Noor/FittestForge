package com.fitforge.app.ui.workout

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fitforge.app.data.ChallengeData
import com.fitforge.app.data.models.ChallengeDay
import com.fitforge.app.databinding.ActivityChallengeDetailBinding
import com.fitforge.app.databinding.ItemChallengeDayBinding
import com.fitforge.app.data.repository.UserRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class ChallengeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChallengeDetailBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val challengeId = intent.getStringExtra("challenge_id") ?: return
        val challenge = ChallengeData.getById(challengeId) ?: return

        supportActionBar?.title = challenge.title
        Glide.with(this)
            .load(challenge.bannerImageUrl)
            .into(binding.ivDetailBanner)
        binding.tvChallengeDescription.text = challenge.description

        lifecycleScope.launch {
            val user = userRepository.getUserProfile().getOrNull()
            
            val completedDaysStr = user?.completedChallengeDays?.get(challengeId) ?: emptyList()
            val completedDays = completedDaysStr.toSet()
            val lastCompletedDateStr = user?.challengeLastCompletedDate?.get(challengeId)
            val today = LocalDate.now().toString()
            val isLastCompletedToday = lastCompletedDateStr == today

            val currentDay = if (isLastCompletedToday) {
                completedDays.size // They cannot start the next day yet
            } else {
                completedDays.size + 1 // They can start the next day
            }.coerceIn(1, challenge.durationDays)

            val adapter = ChallengeDayAdapter(challenge.days, currentDay, completedDays) { day ->
                if (!day.isRestDay) {
                    // Launch the new Guided Workout list
                    val intent = Intent(this@ChallengeDetailActivity, ChallengeDayWorkoutActivity::class.java)
                    intent.putExtra("challenge_id", challengeId)
                    intent.putExtra("challenge_day_number", day.dayNumber)
                    startActivity(intent)
                } else {
                    // Mark rest day as complete
                    lifecycleScope.launch {
                        val newCompleted = completedDays.toMutableSet()
                        newCompleted.add(day.dayNumber.toString())
                        userRepository.updateChallengeProgress(
                            challengeId = challengeId,
                            completedDays = newCompleted.toList(),
                            lastCompletedDate = today
                        )
                        recreate()
                    }
                }
            }

            binding.rvChallengeDays.layoutManager = LinearLayoutManager(this@ChallengeDetailActivity)
            binding.rvChallengeDays.adapter = adapter
        }
    }
}

class ChallengeDayAdapter(
    private val days: List<ChallengeDay>,
    private val currentDay: Int,
    private val completedDays: Set<String>,
    private val onDayClick: (ChallengeDay) -> Unit
) : RecyclerView.Adapter<ChallengeDayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size

    inner class ViewHolder(private val binding: ItemChallengeDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: ChallengeDay) {
            val isCompleted = completedDays.contains(day.dayNumber.toString())
            val isUnlocked = day.dayNumber <= currentDay

            binding.tvDayNumber.text = "Day ${day.dayNumber}"
            binding.tvDayFocus.text = day.focus
            binding.tvDayDesc.text = if (day.isRestDay) "Rest day" else day.exerciseIds.joinToString(", ") { it.replace("_", " ").replaceFirstChar { c -> c.uppercase() } }

            when {
                isCompleted -> {
                    binding.tvDayStatus.text = "✅"
                    binding.dayBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1B5E20"))
                    binding.tvDayNumber.setTextColor(Color.WHITE)
                    binding.dayContainer.alpha = 1f
                    binding.root.isClickable = false
                }
                isUnlocked -> {
                    binding.tvDayStatus.text = if (day.isRestDay) "😴" else "▶️"
                    binding.dayBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1565C0"))
                    binding.tvDayNumber.setTextColor(Color.WHITE)
                    binding.dayContainer.alpha = 1f
                    binding.root.isClickable = true
                    binding.root.setOnClickListener { onDayClick(day) }
                }
                else -> {
                    binding.tvDayStatus.text = "🔒"
                    binding.dayBadge.backgroundTintList = null
                    binding.tvDayNumber.setTextColor(Color.GRAY)
                    binding.dayContainer.alpha = 0.4f
                    binding.root.isClickable = false
                }
            }
        }
    }
}
