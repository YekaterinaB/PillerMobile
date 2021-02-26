package com.example.piller.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.piller.R
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants
import java.util.*
import java.util.Calendar.*

object AlarmScheduler {
    /**
     * Schedules all the alarms for [ReminderData].
     *
     * @param context      current application context
     * @param reminderData ReminderData to use for the alarm
     */
    fun scheduleAlarmsForReminder(context: Context, calendarEventArray: Array<MutableList<CalendarEvent>>,email:String,currentProfile:String) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for(i in 0 until calendarEventArray.size){
            val dayListEvents=calendarEventArray[i]
            if(!dayListEvents.isEmpty()){
                for(event in dayListEvents){
                    val alarmIntent = createPendingIntent(context, event,email,currentProfile)
                    scheduleAlarm(event,alarmIntent,alarmMgr)
                }
            }
        }
    }


    private fun createPendingIntent(context: Context, calendarEvent: CalendarEvent,email:String,currentProfile:String): PendingIntent? {
        val bundleCalenderEvent= Bundle()
        bundleCalenderEvent.putParcelable(DbConstants.CALENDAR_EVENT, calendarEvent)
        // 1
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            // 2
            action = context.getString(R.string.action_notify_medication)
            // 3
            type = "${calendarEvent.intake_time.time}-${calendarEvent.drug_rxcui}"
            // 4

            putExtra(DbConstants.CALENDAR_EVENT, bundleCalenderEvent)
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
    private fun scheduleAlarm(calendarEvent: CalendarEvent, alarmIntent: PendingIntent?, alarmMgr: AlarmManager) {
        // 1
        val dayOfWeek=calendarEvent.index_day
        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = System.currentTimeMillis()
        datetimeToAlarm.set(HOUR_OF_DAY, calendarEvent.intake_time.hours)
        datetimeToAlarm.set(MINUTE, calendarEvent.intake_time.minutes)
        datetimeToAlarm.set(SECOND, 0)
        datetimeToAlarm.set(MILLISECOND, 0)
        datetimeToAlarm.set(DAY_OF_WEEK,dayOfWeek )
        // 2
        val today = Calendar.getInstance(Locale.getDefault())
        if (shouldNotifyToday(dayOfWeek, today, datetimeToAlarm)) {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)
            return
        }
//        // 3 schedule to the right day
//        datetimeToAlarm.roll(WEEK_OF_YEAR, 1)
//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
//            datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)
    }

    /**
     * Determines if the Alarm should be scheduled for today.
     *
     * @param dayOfWeek day of the week as an Int
     * @param today today's datetime
     * @param datetimeToAlarm Alarm's datetime
     */
    private fun shouldNotifyToday(dayOfWeek: Int, today: Calendar, datetimeToAlarm: Calendar): Boolean {
        return dayOfWeek == today.get(DAY_OF_WEEK) &&
                today.get(HOUR_OF_DAY) <= datetimeToAlarm.get(HOUR_OF_DAY) &&
                today.get(MINUTE) <= datetimeToAlarm.get(MINUTE)
    }

    /**
     * Updates a notification.
     * Note: this just calls [AlarmScheduler.scheduleAlarmsForReminder] since
     * alarms with exact matching pending intents will update if they are already set, otherwise
     * call [AlarmScheduler.removeAlarmsForReminder] if the medicine has been administered.
     *
     * @param context      current application context
     * @param reminderData ReminderData for the notification
     */
    fun updateAlarmsForReminder(context: Context, calendarEvent: CalendarEvent) {

            //cancel alarms from settings
//        if (!calendarEvent.administered) {
//            AlarmScheduler.scheduleAlarmsForReminder(context, reminderData)
//        } else {
//            AlarmScheduler.removeAlarmsForReminder(context, reminderData)
//        }
    }

    /**
     * Removes the notification if it was previously scheduled.
     *
     * @param context      current application context
     * @param reminderData ReminderData for the notification
     */
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