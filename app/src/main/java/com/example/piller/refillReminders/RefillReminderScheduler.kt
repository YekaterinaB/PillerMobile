package com.example.piller.refillReminders

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
import java.util.Calendar.MILLISECOND
import java.util.Calendar.getInstance


object RefillReminderScheduler {
//    fun runBackgroundService(context: Context, email: String) {
//        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent = createPendingIntentForAll(context, email)
//        alarmMgr.set(AlarmManager.RTC_WAKEUP, 10, alarmIntent)
//    }
//
//    private fun createPendingIntentForAll(
//        context: Context,
//        email: String
//    ): PendingIntent? {
//        val intent = Intent(context.applicationContext, BackgroundReceiver::class.java).apply {
//            // 2
//            action = context.getString(R.string.action_refill_reminder)
//            // 3
//            type = email
//            // 4
//            putExtra(DbConstants.LOGGED_USER_EMAIL, email)
//            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        }
//        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//    }

    fun scheduleAlarmsForReminder(
        context: Context,
        loggedUserObject: UserObject,
        drug: DrugObject
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = getInstance()
        cal[MILLISECOND] = 0
        val calRepeatEnd = getInstance()
        calRepeatEnd.timeInMillis = drug.occurrence.repeatEnd
        calRepeatEnd[MILLISECOND] = 0
        // if today is after the repeat end time, to not notify
        if (!(drug.occurrence.hasRepeatEnd() && DateUtils.isDateBefore(calRepeatEnd, cal))) {
            scheduleAlarm(context, loggedUserObject, drug, alarmMgr)
        }
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
        val timeList = drug.refill.reminderTime.split(":").map { it.toInt() }.toTypedArray()
        val datetimeToAlarm = DateUtils.getFutureHourDate(timeList[0], timeList[1])
        val alarmIntent = createPendingIntent(context, loggedUserObject, drug)
        alarmMgr.set(AlarmManager.RTC_WAKEUP, datetimeToAlarm.timeInMillis, alarmIntent)
    }

    private fun createPendingIntent(
        context: Context, loggedUserObject: UserObject, drug: DrugObject
    ): PendingIntent? {
        val bundleDrugObject = Bundle()
        bundleDrugObject.putParcelable(DbConstants.DRUG_OBJECT, drug)

        // 1
        val intent = Intent(context.applicationContext, RefillReminderReceiver::class.java).apply {
            action = context.getString(R.string.action_refill_reminder)
            // 3
            type = "Refill-${drug.drugId}-${drug.rxcui}-"
            // 4
            putExtra(DbConstants.DRUG_OBJECT, bundleDrugObject)
            val userBundle = Bundle()
            userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, loggedUserObject)
            putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    fun removeAlarmsForReminder(context: Context, drug: DrugObject, loggedUserObject: UserObject) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = createPendingIntent(context, loggedUserObject, drug)
        alarmMgr.cancel(alarmIntent)
    }
}