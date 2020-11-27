package com.example.piller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.piller.R

class WeeklyCalendarFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weekly_calendar, container, false)
    }

    companion object {
        fun newInstance(): WeeklyCalendarFragment = WeeklyCalendarFragment()
    }
}