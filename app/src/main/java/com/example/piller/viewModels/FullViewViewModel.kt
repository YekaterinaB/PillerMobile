package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.EventInterpreter
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class FullViewViewModel : ViewModel() {
    private val eventInterpreter = EventInterpreter()
    val mutableCurrentMonthlyCalendar: MutableLiveData<Array<MutableList<CalendarEvent>>> by lazy {
        MutableLiveData<Array<MutableList<CalendarEvent>>>()
    }

    fun initiateMonthEvents(loggedUserEmail: String, profile: Profile) {
        if (profile.getMonthlyCalendar().isEmpty()) {
            getCalendarByUser(loggedUserEmail, profile)
        } else {
            // update mutable calendar
            changeMutableMonthlyCalendar(profile.getMonthlyCalendar())
        }
    }

    private fun getCalendarByUser(email: String, profile: Profile) {
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(email, profile.getProfileName()).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        initCalenderView(response)
                    } else {
                        //  todo handle error
                    }
                }
            }
        )

    }

    private fun changeMutableMonthlyCalendar(monthEvents: Array<MutableList<CalendarEvent>>) {
        mutableCurrentMonthlyCalendar.value = monthEvents
    }

    private fun initCalenderView(calendarInfo: Response<ResponseBody>) {
        val jObject = JSONObject(calendarInfo.body()!!.string())
        val drugInfoList = jObject.get(DbConstants.DRUG_INFO_LIST)

        val startDate = eventInterpreter.getFirstDayOfMonth()
        val endDate = eventInterpreter.getLastDayOfMonth()
        val monthEvents = eventInterpreter.getEventsForCalendarByDate(
            startDate, endDate,
            drugInfoList as JSONArray
        )
        changeMutableMonthlyCalendar(monthEvents)
    }
}