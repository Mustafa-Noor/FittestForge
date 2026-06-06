package com.fitforge.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.fitforge.app.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth

class CalendarDialog(private val workoutDates: List<String>) : DialogFragment() {

    private lateinit var binding: com.fitforge.app.databinding.DialogCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = com.fitforge.app.databinding.DialogCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(1)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)
        
        binding.calendarView.monthScrollListener = { month ->
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")
            binding.tvMonthName.text = formatter.format(month.yearMonth)
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                val fireIcon = container.fireIcon
                val background = container.background

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.alpha = 1f
                    val dateString = data.date.toString()
                    if (workoutDates.contains(dateString)) {
                        fireIcon.visibility = View.VISIBLE
                        background.visibility = View.VISIBLE
                    } else {
                        fireIcon.visibility = View.INVISIBLE
                        background.visibility = View.INVISIBLE
                    }
                } else {
                    textView.alpha = 0.3f
                    fireIcon.visibility = View.INVISIBLE
                    background.visibility = View.INVISIBLE
                }
            }
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        lateinit var day: CalendarDay
        val textView: TextView = view.findViewById(R.id.tvDayText)
        val fireIcon: TextView = view.findViewById(R.id.tvFireIcon)
        val background: View = view.findViewById(R.id.vBackground)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
