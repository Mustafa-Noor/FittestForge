package com.fitforge.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.data.models.Workout
import com.fitforge.app.databinding.ItemWorkoutHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryAdapter(private val onWorkoutClick: (Workout) -> Unit) :
    ListAdapter<Workout, WorkoutHistoryAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkoutViewHolder(private val binding: ItemWorkoutHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(workout: Workout) {
            if (workout.date != null) {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvDate.text = sdf.format(workout.date.toDate())
            } else {
                binding.tvDate.text = workout.dateString
            }
            
            if (workout.isRecoveryDay) {
                binding.tvDetails.text = workout.recoveryReason ?: "Recovery Day"
                binding.ivShield.visibility = View.VISIBLE
            } else {
                binding.tvDetails.text = "${workout.durationMinutes}m • ${workout.exercises.size} Exercises"
                binding.ivShield.visibility = View.GONE
            }

            itemView.setOnClickListener { onWorkoutClick(workout) }
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem == newItem
        }
    }
}