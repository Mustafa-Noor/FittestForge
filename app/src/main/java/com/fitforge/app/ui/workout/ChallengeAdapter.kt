package com.fitforge.app.ui.workout

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fitforge.app.data.models.Challenge
import com.fitforge.app.databinding.ItemChallengeBinding

class ChallengeAdapter(
    private val challenges: List<Challenge>,
    private val onClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount() = challenges.size

    inner class ViewHolder(private val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(challenge: Challenge) {
            Glide.with(binding.ivChallengeBanner.context)
                .load(challenge.bannerImageUrl)
                .into(binding.ivChallengeBanner)
            
            binding.chipDuration.text = "${challenge.durationDays} Days"
            binding.tvChallengeTitle.text = challenge.title
            binding.tvChallengeDesc.text = challenge.description
            
            // Check completion status from SharedPreferences
            val prefs = binding.root.context.getSharedPreferences("challenge_${challenge.id}", Context.MODE_PRIVATE)
            val completedDays = prefs.getStringSet("completed_days", emptySet()) ?: emptySet()
            val isFullyCompleted = completedDays.size >= challenge.durationDays
            
            if (isFullyCompleted) {
                binding.btnStartChallenge.text = "COMPLETED ✅"
                binding.btnStartChallenge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1B5E20"))
            } else {
                binding.btnStartChallenge.text = "START CHALLENGE"
                binding.btnStartChallenge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1565C0"))
            }

            binding.btnStartChallenge.setOnClickListener { onClick(challenge) }
            binding.root.setOnClickListener { onClick(challenge) }
        }
    }
}
