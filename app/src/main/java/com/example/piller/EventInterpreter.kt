package com.example.piller

import com.example.piller.models.CalendarEvent
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class EventInterpreter {
    fun getEventsForCalendarByDate(
        start: Date,
        end: Date,
        drugList: JSONArray,
        maxMissDaysThreshold: Int = 0
    ): Array<MutableList<CalendarEvent>> {
        val daysBetween = getDaysBetween(start, end) + 1
        val eventList = Array(daysBetween) { mutableListOf<CalendarEvent>() }
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val drugName = drug.get("name") as String
            val drugRxcui = drug.get("rxcui").toString()
            val drugInfo = drug.get("drug_info") as JSONObject
            val drugEventList = getDrugEvent(drugName, drugRxcui, drugInfo, start, end)
            // put all event in array
            if (drugEventList.isNotEmpty()) {
                updateMissedDaysCheckboxVisibility(maxMissDaysThreshold, drugEventList)
                for (j in 0 until drugEventList.size) {
                    val indexDay = drugEventList[j].index_day
                    eventList[indexDay].add(drugEventList[j])
                }
            }
        }
        return eventList
    }

    private fun updateMissedDaysCheckboxVisibility(
        maxMissDaysThreshold: Int,
        drugEventList: MutableList<CalendarEvent>
    ) {
        //  set limit to min between: maxMissDaysThreshold and drugEventList.size
        var limit = maxMissDaysThreshold
        if (limit > drugEventList.size) {
            limit = drugEventList.size
        }

        //  show the checkbox only for the events that are in the last maxMissDaysThreshold occurrences
        for (j in drugEventList.size - 1 downTo drugEventList.size - limit) {
            drugEventList[j].showTakenCheckBox = true
        }
    }

    private fun getDaysBetween(first: Date, second: Date): Int {
        val firstCal = Calendar.getInstance()
        firstCal.time = first
        setCalendarTime(firstCal, 0, 0, 0)
        val secondCal = Calendar.getInstance()
        secondCal.time = second
        setCalendarTime(secondCal, 0, 0, 0)

        val diff: Long = secondCal.timeInMillis - firstCal.timeInMillis
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }

    private fun getDrugEvent(
        drugName: String,
        drugRxcui: String,
        drugInfo: JSONObject,
        start: Date,
        end: Date
    ): MutableList<CalendarEvent> {
        val eventList: MutableList<CalendarEvent> = mutableListOf()
        val repeatStart = (drugInfo.get("repeat_start") as String).toLong()
        val repeatEnd = (drugInfo.get("repeat_end") as String).toLong()
        val repeatYear = drugInfo.get("repeat_year") as Int
        val repeatMonth = drugInfo.get("repeat_month") as Int
        val repeatDay = drugInfo.get("repeat_day") as Int
        val repeatWeek = drugInfo.get("repeat_week") as Int
        val repeatWeekday = (drugInfo.get("repeat_weekday") as String).split(',').map(String::toInt)

        var calendarCurrent = Calendar.getInstance()
        calendarCurrent.time = start

        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = end

        val calendarRepeatEnd = Calendar.getInstance()
        //  the next lines are: if repeatEnd != 0 then set calendarCurrent to repeatEnd
        //  otherwise set it to calendarEnd time
        calendarRepeatEnd.timeInMillis = when (repeatEnd != 0.toLong()) {
            true -> repeatEnd
            false -> calendarEnd.timeInMillis
        }

        // start repeat count
        val calendarStartRepeat = Calendar.getInstance()
        calendarStartRepeat.timeInMillis = repeatStart
        //save intake time
        val hourOfDay = calendarStartRepeat.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = calendarStartRepeat.get(Calendar.MINUTE)
        // start of day at 00:00
        setCalendarTime(calendarStartRepeat, 0, 0, 0)

        // if the start intake is after start day
        if (isDateBefore(calendarCurrent, calendarStartRepeat)) {
            calendarCurrent = calendarStartRepeat
        }

        // check days between actual start date and the new start date
        var indexDay = getDaysBetween(start, calendarCurrent.time)
        val calendarClosestRepeat = Calendar.getInstance()
        calendarClosestRepeat.timeInMillis = calendarStartRepeat.timeInMillis
        val onlyOnce = isOnlyRepeat(repeatYear, repeatMonth, repeatWeek, repeatDay)
        while (isDateBefore(calendarCurrent, calendarEnd) && isDateBefore(
                calendarCurrent,
                calendarRepeatEnd
            )
        ) {
            val isInRepeat = isDateInRepeat(
                repeatYear,
                repeatMonth,
                repeatWeek,
                repeatWeekday,
                repeatDay,
                calendarCurrent,
                calendarClosestRepeat,
                onlyOnce
            )
            if (isInRepeat) {
                //event is in repeats
                calendarCurrent.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarCurrent.set(Calendar.MINUTE, minuteOfDay)
                val event =
                    CalendarEvent(
                        drugName,
                        drugRxcui,
                        indexDay,
                        calendarCurrent.time,
                        false
                    )
                //todo is taken
                eventList.add(event)
                setCalendarTime(calendarCurrent, 0, 0, 0)
            }
            if (onlyOnce) {
                //  if the event happens only once - stop the loop (whether we added it or not)
                break
            }
            calendarCurrent.add(Calendar.DATE, 1)
            indexDay += 1
        }
        return eventList
    }


    private fun isOnlyRepeat(
        repeatYear: Int = 1,
        repeatMonth: Int = 1,
        repeatWeek: Int = 1,
        repeatDay: Int = 1
    ): Boolean = (repeatYear == 0 && repeatMonth == 0 && repeatWeek == 0 && repeatDay == 0)


    private fun isDateInRepeat(
        repeatYear: Int = 1,
        repeatMonth: Int = 1,
        repeatWeek: Int = 1,
        repeatDayOfWeek: List<Int> = emptyList(),
        repeatDay: Int = 1,
        currentDate: Calendar,
        calendarClosestRepeat: Calendar,
        isOnlyRepeat: Boolean
    ): Boolean {
        var isInRepeat = false

        when {
            (isOnlyRepeat && areDatesEqual(currentDate, calendarClosestRepeat)) -> {
                //  check whether it's a one time event (all the fields are equal to 0)
                isInRepeat = true
            }
            (repeatYear != 0) -> {
                if (repeatYear == 1) {
                    isInRepeat = true
                } else {
                    isInRepeat =
                        setRepeatEvent(
                            currentDate,
                            repeatYear,
                            calendarClosestRepeat,
                            Calendar.YEAR
                        )
                }
            }
            (repeatMonth != 0) -> {
                if (repeatMonth == 1) {
                    isInRepeat = true
                } else {
                    isInRepeat =
                        setRepeatEvent(
                            currentDate,
                            repeatMonth,
                            calendarClosestRepeat,
                            Calendar.MONTH
                        )
                }
            }
            (repeatDay != 0) -> {
                if (repeatDay == 1) {
                    isInRepeat = true
                } else {
                    isInRepeat =
                        setRepeatEvent(currentDate, repeatDay, calendarClosestRepeat, Calendar.DATE)
                }
            }
            (repeatWeek != 0) -> {
                if (repeatWeek == 1) {
                    if (currentDate.get(Calendar.DAY_OF_WEEK) in repeatDayOfWeek) {
                        //  all the days of week in the list are at the same week, so we can add all of them
                        isInRepeat = true
                    }
                } else {
                    isInRepeat =
                        setRepeatWeekEvent(
                            currentDate,
                            repeatWeek,
                            calendarClosestRepeat,
                            repeatDayOfWeek
                        )
                }
            }
        }
//        if (repeatYear == 0 && repeatMonth == 0 && repeatWeek == 0 && repeatDay == 0) {
//            //current is already the firstRepeat
//            onlyOnceRepeat = true
//            if (currentDate.time == calendarClosestRepeat.time) {
//                isInRepeat = true
//            }
//        } else if (repeatYear != 1) {
//            isInRepeat =
//                setRepeatEvent(currentDate, repeatYear, calendarClosestRepeat, Calendar.YEAR)
//        } else if (repeatMonth != 1) {
//            isInRepeat =
//                setRepeatEvent(currentDate, repeatMonth, calendarClosestRepeat, Calendar.MONTH)
//        } else if (repeatWeek != 1) {
//            isInRepeat =
//                setRepeatWeekEvent(currentDate, repeatMonth, calendarClosestRepeat, repeatDayOfWeek)
//        } else if (repeatDay != 1) {
//            isInRepeat =
//                setRepeatEvent(currentDate, repeatDay, calendarClosestRepeat, Calendar.DATE)
//        }
        // if all of the fields above equal to 1, then it means we should repeat every day,
        // therefore we can just return the current date
        return isInRepeat
    }

    private fun setRepeatWeekEvent(
        currentDate: Calendar,
        repeat: Int,
        calendarClosestRepeat: Calendar,
        daysOfWeek: List<Int>
    ): Boolean {
        var isIn = false
        val calenderRunFromStartToCurrent = Calendar.getInstance()
        calenderRunFromStartToCurrent.timeInMillis = calendarClosestRepeat.timeInMillis
        while (isDateBefore(calenderRunFromStartToCurrent, currentDate)) {
            calenderRunFromStartToCurrent.add(Calendar.WEEK_OF_MONTH, repeat)
        }
        if (currentDate.get(Calendar.DAY_OF_WEEK) in daysOfWeek &&
            calenderRunFromStartToCurrent.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            calenderRunFromStartToCurrent.get(Calendar.WEEK_OF_YEAR) == currentDate.get(Calendar.WEEK_OF_YEAR)
        ) {
            //  all the days of week in the list are at the same week, so we can add all of them
            isIn = true
        }

        calendarClosestRepeat.timeInMillis = calenderRunFromStartToCurrent.timeInMillis

        return isIn
    }


    private fun setRepeatEvent(
        currentDate: Calendar,
        repeat: Int,
        calendarClosestRepeat: Calendar,
        addTimeUnit: Int
    ): Boolean {
        var isIn = false
        val calenderRunFromStartToCurrent = Calendar.getInstance()
        calenderRunFromStartToCurrent.timeInMillis = calendarClosestRepeat.timeInMillis

        //calenderRunFromStartToCurrent.time < currentDate.time
        while (isDateBefore(calenderRunFromStartToCurrent, currentDate)) {
            calenderRunFromStartToCurrent.add(addTimeUnit, repeat)
        }
//        calenderRunFromStartToCurrent.time == currentDate.time
        if (areDatesEqual(calenderRunFromStartToCurrent, currentDate)) {
            isIn = true
        }

        calendarClosestRepeat.timeInMillis = calenderRunFromStartToCurrent.timeInMillis

        return isIn
    }

    private fun areDatesEqual(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.DATE) == date2.get(Calendar.DATE)
                && date1.get(Calendar.HOUR_OF_DAY) == date2.get(Calendar.HOUR_OF_DAY)
                && date1.get(Calendar.MINUTE) == date2.get(Calendar.MINUTE)
    }

    private fun isDateBefore(date1: Calendar, date2: Calendar): Boolean {
        if (date1.time < date2.time) {
            return !areDatesEqual(date1, date2)
        }
        return false
    }

    fun isDateAfter(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date1
        val calendar2 = Calendar.getInstance()
        calendar2.time = date2
        return !isDateBefore(calendar1, calendar2)
    }

    fun isDateAfter(date1: Calendar, date2: Calendar): Boolean {
        return !isDateBefore(date1, date2)
    }

    private fun setCalendarTime(calendar: Calendar, hour: Int, minutes: Int, seconds: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, seconds)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun getTomorrowDateInMillis(startDate: Date): Long {
        // get start of this week in milliseconds
        val cal: Calendar = Calendar.getInstance()
        cal.time = startDate
        cal.add(Calendar.DATE, 1)
        setCalendarTime(cal, 0, 0, 0)
        return cal.timeInMillis
    }

    fun getFirstDayOfWeek(): Date {
        // get start of this week in milliseconds
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        setCalendarTime(cal, 0, 0, 0)
        return cal.time
    }

    fun getLastDayOfWeek(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        setCalendarTime(cal, 23, 59, 59)
        return cal.time
    }

    fun getFirstDayOfSpecificMonth(date: Date): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        //  set time to 00:00:00
        cal.set(Calendar.DAY_OF_MONTH, 1)
        setCalendarTime(cal, 0, 0, 0)
        return cal.time
    }

    fun getFirstDayOfMonth(): Date {
        return getFirstDayOfSpecificMonth(Calendar.getInstance().time)
    }

    fun getLastDayOfSpecificMonth(date: Date): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        //  set time to 23:59:59
        setCalendarTime(cal, 23, 59, 59)
        return cal.time
    }

    fun getLastDayOfMonth(): Date {
        return getLastDayOfSpecificMonth(Calendar.getInstance().time)
    }

    fun getFirstAndLastDaysOfSpecificMonth(calendar: Calendar): Pair<Date, Date> {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = calendar.time
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDay = calendar.time
        return Pair(firstDay, lastDay)
    }
}