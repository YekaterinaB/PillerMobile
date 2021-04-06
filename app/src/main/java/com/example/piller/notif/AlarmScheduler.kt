package com.example.piller.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.intakeReminders.IntakeReminderScheduler
import com.example.piller.models.DrugObject
import com.example.piller.refillReminders.RefillReminderScheduler
import com.example.piller.utilities.DbConstants


object AlarmScheduler {

    fun scheduleAllNotifications(
        email: String,
        currentProfile: String,
        context: Context,
        drug: DrugObject
    ) {
        IntakeReminderScheduler.scheduleAlarmsForReminder(
            context, email, currentProfile, drug
        )

        RefillReminderScheduler.scheduleAlarmsForReminder(
            context, email, currentProfile, drug
        )
    }

    fun removeAllNotifications(
        email: String,
        currentProfile: String,
        context: Context,
        drug: DrugObject
    ) {
        IntakeReminderScheduler.removeAlarmsForReminder(
            context, drug, email, currentProfile
        )
        RefillReminderScheduler.removeAlarmsForReminder(
            context, drug, email, currentProfile
        )
    }

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

}