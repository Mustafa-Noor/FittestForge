package com.fitforge.app.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ItemSetRowBinding

class WorkoutSetAdapter(
    private val sets: MutableList<WorkoutSet>
) : RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    inner class SetViewHolder(private val binding: ItemSetRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(set: WorkoutSet, position: Int) {
            binding.tvSetNumber.text = (position + 1).toString()
            binding.etWeight.setText(if (set.weightKg > 0) set.weightKg.toString() else "")
            binding.etReps.setText(if (set.reps > 0) set.reps.toString() else "")
            binding.cbDone.isChecked = set.completed

            binding.etWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val w = s.toString().toFloatOrNull() ?: 0f
                    sets[adapterPosition] = sets[adapterPosition].copy(weightKg = w)
                }
            })

            binding.etReps.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val r = s.toString().toIntOrNull() ?: 0
                    sets[adapterPosition] = sets[adapterPosition].copy(reps = r)
                }
            })

            binding.cbDone.setOnCheckedChangeListener { _, isChecked ->
                sets[adapterPosition] = sets[adapterPosition].copy(completed = isChecked)
            }

            binding.btnRemoveSet.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onRemoveClick?.invoke(adapterPosition)
                }
            }
        }
    }

    var onRemoveClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemSetRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position], position)
    }

    override fun getItemCount() = sets.size
    
    fun addSet() {
        sets.add(WorkoutSet())
        notifyItemInserted(sets.size - 1)
    }

    fun removeSet(position: Int) {
        if (position in 0 until sets.size) {
            sets.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, sets.size)
        }
    }
}
