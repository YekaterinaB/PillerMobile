package com.example.piller.intakeReminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.DrugIntakeAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
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
        val bundle = intent.extras!!.getBundle(DbConstants.LOGGED_USER_BUNDLE)
        val loggedUserObject = bundle?.getParcelable<UserObject>(DbConstants.LOGGED_USER_OBJECT)!!
        // could not get parceble data to intent
        val bundleCalendarEvent = intent.extras!!.getBundle(DbConstants.DRUG_OBJECT)
        // 3
        if (bundleCalendarEvent != null) {
            val drug =
                bundleCalendarEvent.getParcelable<DrugObject>(DbConstants.DRUG_OBJECT)!!
            addIntakeDateFalse(drug.taken_id, drug.refill.refillId)
            if (shouldShowNotifications(context)) {
                IntakeReminderHelper.createNotification(
                    context,
                    drug,
                    loggedUserObject.currentProfile!!.name,
                    loggedUserObject
                )
            }
            IntakeReminderScheduler.scheduleAlarmsForReminder(context, loggedUserObject, drug)
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