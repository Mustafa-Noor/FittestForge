package com.fitforge.app.ui.food

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.data.models.FoodLog
import com.fitforge.app.data.repository.FoodRepository
import com.fitforge.app.databinding.FragmentFoodIntakeBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FoodIntakeFragment : Fragment() {
    private var _binding: FragmentFoodIntakeBinding? = null
    private val binding get() = _binding!!
    private val foodRepository = FoodRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTodayCard()
        loadFoodLogs()
    }

    override fun onResume() {
        super.onResume()
        setupTodayCard()
        loadFoodLogs()
    }

    private fun setupTodayCard() {
        val today = LocalDate.now()
        val displayFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM")
        binding.tvTodayDate.text = today.format(displayFormat)

        lifecycleScope.launch {
            val log = foodRepository.getFoodLogForDate(today.toString()).getOrNull()
            binding.tvTodayCalories.text = if (log != null && log.totalCalories > 0) {
                "${log.totalCalories} kcal"
            } else {
                "Not logged yet"
            }
        }

        binding.btnLogToday.setOnClickListener {
            val intent = Intent(requireContext(), FoodIntakeDetailActivity::class.java)
            intent.putExtra("date", today.toString())
            startActivity(intent)
        }
    }

    private fun loadFoodLogs() {
        lifecycleScope.launch {
            val logs = foodRepository.getFoodLogs().getOrDefault(emptyList())
            val today = LocalDate.now().toString()
            val history = logs.filter { it.dateString != today }

            if (history.isEmpty()) {
                binding.tvEmptyHistory.visibility = View.VISIBLE
                binding.rvFoodLogs.visibility = View.GONE
            } else {
                binding.tvEmptyHistory.visibility = View.GONE
                binding.rvFoodLogs.visibility = View.VISIBLE
                val adapter = FoodLogAdapter(history) { log ->
                    val intent = Intent(requireContext(), FoodIntakeDetailActivity::class.java)
                    intent.putExtra("date", log.dateString)
                    startActivity(intent)
                }
                binding.rvFoodLogs.layoutManager = LinearLayoutManager(requireContext())
                binding.rvFoodLogs.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
