package com.fitforge.app.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.adapters.ChatAdapter
import com.fitforge.app.databinding.FragmentAiCoachBinding

class AICoachFragment : Fragment() {

    private var _binding: FragmentAiCoachBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AICoachViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiCoachBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSend.isEnabled = !isLoading
            binding.layoutTyping.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount)
            }
        }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
        
        // Suggestion chips
        binding.chip1.setOnClickListener { viewModel.sendMessage(binding.chip1.text.toString()) }
        binding.chip2.setOnClickListener { viewModel.sendMessage(binding.chip2.text.toString()) }
        binding.chip3.setOnClickListener { viewModel.sendMessage(binding.chip3.text.toString()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}