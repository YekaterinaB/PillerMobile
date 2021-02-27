package com.example.piller.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.models.CalendarEvent
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
                    // 2
                    val currentProfile = intent.extras!!.getString(DbConstants.LOGGED_USER_NAME)
                    val email = intent.extras!!.getString(DbConstants.LOGGED_USER_EMAIL)
                    val event_id = intent.extras!!.getString(DbConstants.EVENT_ID)
                    // could not get parceble data to intent
                    val bundleCalendarEvent = intent.extras!!.getBundle(DbConstants.DRUG_OBJECT)
                    // 3
                    if (currentProfile != null && email != null
                        && event_id != null && bundleCalendarEvent != null
                    ) {
                        val drug =
                            bundleCalendarEvent.getParcelable<DrugOccurrence>(DbConstants.DRUG_OBJECT)!!
                        NotificationHelper.createNotification(
                            context,
                            drug,
                            event_id,
                            currentProfile,
                            email
                        )

                        if (drug.repeatMonth.toInt() != 0) {
                            //set alarm to next month
                            AlarmScheduler.scheduleAlarmsForReminder(
                                context,
                                email,
                                currentProfile,
                                drug,
                                event_id
                            )
                        }
                    }
                }
            }
        }


    }


}