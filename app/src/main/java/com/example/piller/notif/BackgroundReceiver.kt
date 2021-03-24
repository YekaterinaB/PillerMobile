package com.example.piller.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ProfileAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.*
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.ParserUtils
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response


class BackgroundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            BackgroundNotificationScheduler.createNotificationChannel(context)
            BackgroundNotificationScheduler.setCalendarEventsNotifications(context)
        }
    }
}

object BackgroundNotificationScheduler {
    fun createNotificationChannel(context: Context) {
        NotificationHelper.createNotificationChannel(
            context,
            NotificationManagerCompat.IMPORTANCE_HIGH, true,
            context.getString(R.string.app_name), "App notification channel."
        )
    }

    fun setCalendarEventsNotifications(context: Context) {
        AppPreferences.init(context)
        ServiceBuilder.updateRetrofit(DbConstants.SERVER_URL)
        val email = AppPreferences.email
        val password = AppPreferences.password
        if (AppPreferences.stayLoggedIn && email.isNotEmpty() && password.isNotEmpty()) {
            loginUser(context, email, password)
        }
    }

    private fun loginUser(context: Context, email: String, password: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = User(email = email, name = "", password = password)
        retrofit.loginUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Piller - Could not connect to server.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        val jObject = JSONObject(response.body()!!.string())
                        val mainProfileName = jObject.get("name") as String
                        getUserProfiles(context, mainProfileName)
                    }
                }
            }
        )
    }

    private fun getUserProfiles(context: Context, mainProfileName: String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.getAllProfilesByEmail(AppPreferences.email).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        val profiles = getAllUserProfiles(response)
                        getAllDrugsForAllProfiles(profiles, context, mainProfileName)
                    }
                }
            }
        )
    }

    private fun getAllUserProfiles(response: Response<ResponseBody>): List<Profile> {
        val jObject = JSONObject(response.body()!!.string())
        val profileListBody = jObject.get("profile_list") as JSONArray
        val profiles = mutableListOf<Profile>()
        for (i in 0 until profileListBody.length()) {
            profiles.add(
                Profile(
                    profileListBody[i].toString(),
                    Array(7) { mutableListOf<CalendarEvent>() },
                    emptyArray()
                )
            )
        }
        return profiles
    }

    private fun getAllDrugsForAllProfiles(
        profiles: List<Profile>,
        context: Context,
        mainProfileName: String
    ) {
        //  first - schedule the notification for the main profile
        scheduleAlarmForProfile(mainProfileName, context)
        //  initiate notification for the rest of the profiles
        for (profile in profiles) {
            scheduleAlarmForProfile(profile.getProfileName(), context)
        }
    }

    private fun scheduleAlarmForProfile(profileName: String, context: Context) {
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(AppPreferences.email, profileName).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        val jObject = JSONObject(response.body()!!.string())
                        val drugInfoList = jObject.get(DbConstants.DRUG_INFO_LIST)
                        val calendarId = jObject.get("calendar_id").toString()
                        scheduleAlarmsForAllDrugs(profileName, drugInfoList as JSONArray, context, calendarId)
                    }
                }
            }
        )
    }

    private fun scheduleAlarmsForAllDrugs(
        profileName: String,
        drugList: JSONArray,
        context: Context,
        calendarId:String
    ) {
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val intakeDates = drug.get("intake_dates") as JSONObject
            val drugObject = ParserUtils.parsedDrugObject(drug, intakeDates,calendarId)
            AlarmScheduler.scheduleAlarmsForReminder(
                context,
                AppPreferences.email,
                profileName,
                drugObject
            )
        }
    }

}