package com.fitforge.app.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.fitforge.app.databinding.DialogRecoveryLogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RecoveryLogBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogRecoveryLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogWorkoutViewModel by viewModels({ requireActivity() })
    private var selectedReason = "rest"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecoveryLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipRest.setOnClickListener { selectedReason = "rest" }
        binding.chipSick.setOnClickListener { selectedReason = "sick" }
        binding.chipExams.setOnClickListener { selectedReason = "exams" }
        binding.chipTravel.setOnClickListener { selectedReason = "travel" }
        binding.chipOther.setOnClickListener { selectedReason = "other" }

        binding.btnProtectDay.setOnClickListener {
            binding.btnProtectDay.isEnabled = false
            viewModel.logRecovery(selectedReason)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Day Protected! Stay safe.", Toast.LENGTH_SHORT).show()
                dismiss()
            }.onFailure { e ->
                binding.btnProtectDay.isEnabled = true
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
