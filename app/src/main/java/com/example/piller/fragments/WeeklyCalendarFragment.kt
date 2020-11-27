package com.example.piller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.listAdapters.EliAdapter
import com.example.piller.models.CalendarEvent
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel
import androidx.lifecycle.Observer


class WeeklyCalendarFragment : Fragment() {
    private val weeklyCalendarViewModel: WeeklyCalendarViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var eliAdapters = mutableListOf<EliAdapter>()
    private var eliRecycles = mutableListOf<RecyclerView>()
    private lateinit var fragmentView: View
    private var currentCalendar = Array(7) { mutableListOf<CalendarEvent>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_weekly_calendar, container, false)
        initRecyclersAndAdapters()
        weeklyCalendarViewModel.getWeekEvents(
            profileViewModel.getCurrentEmail(),
            profileViewModel.getCurrentProfile()
        )

        return fragmentView
    }

    fun changeCurrentCalendar(newCalendar:Array<MutableList<CalendarEvent>>){
        currentCalendar=newCalendar
    }

    fun updateRecyclersAndAdapters(){
        for (i in 0 until 7) {
            eliAdapters[i].setData(currentCalendar[i])
            eliAdapters[i].notifyDataSetChanged()
        }
    }

    fun initRecyclersAndAdapters() {

        eliRecycles.add(fragmentView.findViewById(R.id.calendar_sunday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_monday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_tuesday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_wednesday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_thursday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_friday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_saturday_list))


        for (i in 0 until 7) {
            eliRecycles[i].layoutManager = LinearLayoutManager(fragmentView.context)
            eliAdapters.add(EliAdapter(currentCalendar[i]))
            eliRecycles[i].adapter = eliAdapters[i]
        }

    }

    companion object {
        fun newInstance(): WeeklyCalendarFragment = WeeklyCalendarFragment()
    }


}