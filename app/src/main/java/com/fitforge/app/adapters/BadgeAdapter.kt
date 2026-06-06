package com.fitforge.app.adapters

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.R
import com.fitforge.app.data.models.Badge
import com.fitforge.app.databinding.ItemBadgeBinding

class BadgeAdapter(private val onBadgeClick: (Badge) -> Unit) :
    ListAdapter<Badge, BadgeAdapter.BadgeViewHolder>(BadgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BadgeViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: Badge) {
            // Always show the real badge emoji — locked ones shown at 35% opacity
            binding.tvBadgeEmoji.text = badge.emoji
            binding.tvBadgeName.text = badge.name

            if (badge.isUnlocked) {
                // Fully unlocked — vivid colored background
                binding.badgeImageContainer.setBackgroundResource(R.drawable.bg_badge_unlocked)
                binding.tvBadgeName.setTextColor(binding.root.context.getColor(R.color.text_primary))
                // Clear any grayscale filter
                binding.tvBadgeEmoji.alpha = 1f
                binding.badgeImageContainer.alpha = 1f
            } else {
                // Locked — show at low opacity, desaturated
                binding.badgeImageContainer.setBackgroundResource(R.drawable.bg_badge_locked)
                binding.tvBadgeName.setTextColor(binding.root.context.getColor(R.color.text_secondary))
                // Apply grayscale + low opacity to the emoji
                binding.tvBadgeEmoji.alpha = 0.35f
                binding.badgeImageContainer.alpha = 0.5f
            }

            binding.root.setOnClickListener { onBadgeClick(badge) }
        }
    }

    class BadgeDiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(oldItem: Badge, newItem: Badge): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Badge, newItem: Badge): Boolean = oldItem == newItem
    }
}
