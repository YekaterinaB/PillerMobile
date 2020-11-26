package com.example.piller

import com.example.piller.models.CalendarEvent
import com.google.gson.JsonArray
import com.squareup.moshi.Json
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class EventInterpreter {

    fun getEventsForCalendarByDate(
        start: Date,
        end: Date,
        drugList: JSONArray
    ): Array<MutableList<CalendarEvent>> {
        val daysBetween = getDaysBetween(start, end) + 1
        var eventList = Array(daysBetween, { mutableListOf<CalendarEvent>() })
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val drugName = drug.get("drug") as String
            val drugInfo = drug.get("drug_info") as JSONObject
            val drugEventList = getDrugEvent(drugName, drugInfo, start, end)
            // put all event in array
            if (!drugEventList.isEmpty()) {
                for (j in 0 until drugEventList.size) {
                    val indexDay = drugEventList[j].index_day
                    eventList[indexDay].add(drugEventList[j])
                }
            }
        }
        return eventList
    }

    private fun getDaysBetween(first: Date, second: Date): Int {
        val calendarFirst = Calendar.getInstance()
        calendarFirst.time = first

        val calendarSecond = Calendar.getInstance()
        calendarSecond.time = second
        return calendarSecond.get(Calendar.DAY_OF_WEEK) - calendarFirst.get(Calendar.DAY_OF_WEEK)
    }

    private fun getDrugEvent(
        drugName: String,
        drugInfo: JSONObject,
        start: Date,
        end: Date
    ): MutableList<CalendarEvent> {
        val eventList: MutableList<CalendarEvent> = mutableListOf()
        val repeatStart = (drugInfo.get("repeat_start") as String).toLong()
        val repeatYear = (drugInfo.get("repeat_year") as String).split(',').map(String::toInt)
        val repeatMonth = (drugInfo.get("repeat_month") as String).split(',').map(String::toInt)
        val repeatDay = (drugInfo.get("repeat_day") as String).split(',').map(String::toInt)
        val repeatWeek = (drugInfo.get("repeat_week") as String).split(',').map(String::toInt)
        val repeatWeekday = (drugInfo.get("repeat_weekday") as String).split(',').map(String::toInt)
        var calendarCurrent = Calendar.getInstance()
        calendarCurrent.time = start
        val calendarStartRepeat = Calendar.getInstance()

        // if the start intake is after start day
        calendarStartRepeat.setTimeInMillis(repeatStart)
        if (calendarCurrent.time.before(calendarStartRepeat.time)) {
            calendarCurrent = calendarStartRepeat
        }

        // check days between actual start date and the new start date
        var indexDay = getDaysBetween(start, calendarCurrent.time)

        while (calendarCurrent.time < end) {
            val dayOfWeek: Int = calendarCurrent.get(Calendar.DAY_OF_WEEK)
            val numberOfWeek: Int = calendarCurrent.get(Calendar.WEEK_OF_MONTH)
            val dayOfMonth: Int = calendarCurrent.get(Calendar.DAY_OF_MONTH)
            val monthOfYear: Int = calendarCurrent.get(Calendar.MONTH)
            val year: Int = calendarCurrent.get(Calendar.YEAR)
            if (isInRepeat(year, repeatYear) && isInRepeat(monthOfYear, repeatMonth) &&
                isInRepeat(dayOfMonth, repeatDay) && isInRepeat(numberOfWeek, repeatWeek) &&
                isInRepeat(dayOfWeek, repeatWeekday)
            ) {
                //event is in repeats
                val event = CalendarEvent(drugName, indexDay, calendarCurrent.time, false)
                //todo is taken
                eventList.add(event)
            }

            calendarCurrent.add(Calendar.DATE, 1)
            indexDay += 1
        }
        return eventList
    }

    private fun isInRepeat(current: Int, repeat: List<Int>): Boolean {
        var isIn = false
        for (i in 0 until repeat.size) {
            if (repeat[i] == -1 || repeat[i] == current) {
                isIn = true
                break
            }
        }
        return isIn
    }

    fun getFirstDayOfWeek(): Date {
        // get start of this week in milliseconds
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek);
        return cal.time
    }

    fun getLastDayOfWeek(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return cal.time
    }

    fun getFirstDayOfMonth(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.time
    }

    fun getLastDayOfMonth(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, -1);
        return cal.time
    }
}