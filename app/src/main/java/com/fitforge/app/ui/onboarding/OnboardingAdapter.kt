package com.fitforge.app.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.fitforge.app.databinding.ItemOnboardingPageBinding

data class OnboardingPage(val title: String, val description: String, val imageUrl: String)

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
        
        if (page.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(page.imageUrl)
                .transform(CenterCrop(), RoundedCorners(32))
                .into(holder.binding.ivImage)
        }
    }

    override fun getItemCount(): Int = pages.size
}
