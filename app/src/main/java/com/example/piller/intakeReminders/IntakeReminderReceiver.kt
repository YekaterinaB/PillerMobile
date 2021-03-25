package com.example.piller.intakeReminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.DrugIntakeAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugObject
import com.example.piller.notif.AlarmScheduler
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.*


class IntakeReminderReceiver : BroadcastReceiver() {

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
                bundleCalendarEvent.getParcelable<DrugObject>(DbConstants.DRUG_OBJECT)!!
            addIntakeDateFalse(drug.taken_id, drug.refill.refillId)
            if (shouldShowNotifications(context)) {
                IntakeReminderHelper.createNotification(context, drug, currentProfile, email)
            }
            IntakeReminderScheduler.scheduleAlarmsForReminder(
                context,
                email,
                currentProfile,
                drug
            )
        }
    }

    private fun addIntakeDateFalse(takenId: String, refillId: String) {
        val calCurr = Calendar.getInstance()
        DateUtils.setCalendarTime(
            calCurr,
            calCurr.get(Calendar.HOUR_OF_DAY),
            calCurr.get(Calendar.MINUTE)
        )
        val retrofit = ServiceBuilder.buildService(DrugIntakeAPI::class.java)
        retrofit.setIntakeNotTaken(takenId, refillId, calCurr.timeInMillis).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                }
            }
        )

    }

    private fun shouldShowNotifications(context: Context): Boolean {
        var shouldShow: Boolean
        try {
            shouldShow = AppPreferences.showNotifications
        } catch (e: UninitializedPropertyAccessException) {
            AppPreferences.init(context)
            shouldShow = AppPreferences.showNotifications
        }

        return shouldShow
    }

}