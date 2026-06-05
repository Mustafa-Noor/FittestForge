package com.fitforge.app.ui.workout

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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
        holder.binding.etExerciseName.setText(exercise.exerciseName)
        holder.binding.etExerciseName.addTextChangedListener(afterTextChanged { text ->
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val current = exercises[adapterPosition]
                exercises[adapterPosition] = current.copy(exerciseName = text)
            }
        })

        holder.binding.tvMuscleGroup.text = exercise.muscleGroup
        holder.binding.tvMuscleGroup.visibility =
            if (exercise.muscleGroup.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
        
        holder.binding.layoutSetsContainer.removeAllViews()
        exercise.sets.forEachIndexed { index, set ->
            val setBinding = ItemSetRowBinding.inflate(LayoutInflater.from(holder.itemView.context), holder.binding.layoutSetsContainer, false)
            setBinding.tvSetNumber.text = (index + 1).toString()
            setBinding.etWeight.setText(if (set.weightKg > 0) set.weightKg.toString() else "")
            setBinding.etReps.setText(if (set.reps > 0) set.reps.toString() else "")
            setBinding.cbDone.isChecked = set.completed

            setBinding.etWeight.addTextChangedListener(afterTextChanged { text ->
                updateSet(holder.bindingAdapterPosition, index) {
                    it.copy(weightKg = text.toFloatOrNull() ?: 0f)
                }
            })
            setBinding.etReps.addTextChangedListener(afterTextChanged { text ->
                updateSet(holder.bindingAdapterPosition, index) {
                    it.copy(reps = text.toIntOrNull() ?: 0)
                }
            })
            setBinding.cbDone.setOnCheckedChangeListener { _, isChecked ->
                updateSet(holder.bindingAdapterPosition, index) {
                    it.copy(completed = isChecked)
                }
            }
            setBinding.btnRemoveSet.setOnClickListener {
                removeSet(holder.bindingAdapterPosition, index)
            }
            setBinding.btnRemoveSet.visibility =
                if (exercise.sets.size > 1) android.view.View.VISIBLE else android.view.View.INVISIBLE

            holder.binding.layoutSetsContainer.addView(setBinding.root)
        }

        holder.binding.btnAddSet.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onAddSet(adapterPosition)
            }
        }
        holder.binding.btnRemoveExercise.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onRemoveExercise(adapterPosition)
            }
        }
    }

    override fun getItemCount() = exercises.size

    fun updateList(newList: List<WorkoutExercise>) {
        exercises = newList.toMutableList()
        notifyDataSetChanged()
    }

    private fun updateSet(position: Int, setIndex: Int, transform: (WorkoutSet) -> WorkoutSet) {
        if (position == RecyclerView.NO_POSITION) return
        val exercise = exercises.getOrNull(position) ?: return
        val sets = exercise.sets.toMutableList()
        val set = sets.getOrNull(setIndex) ?: return

        sets[setIndex] = transform(set)
        exercises[position] = exercise.copy(sets = sets)
    }

    private fun removeSet(position: Int, setIndex: Int) {
        if (position == RecyclerView.NO_POSITION) return
        val exercise = exercises.getOrNull(position) ?: return
        if (exercise.sets.size <= 1) return

        val sets = exercise.sets.toMutableList()
        sets.removeAt(setIndex)
        exercises[position] = exercise.copy(sets = sets)
        notifyItemChanged(position)
    }

    private fun afterTextChanged(onChange: (String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                onChange(s?.toString().orEmpty())
            }
        }
    }
}
