package com.example.piller

import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import com.example.piller.models.Occurrence
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.ParserUtils.Companion.parsedDrugObject
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class EventInterpreter {
    private val _everyInterval = 1
    private val _monthsInYear = 12

    fun getEventsForCalendarByDate(
        start: Date,
        end: Date,
        drugList: JSONArray, calendarId: String
    ): Array<MutableList<CalendarEvent>> {
        //  add one because we want to include all the days, e.g. start = monday end = wednesday 
        //  so getDaysBetween will return 2 but we want 3 (monday, tuesday, wednesday)
        val daysBetween = DateUtils.getDaysBetween(start, end) + 1
        val eventList = Array(daysBetween) { mutableListOf<CalendarEvent>() }
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val intakeDates = drug.get(DbConstants.intakeDates) as JSONObject
            val intakes = intakeDates.get(DbConstants.intakes) as JSONArray
            val drugObject = parsedDrugObject(drug, intakeDates, calendarId)
            val drugEventList = getDrugEvent(drugObject, intakes, start, end, calendarId)
            // put all event in array
            if (drugEventList.isNotEmpty()) {
                for (j in 0 until drugEventList.size) {
                    val indexDay = drugEventList[j].indexDay
                    eventList[indexDay].add(drugEventList[j])
                }
            }
        }
        sortEvents(eventList)
        return eventList
    }

    private fun sortEvents(eventList: Array<MutableList<CalendarEvent>>) {
        for (calendarEvents in eventList) {
            calendarEvents.sortBy { it.intakeTime.time }
        }
    }

    private fun getCalendarClosestCurrent(
        start: Date,
        end: Date,
        occurrence: Occurrence
    ): Map<String, Any> {
        val calendarMap = hashMapOf<String, Any>()
        var calendarCurrent = Calendar.getInstance()
        calendarCurrent.time = start

        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = end

        val calendarRepeatEnd = Calendar.getInstance()
        //  the next lines are: if has repeatEnd then set calendarCurrent to repeatEnd
        //  otherwise set it to calendarEnd time
        calendarRepeatEnd.timeInMillis = when (occurrence.hasRepeatEnd()) {
            true -> occurrence.repeatEnd
            false -> calendarEnd.timeInMillis
        }

        // start repeat count
        val calendarStartRepeat = Calendar.getInstance()
        calendarStartRepeat.timeInMillis = occurrence.repeatStart
        //save intake time
        calendarMap[DbConstants.hourOfDay] = calendarStartRepeat.get(Calendar.HOUR_OF_DAY)
        calendarMap[DbConstants.minuteOfDay] = calendarStartRepeat.get(Calendar.MINUTE)
        // start of day at 00:00
        DateUtils.zeroTime(calendarStartRepeat)

        // if the start intake is after start day
        if (DateUtils.isDateBefore(calendarCurrent, calendarStartRepeat)) {
            calendarCurrent = calendarStartRepeat
        }
        calendarMap[DbConstants.calendarRepeatEnd] = calendarRepeatEnd //calendar
        calendarMap[DbConstants.calendarCurrent] = calendarCurrent //calendar
        calendarMap[DbConstants.calendarStartRepeat] = calendarStartRepeat//calendar
        calendarMap[DbConstants.calendarEnd] = calendarEnd
        return calendarMap
    }

    private fun getDrugEvent(
        drugObject: DrugObject,
        intakes: JSONArray,
        start: Date,
        end: Date, calendarId: String
    ): MutableList<CalendarEvent> {
        //find actual start and end times
        val calendarValuesMap = getCalendarClosestCurrent(
            start, end,
            drugObject.occurrence
        )

        // closest repeat is the start time of events for specific drug
        val calendarClosestRepeat = Calendar.getInstance()
        calendarClosestRepeat.timeInMillis =
            (calendarValuesMap[DbConstants.calendarStartRepeat] as Calendar).timeInMillis
        val onlyOnce = drugObject.occurrence.repeatOnce()

        return createEventListFromRepeats(
            drugObject, calendarValuesMap,
            calendarClosestRepeat, onlyOnce, start, intakes, calendarId
        )
    }

    private fun createEventListFromRepeats(
        drugObject: DrugObject, calendarValuesMap: Map<String, Any>,
        calendarClosestRepeat: Calendar, onlyOnce: Boolean,
        start: Date, intakes: JSONArray, calendarId: String
    ): MutableList<CalendarEvent> {
        val eventList: MutableList<CalendarEvent> = mutableListOf()
        val calendarCurrent = calendarValuesMap[DbConstants.calendarCurrent] as Calendar
        val calendarRepeatEnd = calendarValuesMap[DbConstants.calendarRepeatEnd] as Calendar
        var indexDay = DateUtils.getDaysBetween(start, calendarCurrent.time)

        // loop on the range to find events for drug
        while (DateUtils.isDateInRange(
                calendarCurrent,
                calendarValuesMap[DbConstants.calendarEnd] as Calendar, calendarRepeatEnd
            )
        ) {
            //check if date is in the drug schedule
            val isInRepeat = isDateInRepeat(
                drugObject.occurrence,
                calendarValuesMap[DbConstants.calendarCurrent] as Calendar, calendarClosestRepeat,
                onlyOnce
            )
            if (isInRepeat) {
                //event is in repeats
                calendarCurrent.set(
                    Calendar.HOUR_OF_DAY,
                    calendarValuesMap[DbConstants.hourOfDay] as Int
                )
                calendarCurrent.set(
                    Calendar.MINUTE,
                    calendarValuesMap[DbConstants.minuteOfDay] as Int
                )

                val isTaken = intakeStatusOfCalendarEvent(calendarCurrent, intakes)

                //add to cache the drug
                DrugMap.instance.setDrugObject(calendarId, drugObject)
                val event =
                    CalendarEvent(
                        calendarId, drugObject.drugId,
                        indexDay, calendarCurrent.time, calendarRepeatEnd.time, isTaken
                    )

                eventList.add(event)
                DateUtils.zeroTime(calendarCurrent)
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

    private fun intakeStatusOfCalendarEvent(calendar: Calendar, intakesArray: JSONArray): Boolean {
        var result = false
        for (i in 0 until intakesArray.length()) {
            val intakeObject = intakesArray.get(i) as JSONObject
            val dateCal = Calendar.getInstance()
            dateCal.timeInMillis = intakeObject.get(DbConstants.intakeDate) as Long
            if (DateUtils.areDatesEqual(calendar, dateCal)) {
                result = intakeObject.get(DbConstants.isTaken) as Boolean
                break
            }
        }

        return result
    }

    private fun isDateInRepeat(
        occurrence: Occurrence, currentDate: Calendar,
        calendarClosestRepeat: Calendar, isOnlyRepeat: Boolean
    ): Boolean {
        var isInRepeat = false

        when {
            (isOnlyRepeat && DateUtils.areDatesEqual(currentDate, calendarClosestRepeat)) -> {
                //  check whether it's a one time event (all the fields are equal to 0)
                isInRepeat = true
            }
            (occurrence.hasRepeatYear()) -> {
                isInRepeat =
                    isRepeatEvent(
                        currentDate, occurrence.repeatYear, calendarClosestRepeat,
                        Calendar.YEAR
                    )
            }
            (occurrence.hasRepeatMonth()) -> {
                isInRepeat =
                    isRepeatEventMonth(currentDate, occurrence.repeatMonth, calendarClosestRepeat)

            }
            (occurrence.hasRepeatDay()) -> {
                if (occurrence.repeatDay == _everyInterval) {
                    isInRepeat = true
                } else {
                    isInRepeat =
                        isRepeatEvent(
                            currentDate, occurrence.repeatDay, calendarClosestRepeat, Calendar.DATE
                        )
                }
            }
            (occurrence.hasRepeatWeek()) -> {
                if (occurrence.repeatWeek == _everyInterval) {
                    if (currentDate.get(Calendar.DAY_OF_WEEK) in occurrence.repeatWeekday) {
                        //  all the days of week in the list are at the same week, so we can add all of them
                        isInRepeat = true
                    }
                } else {
                    isInRepeat =
                        isRepeatWeekEvent(
                            currentDate, occurrence.repeatWeek, calendarClosestRepeat,
                            occurrence.repeatWeekday
                        )
                }
            }
        }
        return isInRepeat
    }

    private fun isRepeatWeekEvent(
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

    private fun isRepeatEventMonth(
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
            tempRunFromStart.set(Calendar.DAY_OF_MONTH, 1)
            // must set to avoid exception on set month
            val monthSet = (month + skipMonth) % _monthsInYear
            tempRunFromStart.set(Calendar.MONTH, monthSet)
            if (month + skipMonth >= _monthsInYear) {
                year += 1
                tempRunFromStart.set(Calendar.YEAR, year)
                month = (month + skipMonth) % _monthsInYear
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
                // if was added before, returns to it again
                tempRunFromStart.set(Calendar.YEAR, year)
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

    private fun isRepeatEvent(
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
