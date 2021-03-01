package com.example.piller

import com.example.piller.models.CalendarEvent
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class EventInterpreter {
    fun getEventsForCalendarByDate(
        start: Date,
        end: Date,
        drugList: JSONArray,
        maxMissDaysThreshold: Int = 0
    ): Array<MutableList<CalendarEvent>> {
        val daysBetween = DateUtils.getDaysBetween(start, end) + 1
        val eventList = Array(daysBetween) { mutableListOf<CalendarEvent>() }
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val drugName = drug.get("name") as String
            val drugRxcui = drug.get("rxcui").toString()
            val event_id = drug.get("event_id").toString()
            val drugInfo = drug.get("drug_info") as JSONObject
            val drugEventList = getDrugEvent(drugName, drugRxcui, drugInfo, start, end, event_id)
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


    private fun getDrugEvent(
        drugName: String,
        drugRxcui: String,
        drugInfo: JSONObject,
        start: Date,
        end: Date,
        event_id: String
    ): MutableList<CalendarEvent> {
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
        DateUtils.setCalendarTime(calendarStartRepeat, 0, 0, 0)

        // if the start intake is after start day
        if (DateUtils.isDateBefore(calendarCurrent, calendarStartRepeat)) {
            calendarCurrent = calendarStartRepeat
        }

        // check days between actual start date and the new start date
        val calendarClosestRepeat = Calendar.getInstance()
        calendarClosestRepeat.timeInMillis = calendarStartRepeat.timeInMillis
        val onlyOnce = isOnlyRepeat(repeatYear, repeatMonth, repeatWeek, repeatDay)

        return createEventListFromRepeats(
            calendarCurrent,
            calendarEnd,
            calendarClosestRepeat,
            calendarRepeatEnd,
            onlyOnce,
            start,
            repeatYear,
            repeatMonth,
            repeatWeek,
            repeatWeekday,
            repeatDay,
            hourOfDay,
            minuteOfDay,
            drugName,
            drugRxcui,
            event_id
        )
    }

    private fun createEventListFromRepeats(
        calendarCurrent: Calendar,
        calendarEnd: Calendar,
        calendarClosestRepeat: Calendar,
        calendarRepeatEnd: Calendar,
        onlyOnce: Boolean,
        start: Date,
        repeatYear: Int,
        repeatMonth: Int,
        repeatWeek: Int,
        repeatWeekday: List<Int>,
        repeatDay: Int,
        hourOfDay: Int,
        minuteOfDay: Int,
        drugName: String,
        drugRxcui: String,
        event_id: String
    ): MutableList<CalendarEvent> {
        val eventList: MutableList<CalendarEvent> = mutableListOf()
        var indexDay = DateUtils.getDaysBetween(start, calendarCurrent.time)
        val repeatWeekdayForCalendarEvent =
            getRepeatWeekdayForCalendarEvent(repeatWeekday)

        while (DateUtils.isDateInRange(calendarCurrent, calendarEnd, calendarRepeatEnd)) {
            val isInRepeat = isDateInRepeat(
                repeatYear, repeatMonth, repeatWeek, repeatWeekday,
                repeatDay, calendarCurrent, calendarClosestRepeat, onlyOnce
            )
            if (isInRepeat) {
                //event is in repeats
                calendarCurrent.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarCurrent.set(Calendar.MINUTE, minuteOfDay)

                val event =
                    CalendarEvent(
                        drugName, drugRxcui, indexDay, calendarCurrent.time, event_id,
                        repeatWeekdayForCalendarEvent, false
                    )
                //todo is taken
                eventList.add(event)
                DateUtils.setCalendarTime(calendarCurrent, 0, 0, 0)
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


    private fun getRepeatWeekdayForCalendarEvent(
        repeatWeekday: List<Int>
    ): String {
        // for knowing the repeat weekdays, to make a good pending intent type
        val repeats: String
        if (repeatWeekday[0] > 0) {
            // repeat week is on
            repeats = repeatWeekday.joinToString(",")
        } else {
            repeats = "0"
        }
        return repeats
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
            (isOnlyRepeat && DateUtils.areDatesEqual(currentDate, calendarClosestRepeat)) -> {
                //  check whether it's a one time event (all the fields are equal to 0)
                isInRepeat = true
            }
            (repeatYear != 0) -> {
                isInRepeat =
                    setRepeatEvent(
                        currentDate,
                        repeatYear,
                        calendarClosestRepeat,
                        Calendar.YEAR
                    )
            }
            (repeatMonth != 0) -> {
                isInRepeat =
                    setRepeatEventMonth(
                        currentDate,
                        repeatMonth,
                        calendarClosestRepeat
                    )

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
        while (DateUtils.isDateBefore(calenderRunFromStartToCurrent, currentDate)) {
            calenderRunFromStartToCurrent.add(Calendar.WEEK_OF_YEAR, repeat)
        }
        if (currentDate.get(Calendar.DAY_OF_WEEK) in daysOfWeek) {
            //  all the days of week in the list are at the same week, so we can add all of them
            isIn = true
        }

        calendarClosestRepeat.timeInMillis = calenderRunFromStartToCurrent.timeInMillis

        return isIn
    }

    private fun setRepeatEventMonth(
        currentDate: Calendar,
        repeat: Int,
        calendarClosestRepeat: Calendar
    ): Boolean {

        var isIn = false
        var skipMonth = repeat
        val dayOfMonth = calendarClosestRepeat.get(Calendar.DAY_OF_MONTH)
        var month = calendarClosestRepeat.get(Calendar.MONTH)

        //run from start
        val calenderRunFromStartToCurrent = Calendar.getInstance()
        calenderRunFromStartToCurrent.timeInMillis = calendarClosestRepeat.timeInMillis
        //temp if there is no day in that month
        val tempRunFromStart = Calendar.getInstance()
        tempRunFromStart.timeInMillis = calendarClosestRepeat.timeInMillis
        var year = calenderRunFromStartToCurrent.get(Calendar.YEAR)

        tempRunFromStart.isLenient = false
        //calenderRunFromStartToCurrent.time < currentDate.time
        while (DateUtils.isDateBefore(calenderRunFromStartToCurrent, currentDate)) {
            tempRunFromStart.set(
                Calendar.DAY_OF_MONTH,
                1
            )// must set to avoid exception on set month
            val monthSet = (month + skipMonth) % 12
            tempRunFromStart.set(Calendar.MONTH, monthSet)
            if (month + skipMonth >= 12) {
                year = year + 1
                tempRunFromStart.set(Calendar.YEAR, year)
                month = (month + skipMonth) % 12
                skipMonth = 0
            }
            tempRunFromStart.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            try {
                tempRunFromStart.time
                //there is a day in this month
                calenderRunFromStartToCurrent.time = tempRunFromStart.time
            } catch (e: Exception) {
                // no such day in month
                tempRunFromStart.time = calenderRunFromStartToCurrent.time
                tempRunFromStart.set(
                    Calendar.YEAR,
                    year
                ) // if was added before, returns to it again
            }
            skipMonth += repeat

        }
        //        calenderRunFromStartToCurrent.time == currentDate.time
        if (DateUtils.areDatesEqual(calenderRunFromStartToCurrent, currentDate)) {
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
        while (DateUtils.isDateBefore(calenderRunFromStartToCurrent, currentDate)) {
            calenderRunFromStartToCurrent.add(addTimeUnit, repeat)
        }
//        calenderRunFromStartToCurrent.time == currentDate.time
        if (DateUtils.areDatesEqual(calenderRunFromStartToCurrent, currentDate)) {
            isIn = true
        }

        calendarClosestRepeat.timeInMillis = calenderRunFromStartToCurrent.timeInMillis

        return isIn
    }


}