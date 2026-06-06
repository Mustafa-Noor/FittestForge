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
import com.fitforge.app.data.ChallengeData
import com.fitforge.app.data.models.ChallengeDay
import com.fitforge.app.databinding.ActivityChallengeDetailBinding
import com.fitforge.app.databinding.ItemChallengeDayBinding
import java.time.LocalDate

class ChallengeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChallengeDetailBinding

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
        binding.challengeBannerDetail.setBackgroundColor(Color.parseColor(challenge.bannerColor))
        binding.tvDetailEmoji.text = challenge.bannerEmoji
        binding.tvChallengeDescription.text = challenge.description

        val prefs = getSharedPreferences("challenge_$challengeId", Context.MODE_PRIVATE)
        val startDateStr = prefs.getString("start_date", null)
        val startDate = if (startDateStr != null) LocalDate.parse(startDateStr) else {
            val today = LocalDate.now().toString()
            prefs.edit().putString("start_date", today).apply()
            LocalDate.now()
        }

        val completedDays = prefs.getStringSet("completed_days", emptySet()) ?: emptySet()
        val today = LocalDate.now()
        val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        val currentDay = daysSinceStart.coerceIn(1, challenge.durationDays)

        val adapter = ChallengeDayAdapter(challenge.days, currentDay, completedDays) { day ->
            if (!day.isRestDay) {
                // Mark day as started — open LogWorkoutActivity
                val intent = Intent(this, LogWorkoutActivity::class.java)
                intent.putExtra("challenge_day_focus", day.focus)
                intent.putExtra("challenge_id", challengeId)
                intent.putExtra("challenge_day_number", day.dayNumber)
                startActivity(intent)
            } else {
                // Mark rest day as complete
                val newCompleted = completedDays.toMutableSet()
                newCompleted.add(day.dayNumber.toString())
                prefs.edit().putStringSet("completed_days", newCompleted).apply()
                recreate()
            }
        }

        binding.rvChallengeDays.layoutManager = LinearLayoutManager(this)
        binding.rvChallengeDays.adapter = adapter
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
