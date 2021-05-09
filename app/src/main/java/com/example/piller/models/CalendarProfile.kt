package com.example.piller.models


class CalendarProfile(
    private var profile: Profile,
    weeklyCal: Array<MutableList<CalendarEvent>>,
    monthlyCal: Array<MutableList<CalendarEvent>>
) {
    private var weeklyCalendar = weeklyCal
    private var monthlyCalendar = monthlyCal
    private var isInitialized = false

    fun getProfileObject(): Profile {
        return profile
    }

    fun getIsInitialized(): Boolean {
        return isInitialized
    }

    fun setIsInitialized(isInit: Boolean = true) {
        isInitialized = isInit
    }

    fun getProfileName(): String {
        return profile.name
    }

    fun getProfileRelation(): String {
        return profile.relation
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