package com.example.piller.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.intakeReminders.IntakeReminderScheduler
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
import com.example.piller.refillReminders.RefillReminderScheduler


object AlarmScheduler {

    fun scheduleAllNotifications(loggedUserObject: UserObject, context: Context, drug: DrugObject) {
        IntakeReminderScheduler.scheduleAlarmsForReminder(context, loggedUserObject, drug)
        RefillReminderScheduler.scheduleAlarmsForReminder(context, loggedUserObject, drug)
    }

    fun removeAllNotifications(loggedUserObject: UserObject, context: Context, drug: DrugObject) {
        IntakeReminderScheduler.removeAlarmsForReminder(context, drug, loggedUserObject)
        RefillReminderScheduler.removeAlarmsForReminder(context, drug, loggedUserObject)
    }

    fun runBackgroundService(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = createPendingIntentForAll(context)
        alarmMgr.set(AlarmManager.RTC_WAKEUP, 10, alarmIntent)
    }

    private fun createPendingIntentForAll(context: Context): PendingIntent? {
        val intent = Intent(context.applicationContext, BackgroundReceiver::class.java).apply {
            // 2
            action = context.getString(R.string.action_notify_medication)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}