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
            true,
            context.getString(R.string.app_name),
            NotificationManagerCompat.IMPORTANCE_HIGH
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
        val user = UserSerializable(
            email = email, mainProfileName = "", password = password,
            oldPassword = password
        )
        retrofit.loginUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Piller - Could not connect to server.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        val jObject = JSONObject(response.body()!!.string())
                        val userObject = UserObject(
                            jObject.getString("id"),
                            email,
                            null,
                            null,
                            jObject.getString("googleUser")!!.toBoolean()
                        )
                        scheduleNotificationsForAllProfiles(context, userObject)
                    }
                }
            }
        )
    }

    fun scheduleNotificationsForAllProfiles(context: Context, loggedUserObject: UserObject) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.getAllProfilesByEmail(loggedUserObject.userId).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<ResponseBody>, response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        val profiles = getAllUserProfiles(response)
                        scheduleNotificationsForAllDrugsForAllProfiles(
                            profiles,
                            context,
                            loggedUserObject
                        )
                    }
                }
            }
        )
    }

    private fun getAllUserProfiles(response: Response<ResponseBody>): List<CalendarProfile> {
        val jObject = JSONObject(response.body()!!.string())
        val profileListBody = jObject.get("profile_list") as JSONArray
        val profiles = mutableListOf<CalendarProfile>()
        for (i in 0 until profileListBody.length()) {
            val profileObjectData = profileListBody[i] as JSONObject
            val profileObject = Profile(
                profileObjectData.getString("id"),
                profileObjectData.getString("name"),
                profileObjectData.getString("relation")
            )
            profiles.add(
                CalendarProfile(
                    profileObject,
                    Array(7) { mutableListOf<CalendarEvent>() },
                    emptyArray()
                )
            )
        }
        return profiles
    }

    private fun scheduleNotificationsForAllDrugsForAllProfiles(
        calendarProfiles: List<CalendarProfile>, context: Context, loggedUserObject: UserObject
    ) {
        //  initiate notification for all of the profiles
        for (profile in calendarProfiles) {
            scheduleAlarmForProfile(profile.getProfileObject(), context, loggedUserObject)
        }
    }

    private fun scheduleAlarmForProfile(
        profile: Profile,
        context: Context,
        loggedUserObject: UserObject
    ) {
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(
            loggedUserObject.userId, profile.profileId
        ).enqueue(
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
                        scheduleAlarmsForAllDrugs(
                            drugInfoList as JSONArray,
                            context,
                            calendarId,
                            loggedUserObject
                        )
                    }
                }
            }
        )
    }

    private fun scheduleAlarmsForAllDrugs(
        drugList: JSONArray,
        context: Context,
        calendarId: String,
        loggedUserObject: UserObject
    ) {
        for (i in 0 until drugList.length()) {
            val drug = drugList.getJSONObject(i)
            val intakeDates = drug.get("intake_dates") as JSONObject
            val drugObject = ParserUtils.parsedDrugObject(drug, intakeDates, calendarId)
            AlarmScheduler.scheduleAllNotifications(loggedUserObject, context, drugObject)
        }
    }
}