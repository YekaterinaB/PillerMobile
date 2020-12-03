package com.example.piller.fragments

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.example.piller.EventInterpreter
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.FullViewViewModel
import com.example.piller.viewModels.ProfileViewModel
import java.util.*
import kotlin.collections.ArrayList

class FullViewFragment : Fragment() {
    private val eventInterpreter = EventInterpreter()
    private val viewModel: FullViewViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var fragmentView: View
    private lateinit var calendarView: CalendarView
    private val eventDayCircleRadius = DbConstants.EVENTDAY_DRAWABLE_CIRCLE_RADIUS
    private val eventDayBitMapWidth = 256
    private val eventDayBitMapHeight = 128
    private var currentFirstDayOfMonth = eventInterpreter.getFirstDayOfMonth()

    companion object {
        fun newInstance() = FullViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.full_view_fragment, container, false)
        initViews()
        //  create the dots that appear on the calendar
        initEvents()
        setOnClickListeners()
        return fragmentView
    }

    private fun updateEventsOnMonthChange() {
        val cal: Calendar = Calendar.getInstance()
        cal.time = calendarView.currentPageDate.time
        val firstAndLastDays = eventInterpreter.getFirstAndLastDaysOfSpecificMonth(cal)
        currentFirstDayOfMonth = firstAndLastDays.first
        viewModel.updateCalendarByUser(
            profileViewModel.getCurrentEmail(),
            profileViewModel.getCurrentProfile(),
            firstAndLastDays.first,
            firstAndLastDays.second
        )
    }

    private fun setOnClickListeners() {
        val monthClickListener = object : OnCalendarPageChangeListener {
            override fun onChange() {
                updateEventsOnMonthChange()
            }
        }

        calendarView.setOnPreviousPageChangeListener(listener = monthClickListener)
        calendarView.setOnForwardPageChangeListener(listener = monthClickListener)
    }

    private fun initViews() {
        calendarView = fragmentView.findViewById(R.id.fv_calendar)
    }

    private fun getDrawableText(typeface: Int?, color: Int, size: Int): Any {
        //  create a circle bitmapdrawable item
        val bitmap =
            Bitmap.createBitmap(eventDayBitMapWidth, eventDayBitMapHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
//        val scale = this.resources.displayMetrics.density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface =
                (typeface ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)) as Typeface?
            this.color = color
//            this.textSize = (size * scale).toInt().toFloat() * 2
        }

        val bounds = Rect()
        //  todo - remove comments before submitting final project!
//        text was an argument to this function
//        paint.getTextBounds(text, 0, text.length, bounds)
        val x = (bitmap.width - bounds.width()) / 2
        val y = (bitmap.height + bounds.height()) / 2
        canvas.drawCircle(x.toFloat(), y.toFloat(), eventDayCircleRadius, paint)
//        canvas.drawCircle(x.toFloat() + 40, y.toFloat(), 20F, paint)
//        canvas.drawText("+5", x.toFloat() + 80, y.toFloat() + 12, paint)

        return BitmapDrawable(this.resources, bitmap)
    }

    private fun initEvents() {
        val startDate = eventInterpreter.getFirstDayOfMonth()
        val endDate = eventInterpreter.getLastDayOfMonth()
        //  get all the events for the selected month
        viewModel.initiateMonthEvents(
            profileViewModel.getCurrentEmail(),
            profileViewModel.getCurrentProfile(),
            startDate,
            endDate
        )

        //  when the data arrives, set a dot for each day that has at least one event
        viewModel.mutableCurrentMonthlyCalendar.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { eventsArray ->
                eventsArray?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                        val events: MutableList<EventDay> = ArrayList()
                        // for each day that has at least one event - add an EventDay object in the calendar view
                        for ((i, day) in it.withIndex()) {
                            if (day.isNotEmpty()) {
                                //  for each day to add create a calendar, do not reuse the same calendar because it won't work!
                                val firstDateOfMonth = Calendar.getInstance()
                                firstDateOfMonth.time = currentFirstDayOfMonth
                                val tempCalendar: Calendar = firstDateOfMonth
                                tempCalendar.add(Calendar.DATE, i)
                                val circleBitmap =
                                    getDrawableText(null, color = Color.BLACK, size = 15)
                                events.add(EventDay(tempCalendar, circleBitmap as Drawable))
                            }
                        }

                        calendarView.setEvents(events)
                    }
                }
            })
    }
}