package com.example.piller.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.piller.R
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugOccurrence
import com.example.piller.utilities.DbConstants
import java.util.*
import java.util.Calendar.*

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
        // 1
        val intent = Intent(context.applicationContext, BackgroundReceiver::class.java).apply {
            // 2
            action = context.getString(R.string.action_notify_medication)
            // 3
            type = email
            // 4
            putExtra(DbConstants.LOGGED_USER_EMAIL, email)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        // 5
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun scheduleAlarmsForReminder(
        context: Context,
        email: String,
        currentProfile: String,
        drug: DrugOccurrence
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = createPendingIntent(context, email, currentProfile, drug)
        scheduleAlarm(drug, alarmIntent, alarmMgr)
    }

    private fun createPendingIntent(
        context: Context,
        email: String,
        currentProfile: String,
        drug: DrugOccurrence
    ): PendingIntent? {
        val bundleDrugObject = Bundle()
        bundleDrugObject.putParcelable(DbConstants.DRUG_OBJECT, drug)
        // 1
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            // 2
            action = context.getString(R.string.action_notify_medication)
            // 3
            type = "${drug.event_id}-${drug.rxcui}"
            // 4
            putExtra(DbConstants.DRUG_OBJECT, bundleDrugObject)
            putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
            putExtra(DbConstants.LOGGED_USER_EMAIL, email)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        // 5
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Schedules a single alarm
     */
    private fun scheduleAlarm(
        drug: DrugOccurrence,
        alarmIntent: PendingIntent?,
        alarmMgr: AlarmManager
    ) {
        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = drug.repeatStart
        if (drug.repeatYear.toInt() == 0 && drug.repeatMonth.toInt() == 0 && drug.repeatDay.toInt() == 0 && drug.repeatWeek.toInt() == 0) {
            //repeat once
            alarmMgr.set(AlarmManager.RTC_WAKEUP, datetimeToAlarm.timeInMillis, alarmIntent)
        } else if (drug.repeatMonth.toInt() != 0) {
            //repeatMonth calculated differently
            val nextOccur = getInstance()
            setScheduleAlarmForMonth(drug, nextOccur, datetimeToAlarm, alarmIntent, alarmMgr)
        } else { // repeat year,week,day
            setScheduleAlarmForYear_Week_Day(drug, datetimeToAlarm, alarmIntent, alarmMgr)
        }
    }

    private fun setScheduleAlarmForMonth(
        drug: DrugOccurrence, nextOccur: Calendar,
        datetimeToAlarm: Calendar, alarmIntent: PendingIntent?,
        alarmMgr: AlarmManager
    ) {

        val dayOfMonth = datetimeToAlarm[DAY_OF_MONTH]
        if (isDateToNotifyPassedInMonth(datetimeToAlarm, nextOccur)) {

            //get nextMonth
            nextOccur.set(DAY_OF_MONTH, drug.repeatMonth.toInt())
            nextOccur.add(Calendar.MONTH, 1)
        }
        //set time
        nextOccur.set(HOUR_OF_DAY, datetimeToAlarm[HOUR_OF_DAY])
        nextOccur.set(MINUTE, datetimeToAlarm[MINUTE])
        nextOccur.set(SECOND, datetimeToAlarm[SECOND])
        nextOccur.set(MILLISECOND, datetimeToAlarm[MILLISECOND])
        nextOccur.set(DAY_OF_MONTH, dayOfMonth)
        nextOccur.isLenient = false
        try {
            nextOccur.time
        } catch (e: Exception) {
            //no such date in month
            nextOccur.set(DAY_OF_MONTH, 1)
            nextOccur.add(Calendar.MONTH, drug.repeatMonth.toInt())
            setScheduleAlarmForMonth(drug, nextOccur, datetimeToAlarm, alarmIntent, alarmMgr)
        }
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextOccur.timeInMillis, alarmIntent)
    }

    private fun isDateToNotifyPassedInMonth(datetimeToAlarm: Calendar, today: Calendar): Boolean {
        var result = false
        if (today[DAY_OF_MONTH] > datetimeToAlarm[DAY_OF_MONTH]) {
            result = true
        } else if (today[DAY_OF_MONTH] == datetimeToAlarm[DAY_OF_MONTH]) {
            if (today[HOUR_OF_DAY] > datetimeToAlarm[HOUR_OF_DAY]) {
                result = true
            } else if (today[HOUR_OF_DAY] == datetimeToAlarm[HOUR_OF_DAY]) {
                if (today[MINUTE] > datetimeToAlarm[MINUTE]) {
                    result = true
                } else if (today[MINUTE] == datetimeToAlarm[MINUTE]) {
                    if (today[SECOND] > datetimeToAlarm[SECOND]) {
                        result = true
                    } else if (today[SECOND] == datetimeToAlarm[SECOND]) {
                        if (today[MILLISECOND] >= datetimeToAlarm[MILLISECOND]) {
                            result = true
                        }
                    }
                }
            }
        }
        return result
    }

    private fun setScheduleAlarmForYear_Week_Day(
        drug: DrugOccurrence, datetimeToAlarm: Calendar, alarmIntent: PendingIntent?,
        alarmMgr: AlarmManager
    ) {
        if (drug.repeatWeekday.split(',').size > 1) {
            // more than one weekday
            val days = drug.repeatWeekday.split(",")
            for (day in days) {
                val dateOfWeek = getRecentDateWithWeekday(day.toInt(), datetimeToAlarm.time)
                //alert every week on weekday
                alarmMgr.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    dateOfWeek.time, 1000 * 60 * 60 * 24 * 7 * drug.repeatWeek.toLong(), alarmIntent
                )
            }
        } else {
            val interval = getIntervalRepeat(drug)
            alarmMgr.setRepeating(
                AlarmManager.RTC_WAKEUP,
                datetimeToAlarm.timeInMillis, interval, alarmIntent
            )
        }
    }

    private fun getRecentDateWithWeekday(dayOfWeek: Int, date: Date): Date {
        val temp = getInstance()
        temp.time = date
        if (temp[DAY_OF_MONTH] != dayOfWeek) {
            temp.add(DAY_OF_MONTH, (dayOfWeek + 7 - temp[DAY_OF_WEEK]) % 7)
        }
        return temp.time
    }

    private fun getIntervalRepeat(drug: DrugOccurrence): Long {
        val interval: Long
        if (drug.repeatYear.toInt() != 0) {
            //repeatYear is set
            interval = drug.repeatYear.toLong() * 1000 * 60 * 60 * 24 * 365.toLong()
        } else if (drug.repeatDay.toInt() != 0) {
            interval = drug.repeatDay.toLong() * 1000 * 60 * 60 * 24.toLong()
        } else //if (drug.repeatWeek.toInt() != 0)
        {
            interval = drug.repeatWeek.toLong() * 1000 * 60 * 60 * 24 * 7.toLong()
        }
        return interval
    }

    fun updateAlarmsForReminder(context: Context, calendarEvent: CalendarEvent) {

        //cancel alarms from settings
//        if (!calendarEvent.administered) {
//            AlarmScheduler.scheduleAlarmsForReminder(context, reminderData)
//        } else {
//            AlarmScheduler.removeAlarmsForReminder(context, reminderData)
//        }
    }

//    fun removeAlarmsForReminder(context: Context, reminderData: ReminderData) {
//        val intent = Intent(context.applicationContext, AlarmReceiver::class.java)
//        intent.action = context.getString(R.string.action_notify_administer_medication)
//        intent.putExtra(ReminderDialog.KEY_ID, reminderData.id)
//
//        // type must be unique so Intent.filterEquals passes the check to make distinct PendingIntents
//        // Schedule the alarms based on the days to administer the medicine
//        if (reminderData.days != null) {
//            for (i in reminderData.days!!.indices) {
//                val day = reminderData.days!![i]
//
//                if (day != null) {
//                    val type = String.format(Locale.getDefault(), "%s-%s-%s-%s", day, reminderData.name, reminderData.medicine, reminderData.type.name)
//
//                    intent.type = type
//                    val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//                    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                    alarmMgr.cancel(alarmIntent)
//                }
//            }
//        }
//    }

}