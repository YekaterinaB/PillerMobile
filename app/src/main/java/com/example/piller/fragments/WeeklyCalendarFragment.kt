package com.example.piller.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.activities.DrugInfoActivity
import com.example.piller.listAdapters.WeeklyDayAdapter
import com.example.piller.models.CalendarEvent
import com.example.piller.models.UserObject
import com.example.piller.models.WeeklyDay
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel


class WeeklyCalendarFragment : FragmentWithUserObject() {
    private val _weeklyCalendarViewModel: WeeklyCalendarViewModel by activityViewModels()
    private val _profileViewModel: ProfileViewModel by activityViewModels()

    private val DRUG_INFO_INTENT_ID = 1

    private lateinit var _fragmentView: View
    private lateinit var currentCalendarEvent: CalendarEvent

    private lateinit var daysContainer: RecyclerView
    private lateinit var daysAdapter: WeeklyDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.fragment_weekly_calendar, container, false)
        initViews(_fragmentView)
        initObservers()
        _weeklyCalendarViewModel.getWeekEvents(
            _loggedUserObject,
            _profileViewModel.getCurrentProfile()
        )

        return _fragmentView
    }

    private fun initObservers() {
        _weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.observe(
            viewLifecycleOwner,
            Observer { calendar ->
                calendar?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        //update view
                        updateRecyclersAndAdapters()
                        //update current profile calendar
                        _profileViewModel.changeCalendarForCurrentProfile(it)
                    }
                }
            })

        _weeklyCalendarViewModel.mutableToastError.observe(
            viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        SnackBar.showToastBar(this.requireContext(), toastMessage)
                    }

                }
            })

        _weeklyCalendarViewModel.mutableDeleteSuccess.observe(
            viewLifecycleOwner,
            Observer { deleteSuccess ->
                if (deleteSuccess) {
                    //update view
                    updateRecyclersAndAdapters()
                    _weeklyCalendarViewModel.mutableDeleteSuccess.value = false
                }
            })
    }

    private fun updateRecyclersAndAdapters() {
        daysAdapter.setCalendarEventDataSet(_weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.value!!)
        daysContainer.smoothScrollToPosition(DateUtils.getDayOfWeekNumber() - 1)
        daysAdapter.notifyDataSetChanged()
    }

    private fun showDrugInfo(calendarEvent: CalendarEvent) {
        currentCalendarEvent = calendarEvent
        val intent = Intent(requireContext(), DrugInfoActivity::class.java)
        intent.putExtra(DbConstants.CALENDAR_EVENT, calendarEvent)
        putLoggedUserObjectInIntent(intent)
        startActivityForResult(intent, DRUG_INFO_INTENT_ID)
    }

    companion object {
        fun newInstance(loggedUser: UserObject) =
            WeeklyCalendarFragment().apply {
                arguments = Bundle().apply {
                    _loggedUserObject = loggedUser
                }
            }
    }

    private fun initViews(fragment: View) {
        daysContainer = fragment.findViewById(R.id.weekly_days_container)
        initWeeklyDaysContainer()
    }

    private fun initWeeklyDaysContainer() {
        val weeklyEvents = _profileViewModel.getCurrentProfile().getWeeklyCalendar()
        val weekDaysDates = DateUtils.getDayNumberForCurrentWeek()
        val dayOfWeekString = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val daysData = mutableListOf<WeeklyDay>()
        for (i in 0 until 7) {
            daysData.add(WeeklyDay(dayOfWeekString[i], weekDaysDates[i]))
        }
        daysContainer.layoutManager = LinearLayoutManager(context)
        daysAdapter = WeeklyDayAdapter(daysData, weeklyEvents) { showDrugInfo(it) }
        daysContainer.adapter = daysAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && data.hasExtra(DbConstants.TAKEN_NEW_VALUE)) {
            //  update taken status
            val newTakenValue = data.getBooleanExtra(
                DbConstants.TAKEN_NEW_VALUE,
                currentCalendarEvent.isTaken
            )
            if (newTakenValue != currentCalendarEvent.isTaken) {
                currentCalendarEvent.isTaken = newTakenValue
                updateRecyclersAndAdapters()
            }
        }

        if (requestCode == DRUG_INFO_INTENT_ID) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    _weeklyCalendarViewModel.deleteDrug(currentCalendarEvent)
                }
                DbConstants.REMOVE_DRUG_FUTURE -> {
                    _weeklyCalendarViewModel.deleteFutureDrug(currentCalendarEvent)
                }
            }
        }
    }
}