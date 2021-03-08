package com.example.piller

import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class EventInterpreter {
    fun getEventsForCalendarByDate(
        start: Date,
        end: Date,
        drugList: JSONArray
    ): Array<MutableList<CalendarEvent>> {
        val daysBetween = DateUtils.getDaysBetween(start, end) + 1
        val eventList = Array(daysBetween) { mutableListOf<CalendarEvent>() }
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val parsedDrugMap = parsedDrugObject(drug)
            val drugEventList =
                getDrugEvent(
                    parsedDrugMap["drugName"] as String, parsedDrugMap["drugRxcui"] as Int,
                    parsedDrugMap["drugInfo"] as JSONObject, start, end,
                    parsedDrugMap["event_id"] as String, parsedDrugMap["taken_id"] as String,
                    parsedDrugMap["intakes"] as JSONArray
                )
            // put all event in array
            if (drugEventList.isNotEmpty()) {
                for (j in 0 until drugEventList.size) {
                    val indexDay = drugEventList[j].index_day
                    eventList[indexDay].add(drugEventList[j])
                }
            }
        }
        return eventList
    }

    private fun parsedDrugObject(drug: JSONObject): Map<String, Any> {
        val parsedDrug = hashMapOf<String, Any>()
        parsedDrug["drugName"] = drug.get("name") as String
        parsedDrug["drugRxcui"] = drug.get("rxcui").toString().toInt()
        parsedDrug["event_id"] = drug.get("event_id").toString()
        parsedDrug["taken_id"] = drug.get("taken_id").toString()
        parsedDrug["intakes"] = drug.get("intakes") as JSONArray
        parsedDrug["drugInfo"] = drug.get("drug_info") as JSONObject
        return parsedDrug
    }

    private fun getParsedRepeatsObject(drugInfo: JSONObject): Map<String, Any> {
        val parsedRepeat = hashMapOf<String, Any>()
        parsedRepeat["repeatStart"] = (drugInfo.get("repeat_start") as String).toLong()
        parsedRepeat["repeatEnd"] = (drugInfo.get("repeat_end") as String).toLong()
        parsedRepeat["repeatYear"] = drugInfo.get("repeat_year") as Int
        parsedRepeat["repeatMonth"] = drugInfo.get("repeat_month") as Int
        parsedRepeat["repeatDay"] = drugInfo.get("repeat_day") as Int
        parsedRepeat["repeatWeek"] = drugInfo.get("repeat_week") as Int
        parsedRepeat["repeatWeekday"] =
            (drugInfo.get("repeat_weekday") as String).split(',').map(String::toInt)
        return parsedRepeat
    }

    private fun getCalendarClosestCurrent(
        start: Date,
        end: Date,
        repeatEnd: Long,
        repeatStart: Long
    ): Map<String, Any> {
        val calendarMap = hashMapOf<String, Any>()
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
        calendarMap["hourOfDay"] = calendarStartRepeat.get(Calendar.HOUR_OF_DAY)
        calendarMap["minuteOfDay"] = calendarStartRepeat.get(Calendar.MINUTE)
        // start of day at 00:00
        DateUtils.setCalendarTime(calendarStartRepeat, 0, 0, 0)

        // if the start intake is after start day
        if (DateUtils.isDateBefore(calendarCurrent, calendarStartRepeat)) {
            calendarCurrent = calendarStartRepeat
        }
        calendarMap["calendarRepeatEnd"] = calendarRepeatEnd //calendar
        calendarMap["calendarCurrent"] = calendarCurrent //calendar
        calendarMap["calendarStartRepeat"] = calendarStartRepeat//calendar
        calendarMap["calendarEnd"] = calendarEnd
        return calendarMap
    }

    private fun getDrugEvent(
        drugName: String,
        drugRxcui: Int,
        drugInfo: JSONObject,
        start: Date,
        end: Date,
        event_id: String,
        taken_id: String,
        intakes: JSONArray
    ): MutableList<CalendarEvent> {
        val parsedRepeat = getParsedRepeatsObject(drugInfo)
        val calendarValuesMap = getCalendarClosestCurrent(
            start,
            end,
            repeatEnd = parsedRepeat["repeatEnd"] as Long,
            repeatStart = parsedRepeat["repeatStart"] as Long
        )

        // check days between actual start date and the new start date
        val calendarClosestRepeat = Calendar.getInstance()
        calendarClosestRepeat.timeInMillis =
            (calendarValuesMap["calendarStartRepeat"] as Calendar).timeInMillis
        val onlyOnce = isOnlyRepeat(
            parsedRepeat["repeatYear"] as Int,
            parsedRepeat["repeatMonth"] as Int,
            parsedRepeat["repeatWeek"] as Int,
            parsedRepeat["repeatDay"] as Int
        )

        return createEventListFromRepeats(
            parsedRepeat, calendarValuesMap, calendarClosestRepeat, onlyOnce,
            start, drugName, drugRxcui, event_id, taken_id, intakes
        )
    }

    private fun createEventListFromRepeats(
        parsedRepeat: Map<String, Any>, calendarValuesMap: Map<String, Any>,
        calendarClosestRepeat: Calendar, onlyOnce: Boolean, start: Date,
        drugName: String, drugRxcui: Int, event_id: String, taken_id: String,
        intakes: JSONArray
    ): MutableList<CalendarEvent> {
        val eventList: MutableList<CalendarEvent> = mutableListOf()
        val calendarCurrent = calendarValuesMap["calendarCurrent"] as Calendar
        val calendarRepeatEnd = calendarValuesMap["calendarRepeatEnd"] as Calendar
        var indexDay = DateUtils.getDaysBetween(start, calendarCurrent.time)
        val repeatWeekdayForCalendarEvent =
            getRepeatWeekdayForCalendarEvent(parsedRepeat["repeatWeekday"] as List<Int>)

        while (DateUtils.isDateInRange(
                calendarCurrent,
                calendarValuesMap["calendarEnd"] as Calendar,
                calendarRepeatEnd
            )
        ) {
            val isInRepeat = isDateInRepeat(
                parsedRepeat,
                calendarValuesMap["calendarCurrent"] as Calendar,
                calendarClosestRepeat,
                onlyOnce
            )
            if (isInRepeat) {
                //event is in repeats
                calendarCurrent.set(Calendar.HOUR_OF_DAY, calendarValuesMap["hourOfDay"] as Int)
                calendarCurrent.set(Calendar.MINUTE, calendarValuesMap["minuteOfDay"] as Int)

                val showTakenCheckbox = DateUtils.isDateBeforeToday(calendarCurrent)
                var isTaken = false
                if (showTakenCheckbox) {
                    isTaken = isDateInIntakeList(calendarCurrent, intakes)
                }
                val event =
                    CalendarEvent(
                        drugName, drugRxcui, indexDay, calendarCurrent.time, event_id, taken_id,
                        repeatWeekdayForCalendarEvent, calendarRepeatEnd.timeInMillis, isTaken
                    )

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

    private fun isDateInIntakeList(calendar: Calendar, intakesArray: JSONArray): Boolean {
        var result = false
        for (i in 0 until intakesArray.length()) {
            val intakeObject = intakesArray.get(i) as JSONObject
            val dateCal = Calendar.getInstance()
            dateCal.timeInMillis = intakeObject.get("date") as Long
            if (DateUtils.areDatesEqual(calendar, dateCal)) {
                result = true
                break
            }
        }

        return result
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
        parsedRepeat: Map<String, Any>,
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
            (parsedRepeat["repeatYear"] as Int != 0) -> {
                isInRepeat =
                    setRepeatEvent(
                        currentDate,
                        parsedRepeat["repeatYear"] as Int,
                        calendarClosestRepeat,
                        Calendar.YEAR
                    )
            }
            (parsedRepeat["repeatMonth"] as Int != 0) -> {
                isInRepeat =
                    setRepeatEventMonth(
                        currentDate,
                        parsedRepeat["repeatMonth"] as Int,
                        calendarClosestRepeat
                    )

            }
            (parsedRepeat["repeatDay"] as Int != 0) -> {
                if (parsedRepeat["repeatDay"] as Int == 1) {
                    isInRepeat = true
                } else {
                    isInRepeat =
                        setRepeatEvent(
                            currentDate,
                            parsedRepeat["repeatDay"] as Int,
                            calendarClosestRepeat,
                            Calendar.DATE
                        )
                }
            }
            (parsedRepeat["repeatWeek"] as Int != 0) -> {
                if (parsedRepeat["repeatWeek"] as Int == 1) {
                    if (currentDate.get(Calendar.DAY_OF_WEEK) in parsedRepeat["repeatWeekday"] as List<*>) {
                        //  all the days of week in the list are at the same week, so we can add all of them
                        isInRepeat = true
                    }
                } else {
                    isInRepeat =
                        setRepeatWeekEvent(
                            currentDate,
                            parsedRepeat["repeatWeek"] as Int,
                            calendarClosestRepeat,
                            parsedRepeat["repeatWeekday"] as List<Int>
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