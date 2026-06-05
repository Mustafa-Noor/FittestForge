package com.fitforge.app.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fitforge.app.databinding.DialogStreakOverlayBinding

class StreakOverlayDialog : DialogFragment() {

    private var _binding: DialogStreakOverlayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStreakOverlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val streak = arguments?.getInt("STREAK", 0) ?: 0

        binding.tvStreakCount.text = "$streak Day Streak!"
        
        if (streak <= 0) {
            binding.tvStreakMessage.text = "Start the work buddy!"
        } else {
            binding.tvStreakMessage.text = "Great you are an alien!"
        }

        binding.btnGotIt.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(streak: Int): StreakOverlayDialog {
            val args = Bundle()
            args.putInt("STREAK", streak)
            val fragment = StreakOverlayDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
