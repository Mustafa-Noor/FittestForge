package com.fitforge.app.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.databinding.ItemOnboardingPageBinding

data class OnboardingPage(val title: String, val description: String, val imageRes: Int)

class OnboardingAdapter(private val pages: List<OnboardingPage>) : RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    class PageViewHolder(val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.tvTitle.text = page.title
        holder.binding.tvDescription.text = page.description
        // holder.binding.ivImage.setImageResource(page.imageRes)
    }

    override fun getItemCount(): Int = pages.size
}
