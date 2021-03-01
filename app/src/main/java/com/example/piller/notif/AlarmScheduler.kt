package com.example.piller.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.piller.DateUtils
import com.example.piller.R
import com.example.piller.models.DrugOccurrence
import com.example.piller.utilities.DbConstants
import java.util.*
import java.util.Calendar.*
import kotlin.math.ceil


object AlarmScheduler {
    fun runBackgroundService(context: Context, email: String) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = createPendingIntentForAll(context, email)
        alarmMgr.set(AlarmManager.RTC_WAKEUP, 10, alarmIntent)
    }

    private fun createPendingIntentForAll(
        context: Context,
        email: String
    ): PendingIntent? {
        val intent = Intent(context.applicationContext, BackgroundReceiver::class.java).apply {
            // 2
            action = context.getString(R.string.action_notify_medication)
            // 3
            type = email
            // 4
            putExtra(DbConstants.LOGGED_USER_EMAIL, email)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun scheduleAlarmsForReminder(
        context: Context,
        email: String,
        currentProfile: String,
        drug: DrugOccurrence
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm(context, email, currentProfile, drug, alarmMgr)
    }


    private fun createPendingIntent(
        context: Context,
        email: String,
        currentProfile: String,
        drug: DrugOccurrence,
        dayOfWeek: String
    ): PendingIntent? {
        val bundleDrugObject = Bundle()
        bundleDrugObject.putParcelable(DbConstants.DRUG_OBJECT, drug)
        // 1
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            action = context.getString(R.string.action_notify_medication)
            // 3
            type = "${drug.event_id}-${drug.rxcui}-" + dayOfWeek
            // 4
            putExtra(DbConstants.DRUG_OBJECT, bundleDrugObject)
            putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
            putExtra(DbConstants.LOGGED_USER_EMAIL, email)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Schedules a single alarm
     */
    private fun scheduleAlarm(
        context: Context,
        email: String,
        currentProfile: String,
        drug: DrugOccurrence,
        alarmMgr: AlarmManager
    ) {
        val datetimeToAlarm = getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = drug.repeatStart
        datetimeToAlarm.set(MILLISECOND, 0)
        val repeatWeek = drug.repeatWeek.toInt()

        if (repeatWeek != 0) {
            schedualeAllWeekAlarms(
                context,
                email,
                currentProfile,
                drug,
                datetimeToAlarm,
                repeatWeek,
                alarmMgr
            )

        } else {
            val alarmIntent = createPendingIntent(context, email, currentProfile, drug, drug.repeatWeekday)
            setScheduleNotWeekAlarms(drug, alarmMgr, datetimeToAlarm, alarmIntent)
        }
    }

    private fun setScheduleNotWeekAlarms(
        drug: DrugOccurrence,
        alarmMgr: AlarmManager,
        datetimeToAlarm: Calendar,
        alarmIntent: PendingIntent?
    ) {
        if (drug.repeatYear.toInt() == 0 && drug.repeatMonth.toInt() == 0 && drug.repeatDay.toInt() == 0 && drug.repeatWeek.toInt() == 0) {
            //repeat once
            setOnceRepeatScheduleAlarm(alarmMgr, datetimeToAlarm, alarmIntent)
        } else if (drug.repeatMonth.toInt() != 0) {
            //repeatMonth calculated differently
            setScheduleAlarmForMonth(
                drug.repeatMonth.toInt(),
                datetimeToAlarm,
                alarmIntent,
                alarmMgr
            )
        } else if (drug.repeatYear.toInt() != 0) {
            setYearScheduleAlarm(
                drug.repeatYear.toInt(),
                alarmMgr,
                datetimeToAlarm,
                alarmIntent
            )
        } else //if (drug.repeatDay.toInt() != 0) {
        {
            setDayScheduleAlarm(drug.repeatDay.toInt(), alarmMgr, datetimeToAlarm, alarmIntent)
        }
    }


    private fun schedualeAllWeekAlarms(
        context: Context,
        email: String,
        currentProfile: String,
        drug: DrugOccurrence,
        datetimeToAlarm: Calendar,
        repeatWeek: Int,
        alarmMgr: AlarmManager
    ) {
        val days = drug.repeatWeekday.split(",")
        for (day in days) {
            val alarmByDay =
                getTimeClosestByDayWeek(datetimeToAlarm, day.toInt(), repeatWeek)
            val alarmIntent = createPendingIntent(context, email, currentProfile, drug, day) // add day in eac

            //alert every week on weekday
            setWeekScheduleAlarm(repeatWeek, alarmByDay, alarmIntent , alarmMgr)
        }
    }


    private fun setOnceRepeatScheduleAlarm(
        alarmMgr: AlarmManager,
        datetimeToAlarm: Calendar, alarmIntent: PendingIntent?
    ) {
        val current = getInstance()
        if (!DateUtils.isDateBefore(datetimeToAlarm, current)) {
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
        val current = getInstance()
        val nextOccur = getInstance()
        nextOccur.time = datetimeToAlarm.time
        if (DateUtils.isDateBefore(nextOccur, current)) {
            //future alarm
            //get closest notification after current
            val currentYear = current[YEAR]
            if (currentYear == nextOccur[YEAR]) {
                //before but same year
                nextOccur.set(YEAR, currentYear + repeat)
            } else {
                //repeat = 3: 2021-2017=4 , 4/3 ceil= 2=> 2017+ (2*3)=2023
                val addToRepeatGetToCurrYear =
                    (ceil((currentYear - nextOccur[YEAR]) / repeat.toFloat())).toInt()
                val yearToAlarm = (nextOccur[YEAR] + (addToRepeatGetToCurrYear * repeat))
                nextOccur.set(YEAR, yearToAlarm)
                if (DateUtils.isDateBefore(nextOccur, current)) {
                    //select next repeat( same year, but stiil the date is before
                    nextOccur.set(YEAR, nextOccur[YEAR] + repeat)
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
        val current = getInstance()
        val nextOccur = getInstance()
        nextOccur.time = datetimeToAlarm.time
        if (DateUtils.isDateBefore(nextOccur, current)) {
            //future alarm
            //get closest notification after current
            if (current[YEAR] == nextOccur[YEAR] &&
                current[MONTH] == nextOccur[MONTH] &&
                current[DAY_OF_MONTH] == nextOccur[DAY_OF_MONTH]
            ) {
                nextOccur.add(DAY_OF_YEAR, repeat)
            } else {
                val daysBetween = DateUtils.getDaysBetween(nextOccur.time, current.time)
                //repeat = 2: 1 to 4- daysBet=3, 3/2 ceil=2, plus 2*2 days
                val addToRepeatGetToCurrDate =
                    (ceil(daysBetween / repeat.toFloat())).toInt()
                nextOccur.add(DAY_OF_YEAR, (addToRepeatGetToCurrDate * repeat))
            }
            if (DateUtils.isDateBefore(nextOccur, current)) {
                //select next repeat( same year, but stiil the date is before
                nextOccur.add(DAY_OF_YEAR, repeat)
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
        val closestTime = getInstance()
        closestTime.time = datetimeToAlarm.time
        val dayOfWeek = closestTime[DAY_OF_WEEK]
        closestTime.add(DAY_OF_YEAR, wantedDay - dayOfWeek) // in the same week
        if (wantedDay < dayOfWeek) {
            // if we have tusday, and want sunday (need the next repeat)
            closestTime.add(WEEK_OF_YEAR, repeatWeek)
        }
        return closestTime
    }

    private fun setWeekScheduleAlarm(
        repeat: Int,
        alarmByDay: Calendar,
        alarmIntent: PendingIntent?,
        alarmMgr: AlarmManager
    ) {
        val current = getInstance()
        val nextOccur = getInstance()
        nextOccur.time = alarmByDay.time
        if (DateUtils.isDateBefore(nextOccur, current)) {
            //future alarm
            //get closest notification after current
            if (current[YEAR] == nextOccur[YEAR] && current[MONTH] == nextOccur[MONTH] && current[DAY_OF_MONTH] == nextOccur[DAY_OF_MONTH]) {
                // same day but still before
                nextOccur.add(WEEK_OF_YEAR, repeat)
            } else {
                val daysBetween = DateUtils.getDaysBetween(nextOccur.time, current.time)
                val weeksBetween = (ceil(daysBetween / 7.0)).toInt()
                //repeat = 2: 1 to 4- daysBet=3, 3/2 ceil=2, plus 2*2 days
                val addToRepeatGetToCurrDate =
                    (ceil(weeksBetween / repeat.toFloat())).toInt()
                nextOccur.add(WEEK_OF_YEAR, addToRepeatGetToCurrDate * repeat)
            }
            if (DateUtils.isDateBefore(nextOccur, current)) {
                //select next repeat( same year, but stiil the date is before
                nextOccur.add(WEEK_OF_YEAR, repeat)
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
        val dayOfMonth = datetimeToAlarm[DAY_OF_MONTH]
        val nextOccur = getInstance()
        nextOccur.time = datetimeToAlarm.time
        nextOccur.set(DAY_OF_MONTH, 1)
        setMonthToFirstMonthAlarmAfterCurrent(nextOccur, repeat)
        var monthToAlarm = nextOccur[MONTH]
        //check if there is a date in that month
        nextOccur.isLenient = false
        while (!foundDate) {
            //set time
            nextOccur.set(HOUR_OF_DAY, datetimeToAlarm[HOUR_OF_DAY])
            nextOccur.set(MINUTE, datetimeToAlarm[MINUTE])
            nextOccur.set(SECOND, datetimeToAlarm[SECOND])
            nextOccur.set(MILLISECOND, datetimeToAlarm[MILLISECOND])
            nextOccur.set(DAY_OF_MONTH, dayOfMonth)
            try {
                nextOccur.time
                foundDate = true
            } catch (e: Exception) {
                //no such date in month
                nextOccur.set(DAY_OF_MONTH, 1)
                // skip to the month according to the amout of months we could not find date in
                nextOccur.set(MONTH, (monthToAlarm + skipMonth) % 12)
                if (monthToAlarm + skipMonth >= 12) {
                    nextOccur.add(YEAR, 1)
                    monthToAlarm = (monthToAlarm + skipMonth) % 12
                    skipMonth = 0
                }
                skipMonth += repeat
            }
        }

        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextOccur.timeInMillis, alarmIntent)
    }


    private fun setMonthToFirstMonthAlarmAfterCurrent(nextOccur: Calendar, repeat: Int) {
        val currentDate = getInstance()

        // get the closest month to alarm after current day
        var currentMonth = currentDate[MONTH]
        // if curr=3, next=12 -> curr=15, next=12, (15-12)/repeat will give us how many repeats we need to add to get after curr
        if (currentMonth < nextOccur[MONTH]) {
            currentMonth += 12 // month start from 0
        }
        val addToRepeatGetToCurrMonth =
            (ceil((currentMonth - nextOccur[MONTH]) / repeat.toFloat())).toInt()
        val monthToAlarm = (nextOccur[MONTH] + (addToRepeatGetToCurrMonth * repeat)) % 12
        nextOccur.set(MONTH, monthToAlarm)
        nextOccur.set(YEAR, currentDate[YEAR])
    }


    fun removeAlarmsForReminder(
        context: Context,
        drug: DrugOccurrence,
        email: String,
        currentProfile: String
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val days = drug.repeatWeekday.split(",")
        if (days[0].toInt() > 0) {
            //repeat week on
            for (day in days) {
                val alarmIntent = createPendingIntent(context, email, currentProfile, drug, day)
                alarmMgr.cancel(alarmIntent)
            }
        } else {
            val alarmIntent = createPendingIntent(context, email, currentProfile, drug, drug.repeatWeekday)
            alarmMgr.cancel(alarmIntent)
        }


    }

}