package com.example.piller.viewModels

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piller.EventInterpreter
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class WeeklyCalendarViewModel : ViewModel() {
    private val eventInterpreter = EventInterpreter()

    val mutableCurrentWeeklyCalendar: MutableLiveData<Array<MutableList<CalendarEvent>>> by lazy {
        MutableLiveData<Array<MutableList<CalendarEvent>>>()
    }

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getWeekEvents(
        loggedUserEmail: String,
        profile: Profile
    ) {
        if (!profile.getIsInitialized()) {
            // ask db for user calendar if not initialized
            getCalendarByUser(loggedUserEmail, profile)

        } else {
            // update mutable calendar
            changeMutableWeeklyCalendar(profile.getWeeklyCalendar())
        }
    }


    private fun getCalendarByUser(
        email: String,
        profile: Profile
    ) {
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(email, profile.getProfileName()).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        initCalenderView(response)
                    } else {
                        mutableToastError.value = "Could not get weekly calendar view."
                    }
                }
            }
        )


    }

    private fun changeMutableWeeklyCalendar(weekEvents: Array<MutableList<CalendarEvent>>) {
        mutableCurrentWeeklyCalendar.value = weekEvents
    }

    private fun initCalenderView(
        calendarInfo: Response<ResponseBody>
    ) {
        val jObject = JSONObject(calendarInfo.body()!!.string())
        val drugInfoList = jObject.get("drug_info_list")

        val startDate = eventInterpreter.getFirstDayOfWeek()
        val endDate = eventInterpreter.getLastDayOfWeek()
        val weekEvents = eventInterpreter.getEventsForCalendarByDate(
            startDate, endDate,
            drugInfoList as JSONArray
        )
        changeMutableWeeklyCalendar(weekEvents)

    }

}