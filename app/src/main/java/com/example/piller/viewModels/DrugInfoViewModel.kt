package com.example.piller.viewModels

import androidx.lifecycle.ViewModel
import com.example.piller.models.CalendarEvent

class DrugInfoViewModel : ViewModel() {
    private lateinit var calendarEvent: CalendarEvent

    fun setCalendarEvent(newCalendarEvent: CalendarEvent) {
        calendarEvent = newCalendarEvent
    }

    fun getCalendarEvent(): CalendarEvent {
        return calendarEvent
    }
}