package com.example.piller.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.models.DrugOccurrence
import com.example.piller.utilities.DbConstants


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action != null) {
            // 1
            if (intent.action!!.equals(
                    context.getString(R.string.action_notify_medication),
                    ignoreCase = true
                )
            ) {
                if (intent.extras != null) {
                    handleNewDrugNotification(intent, context)
                }
            }
        }
    }

    private fun handleNewDrugNotification(intent: Intent, context: Context) {
        // 2
        val currentProfile = intent.extras!!.getString(DbConstants.LOGGED_USER_NAME)
        val email = intent.extras!!.getString(DbConstants.LOGGED_USER_EMAIL)
        // could not get parceble data to intent
        val bundleCalendarEvent = intent.extras!!.getBundle(DbConstants.DRUG_OBJECT)
        // 3
        if (currentProfile != null && email != null && bundleCalendarEvent != null) {
            val drug =
                bundleCalendarEvent.getParcelable<DrugOccurrence>(DbConstants.DRUG_OBJECT)!!
            NotificationHelper.createNotification(context, drug, currentProfile, email)


            AlarmScheduler.scheduleAlarmsForReminder(
                context,
                email,
                currentProfile,
                drug
            )

        }
    }
}