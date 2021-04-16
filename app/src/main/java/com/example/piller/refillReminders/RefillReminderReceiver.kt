package com.example.piller.refillReminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants


class RefillReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action != null) {
            // 1
            if (intent.action!!.equals(
                    context.getString(R.string.action_refill_reminder),
                    ignoreCase = true
                )
            ) {
                if (intent.extras != null) {
                    handleRefillDrugReminder(intent, context)
                }
            }
        }
    }

    private fun handleRefillDrugReminder(intent: Intent, context: Context) {
        // 2
        val bundle = intent.extras!!.getBundle(DbConstants.LOGGED_USER_BUNDLE)
        val loggedUserObject = bundle?.getParcelable<UserObject>(DbConstants.LOGGED_USER_OBJECT)!!
        // could not get parcelable data to intent
        val bundleCalendarEvent = intent.extras!!.getBundle(DbConstants.DRUG_OBJECT)
        // 3
        if (bundleCalendarEvent != null) {
            val drug =
                bundleCalendarEvent.getParcelable<DrugObject>(DbConstants.DRUG_OBJECT)!!
            if (shouldShowNotifications(context, drug)) {
                RefillReminderHelper.createNotification(
                    context,
                    drug,
                    loggedUserObject
                )
            }
            RefillReminderScheduler.scheduleAlarmsForReminder(context, loggedUserObject, drug)
        }
    }


    private fun shouldShowNotifications(context: Context, drug: DrugObject): Boolean {
        var shouldShow = false
        if (drug.refill.isToNotify && drug.refill.pillsLeft <= drug.refill.pillsBeforeReminder) {
            try {
                shouldShow = AppPreferences.showNotifications
            } catch (e: UninitializedPropertyAccessException) {
                AppPreferences.init(context)
                shouldShow = AppPreferences.showNotifications
            }
        }
        return shouldShow
    }
}