package com.fitforge.app.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.fitforge.app.R
import com.fitforge.app.adapters.BadgeAdapter
import com.fitforge.app.databinding.FragmentProgressBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()
    private lateinit var badgeAdapter: BadgeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBadgeRecyclerView()
        setupCharts()
        observeViewModel()

        viewModel.loadProgressData()
    }

    private fun setupBadgeRecyclerView() {
        badgeAdapter = BadgeAdapter { badge ->
            if (badge.isUnlocked) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(badge.name)
                    .setMessage(badge.description)
                    .setPositiveButton("Awesome", null)
                    .show()
            }
        }
        binding.rvBadges.layoutManager = GridLayoutManager(context, 3)
        binding.rvBadges.adapter = badgeAdapter
    }

    private fun setupCharts() {
        // Momentum Line Chart Setup
        binding.momentumLineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(false)
            setPinchZoom(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.LTGRAY
            animateX(1000, Easing.EaseInOutQuad)
        }

        // Balance Pie Chart Setup
        binding.balancePieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setCenterTextSize(16f)
            setEntryLabelColor(Color.BLACK)
            animateY(1400, Easing.EaseInOutQuad)
        }
    }

    private fun observeViewModel() {
        viewModel.momentumData.observe(viewLifecycleOwner) { points ->
            val entries = points.mapIndexed { index, point ->
                Entry(index.toFloat(), point.value)
            }
            val dataSet = LineDataSet(entries, "Momentum").apply {
                color = resources.getColor(R.color.primary, null)
                setCircleColor(resources.getColor(R.color.primary, null))
                lineWidth = 3f
                circleRadius = 5f
                setDrawCircleHole(true)
                valueTextSize = 0f
                setDrawFilled(true)
                fillDrawable = resources.getDrawable(R.drawable.momentum_chart_gradient, null)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            binding.momentumLineChart.data = LineData(dataSet)
            binding.momentumLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(points.map { it.date.takeLast(5) })
            binding.momentumLineChart.invalidate()
        }

        viewModel.workoutDNA.observe(viewLifecycleOwner) { dna ->
            binding.tvArchetype.text = dna.archetype
            binding.tvDnaAvgWorkouts.text = "📅 Avg ${String.format("%.1f", dna.avgWorkoutsPerWeek)} workouts/week"
            binding.tvDnaTopMuscle.text = "🎯 ${dna.topMuscleGroup} dominates (${dna.topMuscleGroupPercent}%)"
            binding.tvDnaPeakDay.text = "⚡ Peak day: ${dna.peakDay}"
            
            // Populate Pie Chart with muscle distribution (Mocking distribution for now based on top muscle)
            val pieEntries = mutableListOf<PieEntry>()
            pieEntries.add(PieEntry(dna.topMuscleGroupPercent.toFloat(), dna.topMuscleGroup))
            pieEntries.add(PieEntry((100 - dna.topMuscleGroupPercent).toFloat(), "Others"))
            
            val pieDataSet = PieDataSet(pieEntries, "").apply {
                colors = listOf(resources.getColor(R.color.primary, null), resources.getColor(R.color.divider, null))
                valueTextSize = 12f
                sliceSpace = 3f
            }
            binding.balancePieChart.data = PieData(pieDataSet)
            binding.balancePieChart.centerText = "Muscle\nBalance"
            binding.balancePieChart.invalidate()
        }

        // Mock badges for now
        badgeAdapter.submitList(listOf(
            com.fitforge.app.data.models.Badge("1", "Early Bird", "🌅", "Workout before 8 AM", true),
            com.fitforge.app.data.models.Badge("2", "Streak King", "👑", "7 day streak", true),
            com.fitforge.app.data.models.Badge("3", "Heavy Lifter", "🏋️", "Lift 100kg total", false),
            com.fitforge.app.data.models.Badge("4", "Consistency", "📈", "30 days active", false)
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}