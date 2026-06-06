package com.fitforge.app.ui.workout

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            binding.challengeBanner.setBackgroundColor(Color.parseColor(challenge.bannerColor))
            binding.tvChallengeEmoji.text = challenge.bannerEmoji
            binding.chipDuration.text = "${challenge.durationDays} Days"
            binding.tvChallengeTitle.text = challenge.title
            binding.tvChallengeDesc.text = challenge.description
            binding.btnStartChallenge.setOnClickListener { onClick(challenge) }
            binding.root.setOnClickListener { onClick(challenge) }
        }
    }
}
