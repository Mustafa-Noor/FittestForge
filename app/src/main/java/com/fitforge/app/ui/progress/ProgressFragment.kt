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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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
            val message = if (badge.isUnlocked) badge.description else "Locked: ${badge.unlockConditionText}"
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(badge.name)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show()
        }
        binding.rvBadges.layoutManager = GridLayoutManager(context, 3)
        binding.rvBadges.adapter = badgeAdapter
    }

    private fun setupCharts() {
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
            val hasData = points.isNotEmpty()
            binding.momentumLineChart.visibility = if (hasData) View.VISIBLE else View.GONE
            binding.tvMomentumEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
            if (!hasData) {
                binding.momentumLineChart.clear()
                return@observe
            }

            val entries = points.mapIndexed { index, point ->
                Entry(index.toFloat(), point.value)
            }
            val dataSet = LineDataSet(entries, "Logged Sets").apply {
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
            binding.momentumLineChart.xAxis.valueFormatter =
                IndexAxisValueFormatter(points.map { it.date.takeLast(5) })
            binding.momentumLineChart.invalidate()
        }

        viewModel.workoutDNA.observe(viewLifecycleOwner) { dna ->
            binding.tvArchetype.text = dna.archetype
            binding.tvDnaAvgWorkouts.text = "Avg ${String.format("%.1f", dna.avgWorkoutsPerWeek)} workouts/week"
            binding.tvDnaTopMuscle.text = "${dna.topMuscleGroup} focus (${dna.topMuscleGroupPercent}%)"
            binding.tvDnaPeakDay.text = "Peak day: ${dna.peakDay}"
        }

        viewModel.muscleGroupData.observe(viewLifecycleOwner) { muscleData ->
            val hasData = muscleData.isNotEmpty()
            binding.balancePieChart.visibility = if (hasData) View.VISIBLE else View.GONE
            binding.tvBalanceEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
            if (!hasData) {
                binding.balancePieChart.clear()
                return@observe
            }

            val pieEntries = muscleData
                .filter { it.key.isNotBlank() && it.value > 0 }
                .map { PieEntry(it.value.toFloat(), it.key) }

            val pieDataSet = PieDataSet(pieEntries, "").apply {
                colors = listOf(
                    resources.getColor(R.color.primary, null),
                    resources.getColor(R.color.success, null),
                    resources.getColor(R.color.warning, null),
                    resources.getColor(R.color.accent, null),
                    resources.getColor(R.color.text_secondary, null)
                )
                valueTextSize = 12f
                sliceSpace = 3f
            }
            binding.balancePieChart.data = PieData(pieDataSet)
            binding.balancePieChart.centerText = "Muscle\nBalance"
            binding.balancePieChart.invalidate()
        }

        viewModel.badges.observe(viewLifecycleOwner) { badges ->
            badgeAdapter.submitList(badges)
        }

        viewModel.hasWorkoutData.observe(viewLifecycleOwner) { hasData ->
            binding.tvNoWorkoutMessage.visibility = if (hasData) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
