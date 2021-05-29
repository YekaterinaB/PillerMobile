package com.example.piller.utilities

import java.util.*
import java.util.concurrent.TimeUnit

class DateUtils {
    companion object {
        fun getDaysBetween(first: Date, second: Date): Int { // between 1 to 31 => 30
            val firstCal = Calendar.getInstance()
            firstCal.time = first
            zeroTime(firstCal)
            val secondCal = Calendar.getInstance()
            secondCal.time = second
            zeroTime(secondCal)

            val diff: Long = secondCal.timeInMillis - firstCal.timeInMillis
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        }

        fun zeroTime(calendar: Calendar) {
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
        }

        fun getDayOfWeekNumber(): Int {
            val cal = Calendar.getInstance()
            return cal[Calendar.DAY_OF_WEEK]
        }

        fun getDayNumberForCurrentWeek(): List<Int> {
            val cal = Calendar.getInstance()
            val stringDates = mutableListOf<Int>()
            cal.time = getFirstDayOfWeek()
            for (i in 0 until 7) {
                stringDates.add(cal[Calendar.DAY_OF_MONTH])
                cal.add(Calendar.DATE, 1)
            }

            return stringDates
        }

        fun getCurrentWeekDayNumber(): Int {
            val cal = Calendar.getInstance()
            return cal[Calendar.DAY_OF_WEEK]
        }

        fun areDatesEqual(date1: Calendar, date2: Calendar): Boolean {
            return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
                    && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH)
                    && date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
                    && date1.get(Calendar.HOUR_OF_DAY) == date2.get(Calendar.HOUR_OF_DAY)
                    && date1.get(Calendar.MINUTE) == date2.get(Calendar.MINUTE)
                    && date1.get(Calendar.SECOND) == date2.get(Calendar.SECOND)
        }

        fun isDateBeforeToday(date1: Calendar): Boolean {
            val today = Calendar.getInstance()
            setToLastTimeOfDay(today)
            return isDateBefore(date1, today)
        }

        fun setToLastTimeOfDay(calendar: Calendar) {
            setCalendarTime(calendar, 23, 59, 59)
        }

        fun getFutureHourDate(hour: Int, minutes: Int): Calendar {
            val futureDate = Calendar.getInstance()
            if ((futureDate.get(Calendar.HOUR_OF_DAY) > hour) ||
                !(futureDate.get(Calendar.HOUR_OF_DAY) == hour && futureDate.get(Calendar.MINUTE) < minutes)
            ) {
                //now is after the time
                futureDate.add(Calendar.DATE, 1)
            }
            setCalendarTime(futureDate, hour, minutes)

            return futureDate
        }

        fun isDateBefore(date1: Calendar, date2: Calendar): Boolean {
            if (date1.time < date2.time) {
                return !areDatesEqual(date1, date2)
            }
            return false
        }

        fun isDateBefore(date1: Date, date2: Date): Boolean {
            val calDate1 = Calendar.getInstance()
            calDate1.time = date1
            val calDate2 = Calendar.getInstance()
            calDate2.time = date2
            if (date1.time < date2.time) {
                return !areDatesEqual(calDate1, calDate2)
            }
            return false
        }


        fun setCalendarTime(calendar: Calendar, hour: Int, minutes: Int, seconds: Int = 0) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minutes)
            calendar.set(Calendar.SECOND, seconds)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        fun getDayAfterInMillis(year: Int, month: Int, day: Int): Long {
            val cal: Calendar = Calendar.getInstance()
            cal.set(year, month, day)
            return getTomorrowDateInMillis(cal.time)
        }

        fun getTomorrowDateInMillis(startDate: Date): Long {
            // get start of this week in milliseconds
            val cal: Calendar = Calendar.getInstance()
            cal.time = startDate
            cal.add(Calendar.DATE, 1)
            zeroTime(cal)
            return cal.timeInMillis
        }

        fun getFirstDayOfWeek(): Date {
            // get start of this week in milliseconds
            val cal: Calendar = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            zeroTime(cal)
            return cal.time
        }

        fun getLastDayOfWeek(): Date {
            val cal: Calendar = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            setToLastTimeOfDay(cal)
            return cal.time
        }

        fun getFirstDayOfSpecificMonth(date: Date): Date {
            val cal: Calendar = Calendar.getInstance()
            cal.time = date
            //  set time to 00:00:00
            cal.set(Calendar.DAY_OF_MONTH, 1)
            zeroTime(cal)
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
            setToLastTimeOfDay(cal)
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
            return isDateAfter(calendar1, calendar2)
        }

        fun isDateAfter(calendar1: Calendar, calendar2: Calendar): Boolean {
            return !isDateBefore(calendar1, calendar2)
        }

        fun isDateInRange(
            calendarCurrent: Calendar,
            calendarEnd: Calendar,
            calendarRepeatEnd: Calendar
        ): Boolean {
            return (isDateBefore(calendarCurrent, calendarEnd)
                    && isDateBefore(calendarCurrent, calendarRepeatEnd))
                    || areDatesEqual(calendarCurrent, calendarEnd)
        }
    }
}