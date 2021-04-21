package com.example.piller.fragments.CalendarFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.piller.R
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.fragments.FullViewFragment
import com.example.piller.fragments.WeeklyCalendarFragment
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants
import lib.kingja.switchbutton.SwitchMultiButton


class CalendarFragment : FragmentWithUserObject() {
    private lateinit var _calendarSwitch: SwitchMultiButton
    private lateinit var _calendarContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_calendar, container, false)
        initViews(fragmentView)
        initListeners()
        showCalendarFragment(DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID)
        return fragmentView
    }

    private fun initListeners() {
        val calendarViewOptions = resources.getStringArray(R.array.monthly_weekly)
        _calendarSwitch.setOnSwitchListener { position, tabText ->
            if (tabText == calendarViewOptions[0]) {
                showCalendarFragment(DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID)
            } else {
                showCalendarFragment(DbConstants.FULL_VIEW_FRAGMENT_ID)
            }
        }
    }

    private fun showCalendarFragment(fragmentId: String) {
        var fragment: Fragment? = null
        when (fragmentId) {
            DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID ->
                fragment = WeeklyCalendarFragment.newInstance(_loggedUserObject)
            DbConstants.FULL_VIEW_FRAGMENT_ID ->
                fragment = FullViewFragment.newInstance(_loggedUserObject)
        }
        openFragment(fragment!!, fragmentId)
    }

    private fun openFragment(fragment: Fragment, id_fragment: String) {
        val fragmentTransaction: FragmentTransaction =
            activity?.supportFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.calender_container, fragment, id_fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }

    private fun initViews(fragmentView: View) {
        _calendarSwitch = fragmentView.findViewById(R.id.calendar_switch)
        _calendarContainer = fragmentView.findViewById(R.id.calender_container)
    }

    companion object {
        @JvmStatic
        fun newInstance(loggedUser: UserObject) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    _loggedUserObject = loggedUser
                }
            }
    }
}