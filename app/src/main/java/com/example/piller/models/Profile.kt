package com.example.piller.models


class Profile(
    name: String,
    weeklyCal: Array<MutableList<CalendarEvent>>,
    monthlyCal: Array<MutableList<CalendarEvent>>
) {
    private var profileName = name
    private var weeklyCalendar = weeklyCal
    private var monthlyCalendar = monthlyCal
    private var isInitialized = false

    fun getIsInitialized(): Boolean {
        return isInitialized
    }

    fun profileInitialized() {
        isInitialized = true
    }

    fun getProfileName(): String {
        return profileName
    }

    fun getWeeklyCalendar(): Array<MutableList<CalendarEvent>> {
        return weeklyCalendar
    }

    fun setWeeklyCalendar(calendar: Array<MutableList<CalendarEvent>>) {
        weeklyCalendar = calendar
    }

    fun getMonthlyCalendar(): Array<MutableList<CalendarEvent>> {
        return monthlyCalendar
    }

    fun setMonthlyCalendar(calendar: Array<MutableList<CalendarEvent>>) {
        monthlyCalendar = calendar
    }
}