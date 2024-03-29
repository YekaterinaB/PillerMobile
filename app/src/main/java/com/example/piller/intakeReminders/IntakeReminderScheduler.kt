package com.example.piller.intakeReminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.piller.R
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import java.util.*
import kotlin.math.ceil

object IntakeReminderScheduler {
    private const val monthsInYear = 12
    private const val startOfMonthDay = 1
    private const val startOfYearDay = 1

    fun scheduleAlarmsForReminder(
        context: Context,
        loggedUserObject: UserObject,
        drug: DrugObject
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance()
        //  set seconds and ms to 0
        DateUtils.setCalendarTime(cal, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE])
        val calRepeatEnd = Calendar.getInstance()
        calRepeatEnd.timeInMillis = drug.occurrence.repeatEnd
        //  set seconds and ms to 0
        DateUtils.setCalendarTime(
            calRepeatEnd,
            calRepeatEnd[Calendar.HOUR_OF_DAY],
            calRepeatEnd[Calendar.MINUTE]
        )
        // if today is after the repeat end time, do not notify
        if (!(drug.occurrence.hasRepeatEnd() && DateUtils.isDateBefore(calRepeatEnd, cal))) {
            scheduleAlarm(context, loggedUserObject, drug, alarmMgr)
        }
    }

    private fun createPendingIntent(
        context: Context,
        loggedUserObject: UserObject,
        drug: DrugObject,
        dayOfWeek: String
    ): PendingIntent? {
        val bundleDrugObject = Bundle()
        bundleDrugObject.putParcelable(DbConstants.DRUG_OBJECT, drug)

        // 1
        val intent = Intent(context.applicationContext, IntakeReminderReceiver::class.java).apply {
            action = context.getString(R.string.action_notify_medication)
            // 3
            type = context.getString(
                R.string.intakeReminderPendingType,
                drug.drugId,
                drug.rxcui,
                dayOfWeek
            )
            // 4
            putExtra(DbConstants.DRUG_OBJECT, bundleDrugObject)
            val userBundle = Bundle()
            userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, loggedUserObject)
            putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getBroadcast(
            context,
            DbConstants.pendingIntentRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Schedules a single alarm
     */
    private fun scheduleAlarm(
        context: Context,
        loggedUserObject: UserObject,
        drug: DrugObject,
        alarmMgr: AlarmManager
    ) {
        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = drug.occurrence.repeatStart
        DateUtils.setCalendarTime(
            datetimeToAlarm,
            datetimeToAlarm[Calendar.HOUR_OF_DAY],
            datetimeToAlarm[Calendar.MINUTE]
        )
        if (drug.occurrence.hasRepeatWeek()) {
            schedualeAllWeekAlarms(
                context,
                loggedUserObject,
                drug,
                datetimeToAlarm,
                drug.occurrence.repeatWeek,
                alarmMgr
            )
        } else {
            val alarmIntent =
                createPendingIntent(context, loggedUserObject, drug, DbConstants.noDayOfWeekStr)
            setScheduleNotWeekAlarms(drug, alarmMgr, datetimeToAlarm, alarmIntent)
        }
    }

    private fun setScheduleNotWeekAlarms(
        drug: DrugObject,
        alarmMgr: AlarmManager,
        datetimeToAlarm: Calendar,
        alarmIntent: PendingIntent?
    ) {
        val occurrence = drug.occurrence
        if (!occurrence.hasRepeatYear() && !occurrence.hasRepeatMonth() &&
            !occurrence.hasRepeatDay() && !occurrence.hasRepeatWeek()
        ) {
            //repeat once
            setOnceRepeatScheduleAlarm(alarmMgr, datetimeToAlarm, alarmIntent)
        } else if (occurrence.hasRepeatMonth()) {
            //repeatMonth calculated differently
            setScheduleAlarmForMonth(
                occurrence.repeatMonth,
                datetimeToAlarm,
                alarmIntent,
                alarmMgr
            )
        } else if (occurrence.hasRepeatYear()) {
            setYearScheduleAlarm(
                occurrence.repeatYear,
                alarmMgr,
                datetimeToAlarm,
                alarmIntent
            )
        } else //if (drug.hasRepeatDay()) {
        {
            setDayScheduleAlarm(occurrence.repeatDay, alarmMgr, datetimeToAlarm, alarmIntent)
        }
    }


    private fun schedualeAllWeekAlarms(
        context: Context,
        loggedUserObject: UserObject,
        drug: DrugObject,
        datetimeToAlarm: Calendar,
        repeatWeek: Int,
        alarmMgr: AlarmManager
    ) {
        val days = drug.occurrence.repeatWeekday
        for (day in days) {
            val alarmByDay = getTimeClosestByDayWeek(datetimeToAlarm, day, repeatWeek)
            // add day in eac
            val alarmIntent = createPendingIntent(context, loggedUserObject, drug, day.toString())

            //alert every week on weekday
            setWeekScheduleAlarm(repeatWeek, alarmByDay, alarmIntent, alarmMgr)
        }
    }

    private fun setOnceRepeatScheduleAlarm(
        alarmMgr: AlarmManager,
        datetimeToAlarm: Calendar, alarmIntent: PendingIntent?
    ) {
        val current = Calendar.getInstance()
        if (DateUtils.isDateBefore(current, datetimeToAlarm)) {
            //future alarm
            alarmMgr.set(AlarmManager.RTC_WAKEUP, datetimeToAlarm.timeInMillis, alarmIntent)
        }
    }

    private fun setYearScheduleAlarm(
        repeat: Int,
        alarmMgr: AlarmManager,
        datetimeToAlarm: Calendar,
        alarmIntent: PendingIntent?
    ) {
        val current = Calendar.getInstance()
        val nextOccur = Calendar.getInstance()
        nextOccur.time = datetimeToAlarm.time
        if (DateUtils.isDateBefore(nextOccur, current)) {
            //future alarm
            //get closest notification after current
            val currentYear = current[Calendar.YEAR]
            if (currentYear == nextOccur[Calendar.YEAR]) {
                //before but same year
                nextOccur.set(Calendar.YEAR, currentYear + repeat)
            } else {
                //repeat = 3: 2021-2017=4 , 4/3 ceil= 2=> 2017+ (2*3)=2023
                val addToRepeatGetToCurrYear =
                    (ceil((currentYear - nextOccur[Calendar.YEAR]) / repeat.toFloat())).toInt()
                val yearToAlarm = (nextOccur[Calendar.YEAR] + (addToRepeatGetToCurrYear * repeat))
                nextOccur.set(Calendar.YEAR, yearToAlarm)
                if (DateUtils.isDateBefore(nextOccur, current)) {
                    //select next repeat( same year, but still the date is before
                    nextOccur.set(Calendar.YEAR, nextOccur[Calendar.YEAR] + repeat)
                }
            }

        }
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextOccur.timeInMillis, alarmIntent)
    }

    private fun setDayScheduleAlarm(
        repeat: Int,
        alarmMgr: AlarmManager,
        datetimeToAlarm: Calendar,
        alarmIntent: PendingIntent?
    ) {
        val current = Calendar.getInstance()
        val nextOccur = Calendar.getInstance()
        nextOccur.time = datetimeToAlarm.time
        if (DateUtils.isDateBefore(nextOccur, current)) {
            //future alarm
            //get closest notification after current
            if (current[Calendar.YEAR] == nextOccur[Calendar.YEAR] &&
                current[Calendar.MONTH] == nextOccur[Calendar.MONTH] &&
                current[Calendar.DAY_OF_MONTH] == nextOccur[Calendar.DAY_OF_MONTH]
            ) {
                nextOccur.add(Calendar.DAY_OF_YEAR, repeat)
            } else {
                val daysBetween = DateUtils.getDaysBetween(nextOccur.time, current.time)
                //repeat = 2: 1 to 4- daysBet=3, 3/2 ceil=2, plus 2*2 days
                val addToRepeatGetToCurrDate =
                    (ceil(daysBetween / repeat.toFloat())).toInt()
                nextOccur.add(Calendar.DAY_OF_YEAR, (addToRepeatGetToCurrDate * repeat))
            }
            if (DateUtils.isDateBefore(nextOccur, current)) {
                //select next repeat( same year, but still the date is before
                nextOccur.add(Calendar.DAY_OF_YEAR, repeat)
            }
        }
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextOccur.timeInMillis, alarmIntent)
    }

    private fun getTimeClosestByDayWeek(
        datetimeToAlarm: Calendar,
        wantedDay: Int,
        repeatWeek: Int
    ): Calendar {
        // get the wanted day after the datetime
        val closestTime = Calendar.getInstance()
        closestTime.time = datetimeToAlarm.time
        val dayOfWeek = closestTime[Calendar.DAY_OF_WEEK]
        closestTime.add(Calendar.DAY_OF_YEAR, wantedDay - dayOfWeek) // in the same week
        if (wantedDay < dayOfWeek) {
            // if we have tuesday, and want sunday (need the next repeat)
            closestTime.add(Calendar.WEEK_OF_YEAR, repeatWeek)
        }
        return closestTime
    }

    private fun setWeekScheduleAlarm(
        repeat: Int,
        alarmByDay: Calendar,
        alarmIntent: PendingIntent?,
        alarmMgr: AlarmManager
    ) {
        val current = Calendar.getInstance()
        val nextOccur = Calendar.getInstance()
        nextOccur.time = alarmByDay.time
        if (DateUtils.isDateBefore(nextOccur, current)) {
            //future alarm
            //get closest notification after current
            if (current[Calendar.YEAR] == nextOccur[Calendar.YEAR] && current[Calendar.MONTH] == nextOccur[Calendar.MONTH]
                && current[Calendar.DAY_OF_MONTH] == nextOccur[Calendar.DAY_OF_MONTH]
            ) {
                // same day but still before
                nextOccur.add(Calendar.WEEK_OF_YEAR, repeat)
            } else {
                val daysBetween = DateUtils.getDaysBetween(nextOccur.time, current.time)
                val weeksBetween =
                    (ceil(daysBetween / DbConstants.numberOfDaysAWeekInCalendar)).toInt()
                //repeat = 2: 1 to 4- daysBet=3, 3/2 ceil=2, plus 2*2 days
                val addToRepeatGetToCurrDate =
                    (ceil(weeksBetween / repeat.toFloat())).toInt()
                nextOccur.add(Calendar.WEEK_OF_YEAR, addToRepeatGetToCurrDate * repeat)
            }
            if (DateUtils.isDateBefore(nextOccur, current)) {
                //select next repeat( same year, but still the date is before
                nextOccur.add(Calendar.WEEK_OF_YEAR, repeat)
            }
        }
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextOccur.timeInMillis, alarmIntent)
    }

    private fun setScheduleAlarmForMonth(
        repeat: Int, datetimeToAlarm: Calendar, alarmIntent: PendingIntent?,
        alarmMgr: AlarmManager
    ) {
        var foundDate = false
        var skipMonth = repeat // to
        val dayOfMonth = datetimeToAlarm[Calendar.DAY_OF_MONTH]
        val nextOccur = Calendar.getInstance()
        nextOccur.time = datetimeToAlarm.time
        nextOccur.set(Calendar.DAY_OF_MONTH, startOfMonthDay)
        setMonthToFirstMonthAlarmAfterCurrent(nextOccur, repeat)
        var monthToAlarm = nextOccur[Calendar.MONTH]
        //check if there is a date in that month
        nextOccur.isLenient = false
        while (!foundDate) {
            //set time
            nextOccur.set(Calendar.HOUR_OF_DAY, datetimeToAlarm[Calendar.HOUR_OF_DAY])
            nextOccur.set(Calendar.MINUTE, datetimeToAlarm[Calendar.MINUTE])
            nextOccur.set(Calendar.SECOND, datetimeToAlarm[Calendar.SECOND])
            nextOccur.set(Calendar.MILLISECOND, datetimeToAlarm[Calendar.MILLISECOND])
            nextOccur.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            try {
                nextOccur.time
                foundDate = true
            } catch (e: Exception) {
                //no such date in month
                nextOccur.set(Calendar.DAY_OF_MONTH, startOfMonthDay)
                // skip to the month according to the amount of months we could not find date in
                nextOccur.set(Calendar.MONTH, (monthToAlarm + skipMonth) % monthsInYear)
                if (monthToAlarm + skipMonth >= monthsInYear) {
                    nextOccur.add(Calendar.YEAR, startOfYearDay)
                    monthToAlarm = (monthToAlarm + skipMonth) % monthsInYear
                    skipMonth = 0
                }
                skipMonth += repeat
            }
        }

        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextOccur.timeInMillis, alarmIntent)
    }


    private fun setMonthToFirstMonthAlarmAfterCurrent(nextOccur: Calendar, repeat: Int) {
        val currentDate = Calendar.getInstance()

        // get the closest month to alarm after current day
        var currentMonth = currentDate[Calendar.MONTH]
        // if curr=3, next=12 -> curr=15, next=12, (15-12)/repeat will give us how many repeats we need to add to get after curr
        if (currentMonth < nextOccur[Calendar.MONTH]) {
            currentMonth += monthsInYear // month start from 0
        }
        val addToRepeatGetToCurrMonth =
            (ceil((currentMonth - nextOccur[Calendar.MONTH]) / repeat.toFloat())).toInt()
        val monthToAlarm =
            (nextOccur[Calendar.MONTH] + (addToRepeatGetToCurrMonth * repeat)) % monthsInYear
        nextOccur.set(Calendar.MONTH, monthToAlarm)
        nextOccur.set(Calendar.YEAR, currentDate[Calendar.YEAR])
    }


    fun removeAlarmsForReminder(
        context: Context,
        drug: DrugObject,
        loggedUserObject: UserObject
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (drug.occurrence.hasRepeatWeekday()) {
            //repeat week on
            for (day in drug.occurrence.repeatWeekday) {
                val alarmIntent =
                    createPendingIntent(context, loggedUserObject, drug, day.toString())
                alarmMgr.cancel(alarmIntent)
            }
        } else {
            val alarmIntent =
                createPendingIntent(context, loggedUserObject, drug, DbConstants.noDayOfWeekStr)
            alarmMgr.cancel(alarmIntent)
        }
    }
}