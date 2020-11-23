package com.example.piller

import com.example.piller.models.CalendarEvent
import org.json.JSONArray
import java.util.*
import java.util.concurrent.TimeUnit

class EventInterpreter {

    public fun getEventsForCalendarByDate(
        start: Date,
        end: Date,
        drugList: JSONArray
    ): Array<List<CalendarEvent>> {
        val millionSeconds = end.time - start.time
        val daysBetween = TimeUnit.MILLISECONDS.toDays(millionSeconds) as Int

        var eventList = Array(daysBetween, { emptyList<CalendarEvent>() })
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val drugName = drug.get("drug") as String
            val drugInfo = drug.get("drug_info") as Map<String, String>
            val drugEvent = getDrugEvent(drugName, drugInfo, start, end)
            if (drugEvent != null) {
                val day = drugEvent.index_day
                eventList[day].toMutableList().add(drugEvent)
            }
        }
        return eventList
    }

    private fun getDrugEvent(
        drugName: String,
        drugInfo: Map<String, String>,
        start: Date,
        end: Date
    ): CalendarEvent? {
        val repeatStart = drugInfo.get("repeat_start")
        val repeatYear = (drugInfo.get("repeat_year") as String).split(',')
        val repeatMonth = (drugInfo.get("repeat_month") as String).split(',')
        val repeatDay = (drugInfo.get("repeat_day") as String).split(',')
        val repeatWeek = (drugInfo.get("repeat_week") as String).split(',')
        val repeatWeekday = (drugInfo.get("repeat_weekday") as String).split(',')



        return null
    }

    public fun getFirstDayOfWeek(): Date {
        // get start of this week in milliseconds
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek);
        return cal.time
    }

    public fun getLastDayOfWeek(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return cal.time
    }

    public fun getFirstDayOfMonth(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.time
    }

    public fun getLastDayOfMonth(): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, -1);
        return cal.time
    }
}