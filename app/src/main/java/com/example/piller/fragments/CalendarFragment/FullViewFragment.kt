package com.example.piller.fragments.CalendarFragment

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.piller.R
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.models.CalendarEvent
import com.example.piller.models.UserObject
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.FullViewViewModel
import com.example.piller.viewModels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FullViewFragment : FragmentWithUserObject() {
    private val _viewModel: FullViewViewModel by activityViewModels()
    private val _profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var _fragmentView: View
    private lateinit var _calendarView: CalendarView
    private lateinit var _loadingScreen: RelativeLayout
    private val _eventDayCircleRadius = DbConstants.EVENTDAY_DRAWABLE_CIRCLE_RADIUS
    private var _currentFirstDayOfMonth = DateUtils.getFirstDayOfMonth()

    companion object {
        fun newInstance(loggedUser: UserObject) = FullViewFragment().apply {
            arguments = Bundle().apply {
                loggedUserObject = loggedUser
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.full_view_fragment, container, false)
        initViews()
        //  create the dots that appear on the calendar
        initEvents()
        initObservers()
        setOnClickListeners()
        return _fragmentView
    }

    private fun updateEventsOnMonthChange() {
        val cal: Calendar = Calendar.getInstance()
        cal.time = _calendarView.currentPageDate.time
        val firstAndLastDays = DateUtils.getFirstAndLastDaysOfSpecificMonth(cal)
        _currentFirstDayOfMonth = firstAndLastDays.first
        _viewModel.updateCalendarByUser(
            loggedUserObject, firstAndLastDays.first, firstAndLastDays.second
        )
    }

    private fun isDateInCurrentMonth(eventDay: EventDay): Boolean {
        val cal: Calendar = Calendar.getInstance()
        cal.time = _calendarView.currentPageDate.time
        val firstAndLastDays = DateUtils.getFirstAndLastDaysOfSpecificMonth(cal)
        val startOfMonth = Calendar.getInstance()
        startOfMonth.timeInMillis = firstAndLastDays.first.time
        val endOfMonth = Calendar.getInstance()
        endOfMonth.timeInMillis = firstAndLastDays.second.time
        DateUtils.zeroTime(startOfMonth)
        DateUtils.setToLastTimeOfDay(endOfMonth)
        return DateUtils.isDateAfter(eventDay.calendar, startOfMonth)
                && DateUtils.isDateAfter(endOfMonth, eventDay.calendar)
    }

    private fun setOnClickListeners() {
        val monthClickListener = object : OnCalendarPageChangeListener {
            override fun onChange() {
                updateEventsOnMonthChange()
            }
        }

        _calendarView.setOnPreviousPageChangeListener(listener = monthClickListener)
        _calendarView.setOnForwardPageChangeListener(listener = monthClickListener)

        _calendarView.setOnDayClickListener(object :
            OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                //  make sure that the selected day is in the current month
                if (isDateInCurrentMonth(eventDay)) {
                    dayClicked(eventDay)
                }
            }
        })
    }

    private fun dayClicked(eventDay: EventDay) {
        val fvpDayFragment: FullviewPopupFragment = FullviewPopupFragment.newInstance()
        val arguments = Bundle()
        val sdf = SimpleDateFormat(getString(R.string.dateFormat), Locale.ENGLISH)
        val date = sdf.format(eventDay.calendar.time)
        val userBundle = Bundle()
        userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, loggedUserObject)
        arguments.putBundle(DbConstants.LOGGED_USER_BUNDLE, userBundle)
        arguments.putString(FullviewPopupFragment.ARG_DATE_STRING, date)
        //  we need to reduce it by 1 because the get day of month starts from 1 (and our list starts from 0..)
        arguments.putParcelableArray(
            FullviewPopupFragment.ARG_EVENTS_LIST,
            _viewModel.mutableCurrentMonthlyCalendar.value?.get(
                eventDay.calendar.get(Calendar.DAY_OF_MONTH) - 1
            )?.toTypedArray()
        )

        fvpDayFragment.arguments = arguments
        fvpDayFragment.setTargetFragment(this, DbConstants.DRUG_DELETE_POPUP)
        activity?.supportFragmentManager?.let {
            fvpDayFragment.show(
                it,
                getString(R.string.fullViewPopupFragmentTag)
            )
        }
    }

    private fun initViews() {
        _calendarView = _fragmentView.findViewById(R.id.fv_calendar)
        _loadingScreen = _fragmentView.findViewById(R.id.loading_screen)
    }

    private fun getDrawableText(typeface: Int?, color: Int): Any {
        //  create a circle bitmapdrawable item
        val bitmap =
            Bitmap.createBitmap(
                DbConstants.eventDayBitMapWidth,
                DbConstants.eventDayBitMapHeight,
                Bitmap.Config.ARGB_8888
            )

        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface =
                (typeface ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)) as Typeface?
            this.color = color
        }

        val bounds = Rect()
        val x = (bitmap.width - bounds.width()) / DbConstants.eventDayBitMapWidthFactor
        val y = (bitmap.height + bounds.height()) / DbConstants.eventDayBitMapHeightFactor
        canvas.drawCircle(x.toFloat(), y.toFloat(), _eventDayCircleRadius, paint)

        return BitmapDrawable(this.resources, bitmap)
    }

    private fun initEvents() {
        val startDate = DateUtils.getFirstDayOfMonth()
        val endDate = DateUtils.getLastDayOfMonth()
        //  get all the events for the selected month
        _viewModel.initiateMonthEvents(
            loggedUserObject,
            _profileViewModel.getCurrentProfile(),
            startDate,
            endDate
        )
    }

    private fun initObservers() {
        //  when the data arrives, set a dot for each day that has at least one event
        _viewModel.mutableCurrentMonthlyCalendar.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { eventsArray ->
                eventsArray?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        setEvents(it)
                    }
                }
            })

        _viewModel.showLoadingScreen.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { eventsArray ->
                eventsArray?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        if (it) {
                            _loadingScreen.visibility = View.VISIBLE
                        } else {
                            _loadingScreen.visibility = View.GONE
                        }
                    }
                }
            })

        _viewModel.mutableDeleteSuccess.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { eventsArray ->
                eventsArray?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        setEvents(_viewModel.mutableCurrentMonthlyCalendar.value!!)
                    }
                }
            })
    }

    private fun setEvents(calendarEvents: Array<MutableList<CalendarEvent>>) {
        val eventsUI: MutableList<EventDay> = ArrayList()
        //val tomorrow = DateUtils.getTomorrowCalendar().time
        // for each day that has at least one event - add an EventDay object in the calendar view
        for ((i, day) in calendarEvents.withIndex()) {
            if (day.isNotEmpty()) {
                //  for each day to add create a calendar, do not reuse the same calendar because it won't work!
                val firstDateOfMonth = Calendar.getInstance()
                firstDateOfMonth.time = _currentFirstDayOfMonth
                val tempCalendar: Calendar = firstDateOfMonth
                tempCalendar.add(Calendar.DATE, i)
                val circleBitmap =
                    getDrawableText(null, color = getDayColor(day, tempCalendar.time))
                eventsUI.add(EventDay(tempCalendar, circleBitmap as Drawable))
            }
        }

        _calendarView.setEvents(eventsUI)
    }

    private fun getDayColor(day: MutableList<CalendarEvent>, dayDate: Date): Int {
        //  saved it in strings and not in colors because when I saved it in strings it
        //  didn't convert it from string to color correctly
        var dayColor = Color.parseColor(getString(R.string.futureDayFullView))
        //  check whether the day is after tomorrow
        if (DateUtils.isDateAfter(Date(), dayDate)) {
            // check if there is any medicine that wasn't taken
            if (day.any { event -> !event.isTaken }) {
                dayColor = Color.RED
            } else {
                //  all the medicines were taken, set the color to be green
                dayColor = Color.GREEN
            }
        }

        return dayColor
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data!!.getBooleanExtra(DbConstants.SHOULD_REFRESH_DATA, false)) {
            initEvents()
        }
        val bundle = data.extras!!.getBundle(DbConstants.DRUG_DELETES)
        // 3
        if (bundle != null) {
            val deleteAll = bundle.getIntArray(DbConstants.DRUGSLIST)!!
            val deleteFuture = bundle.getParcelableArray(DbConstants.FUTURE_DRUGSLIST)
            if (deleteAll.isNotEmpty()) {
                _profileViewModel.currentProfileUpdated()
                _viewModel.deleteDrugs(deleteAll.toList())
            }
            deleteFuture?.map { list -> list as CalendarEvent }?.let {
                if (it.isNotEmpty()) {
                    _profileViewModel.currentProfileUpdated()
                    _viewModel.deleteFutureDrug(it)
                }
            }
        }
    }
}