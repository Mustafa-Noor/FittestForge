package com.fitforge.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fitforge.app.data.local.ExerciseData
import com.fitforge.app.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<ExerciseData.ExerciseCategory>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ExerciseData.ExerciseCategory) {
            binding.tvCategoryName.text = category.name
            
            Glide.with(binding.root.context)
                .load(category.imageUrl)
                .into(binding.ivCategoryIcon)

            binding.root.setOnClickListener {
                onItemClick(category.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}
