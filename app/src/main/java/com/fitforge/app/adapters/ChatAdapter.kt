package com.fitforge.app.adapters

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitforge.app.databinding.ItemChatMessageAiBinding
import com.fitforge.app.databinding.ItemChatMessageUserBinding
import com.fitforge.app.ui.ai.ChatMessage

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = listOf<ChatMessage>()

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2

        /** Converts **text** markdown bold into SpannableString bold spans */
        fun parseMarkdownBold(input: String): CharSequence {
            val ssb = SpannableStringBuilder()
            val regex = Regex("""\*\*(.+?)\*\*""")
            var lastEnd = 0
            for (match in regex.findAll(input)) {
                ssb.append(input.substring(lastEnd, match.range.first))
                val start = ssb.length
                ssb.append(match.groupValues[1])
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, ssb.length, 0)
                lastEnd = match.range.last + 1
            }
            ssb.append(input.substring(lastEnd))
            return ssb
        }
    }

    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val binding = ItemChatMessageUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatMessageAiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AiViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.binding.tvMessage.text = message.text
        } else if (holder is AiViewHolder) {
            holder.binding.tvMessage.text = parseMarkdownBold(message.text)
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(val binding: ItemChatMessageUserBinding) : RecyclerView.ViewHolder(binding.root)
    class AiViewHolder(val binding: ItemChatMessageAiBinding) : RecyclerView.ViewHolder(binding.root)
}