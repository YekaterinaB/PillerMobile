package com.example.piller.refillReminders

import com.example.piller.notif.AlarmScheduler
import com.example.piller.notif.NotificationHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.DrugIntakeAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.*


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
        val currentProfile = intent.extras!!.getString(DbConstants.LOGGED_USER_NAME)
        val email = intent.extras!!.getString(DbConstants.LOGGED_USER_EMAIL)
        // could not get parceble data to intent
        val bundleCalendarEvent = intent.extras!!.getBundle(DbConstants.DRUG_OBJECT)
        // 3
        if (currentProfile != null && email != null && bundleCalendarEvent != null) {
            val drug =
                bundleCalendarEvent.getParcelable<DrugObject>(DbConstants.DRUG_OBJECT)!!
            if (shouldShowNotifications(context,drug)) {
                RefillReminderHelper.createNotification(context, drug, currentProfile, email)
            }
            RefillReminderScheduler.scheduleAlarmsForReminder(
                context,
                email,
                currentProfile,
                drug
            )
        }
    }


    private fun shouldShowNotifications(context: Context,drug:DrugObject): Boolean {
        var shouldShow =false
        if(drug.refill.isToNotify){
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