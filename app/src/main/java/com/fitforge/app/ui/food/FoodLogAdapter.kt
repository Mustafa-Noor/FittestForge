package com.fitforge.app.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.data.models.FoodLog
import com.fitforge.app.databinding.ItemFoodLogBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FoodLogAdapter(
    private val logs: List<FoodLog>,
    private val onClick: (FoodLog) -> Unit
) : RecyclerView.Adapter<FoodLogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount() = logs.size

    inner class ViewHolder(private val binding: ItemFoodLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: FoodLog) {
            val date = try {
                LocalDate.parse(log.dateString).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
            } catch (e: Exception) { log.dateString }

            binding.tvFoodLogDate.text = date
            binding.tvFoodLogBreakdown.text = "B: ${log.breakfast} | L: ${log.lunch} | D: ${log.dinner} | S: ${log.snacks}"
            binding.tvFoodLogCalories.text = "${log.totalCalories} kcal"
            binding.root.setOnClickListener { onClick(log) }
        }
    }
}
