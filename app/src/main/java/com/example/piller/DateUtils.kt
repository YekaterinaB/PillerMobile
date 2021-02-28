package com.example.piller

import java.util.*

class DateUtils {
    companion object {
        fun areDatesEqual(date1: Calendar, date2: Calendar): Boolean {
            return date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH)
                    && date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
                    && date1.get(Calendar.HOUR_OF_DAY) == date2.get(Calendar.HOUR_OF_DAY)
                    && date1.get(Calendar.MINUTE) == date2.get(Calendar.MINUTE)
        }


        fun isDateBefore(date1: Calendar, date2: Calendar): Boolean {
            if (date1.time < date2.time) {
                return !areDatesEqual(date1, date2)
            }
            return false
        }

        fun setCalendarTime(calendar: Calendar, hour: Int, minutes: Int, seconds: Int) {
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

        fun isDateAfter(date1: Date, date2: Date): Boolean {
            val calendar1 = Calendar.getInstance()
            calendar1.time = date1
            val calendar2 = Calendar.getInstance()
            calendar2.time = date2
            return !isDateBefore(calendar1, calendar2)
        }
    }
}