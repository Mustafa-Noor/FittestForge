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
import com.fitforge.app.data.models.Challenge
import com.fitforge.app.data.models.ChallengeDay
import com.fitforge.app.databinding.ActivityChallengeDetailBinding
import com.fitforge.app.databinding.ItemChallengeDayBinding
import java.time.LocalDate

class ChallengeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChallengeDetailBinding
    private var challengeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        challengeId = intent.getStringExtra("challenge_id")
        refreshUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        val id = challengeId ?: return
        val challenge = ChallengeData.getById(id) ?: return

        supportActionBar?.title = challenge.title
        Glide.with(this)
            .load(challenge.bannerImageUrl)
            .into(binding.ivDetailBanner)
        binding.tvChallengeDescription.text = challenge.description

        val prefs = getSharedPreferences("challenge_$id", Context.MODE_PRIVATE)
        val completedDays = prefs.getStringSet("completed_days", emptySet()) ?: emptySet()
        val lastCompletedDateStr = prefs.getString("last_completed_date", null)
        val today = LocalDate.now().toString()
        val isLastCompletedToday = lastCompletedDateStr == today

        val currentDay = if (isLastCompletedToday) {
            completedDays.size 
        } else {
            completedDays.size + 1
        }.coerceIn(1, challenge.durationDays)

        val adapter = ChallengeDayAdapter(challenge.days, currentDay, completedDays) { day ->
            if (!day.isRestDay) {
                val intent = Intent(this, ChallengeDayWorkoutActivity::class.java)
                intent.putExtra("challenge_id", id)
                intent.putExtra("challenge_day_number", day.dayNumber)
                startActivity(intent)
            } else {
                val newCompleted = completedDays.toMutableSet()
                newCompleted.add(day.dayNumber.toString())
                prefs.edit()
                    .putStringSet("completed_days", newCompleted)
                    .putString("last_completed_date", today)
                    .apply()
                refreshUI()
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
