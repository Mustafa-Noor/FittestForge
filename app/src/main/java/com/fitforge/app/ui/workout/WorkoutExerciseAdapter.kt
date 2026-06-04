package com.fitforge.app.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.data.models.WorkoutExercise
import com.fitforge.app.data.models.WorkoutSet
import com.fitforge.app.databinding.ItemSetRowBinding
import com.fitforge.app.databinding.ItemWorkoutSetBinding

class WorkoutExerciseAdapter(
    private var exercises: MutableList<WorkoutExercise>,
    private val onAddSet: (Int) -> Unit,
    private val onRemoveExercise: (Int) -> Unit
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemWorkoutSetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutSetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.binding.tvExerciseName.text = exercise.exerciseName
        
        holder.binding.layoutSetsContainer.removeAllViews()
        exercise.sets.forEachIndexed { index, set ->
            val setBinding = ItemSetRowBinding.inflate(LayoutInflater.from(holder.itemView.context), holder.binding.layoutSetsContainer, false)
            setBinding.tvSetNumber.text = (index + 1).toString()
            setBinding.etWeight.setText(if (set.weightKg > 0) set.weightKg.toString() else "")
            setBinding.etReps.setText(if (set.reps > 0) set.reps.toString() else "")
            setBinding.cbDone.isChecked = set.completed
            
            // In a real app, we'd add TextWatchers here to update the model
            holder.binding.layoutSetsContainer.addView(setBinding.root)
        }

        holder.binding.btnAddSet.setOnClickListener { onAddSet(position) }
        holder.binding.btnRemoveExercise.setOnClickListener { onRemoveExercise(position) }
    }

    override fun getItemCount() = exercises.size

    fun updateList(newList: List<WorkoutExercise>) {
        exercises = newList.toMutableList()
        notifyDataSetChanged()
    }
}