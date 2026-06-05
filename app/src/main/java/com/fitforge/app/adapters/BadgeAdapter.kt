package com.fitforge.app.adapters

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
            binding.tvBadgeEmoji.text = if (badge.isUnlocked) badge.emoji else "LOCK"
            binding.tvBadgeName.text = if (badge.isUnlocked) badge.name else "Locked"

            if (badge.isUnlocked) {
                binding.badgeImageContainer.setBackgroundResource(R.drawable.bg_badge_unlocked)
                binding.tvBadgeName.setTextColor(binding.root.context.getColor(R.color.text_primary))
            } else {
                binding.badgeImageContainer.setBackgroundResource(R.drawable.bg_badge_locked)
                binding.tvBadgeName.setTextColor(binding.root.context.getColor(R.color.text_secondary))
            }

            binding.root.setOnClickListener { onBadgeClick(badge) }
        }
    }

    class BadgeDiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(oldItem: Badge, newItem: Badge): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Badge, newItem: Badge): Boolean = oldItem == newItem
    }
}
