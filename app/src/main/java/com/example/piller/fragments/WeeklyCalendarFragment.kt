package com.example.piller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.listAdapters.EliAdapter
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel


class WeeklyCalendarFragment : Fragment() {
    private val weeklyCalendarViewModel: WeeklyCalendarViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var eliAdapters = mutableListOf<EliAdapter>()
    private var eliRecycles = mutableListOf<RecyclerView>()
    private lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_weekly_calendar, container, false)
        initObservers()
        weeklyCalendarViewModel.getWeekEvents(
            profileViewModel.getCurrentEmail(),
            profileViewModel.getCurrentProfile()
        )
        initRecyclersAndAdapters()
        return fragmentView
    }

    private fun initObservers(){
        weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.observe(
            viewLifecycleOwner,
            Observer { calendar ->
                calendar?.let {
                    if(viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED){
                        //update view
                        updateRecyclersAndAdapters()
                        //update current profile calendar
                        profileViewModel.changeCalendarForCurrentProfile(it)
                    }

                }
            })

        weeklyCalendarViewModel.mutableToastError.observe(
            viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {
                    if(viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED){
                        SnackBar.showToastBar(this.requireContext(),toastMessage)
                    }

                }
            })
    }

    fun updateRecyclersAndAdapters(){
        for (i in 0 until 7) {
            val newList= weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.value!!.get(i)
            eliAdapters[i].setData(newList)
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

        val weeklyEvents=profileViewModel.getCurrentProfile().getWeeklyCalendar()
        for (i in 0 until 7) {
            eliRecycles[i].layoutManager = LinearLayoutManager(fragmentView.context)
            // add as empty list
            eliAdapters.add(EliAdapter(weeklyEvents[i]))
            eliRecycles[i].adapter = eliAdapters[i]
        }

    }

    companion object {
        fun newInstance(): WeeklyCalendarFragment = WeeklyCalendarFragment()
    }


}