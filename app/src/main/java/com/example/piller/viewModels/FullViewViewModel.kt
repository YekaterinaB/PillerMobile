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
import java.util.*

class FullViewViewModel : ViewModel() {
    private val eventInterpreter = EventInterpreter()
    val mutableCurrentMonthlyCalendar: MutableLiveData<Array<MutableList<CalendarEvent>>> by lazy {
        MutableLiveData<Array<MutableList<CalendarEvent>>>()
    }

    val mutableDeleteSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun initiateMonthEvents(
        loggedUserEmail: String,
        profile: Profile,
        startDate: Date,
        endDate: Date
    ) {
        if (profile.getMonthlyCalendar().isEmpty()) {
            updateCalendarByUser(loggedUserEmail, profile, startDate, endDate)
        } else {
            // update mutable calendar
            setMutableMonthlyCalendar(profile.getMonthlyCalendar())
        }
    }

    fun deleteDrugs(rxcuisToDelete: List<String>) {
        if (rxcuisToDelete.isEmpty()) {
            return
        }
        for (calendarEvents in mutableCurrentMonthlyCalendar.value!!) {
            for (index in calendarEvents.size - 1 downTo 0) {
                for (rxcuiToDelete in rxcuisToDelete) {
                    if (calendarEvents[index].drug_rxcui == rxcuiToDelete) {
                        calendarEvents.removeAt(index)
                    }
                }
            }
        }

        //  do the next line in order to notify the observers (because the for loop above doesn't
        //  update mutableCurrentWeeklyCalendar.value directly, but its list content
        mutableDeleteSuccess.value = true
    }

    fun deleteFutureDrug(rxcuisToDelete: List<CalendarEvent>) {
        if (rxcuisToDelete.isEmpty()) {
            return
        }
        val eventInterpreter = EventInterpreter()
        for (calendarEvents in mutableCurrentMonthlyCalendar.value!!) {
            for (index in calendarEvents.size - 1 downTo 0) {
                val calendarTomorrow = Calendar.getInstance()
                for (rxcuiToDelete in rxcuisToDelete) {
                    calendarTomorrow.timeInMillis =
                        eventInterpreter.getTomorrowDateInMillis(rxcuiToDelete.intake_time)
                    if (calendarEvents[index].drug_rxcui == rxcuiToDelete.drug_rxcui
                        && eventInterpreter.isDateAfter(
                            calendarEvents[index].intake_time,
                            calendarTomorrow.time
                        )
                    ) {
                        calendarEvents.removeAt(index)
                    }
                }
            }
        }

        //  do the next line in order to notify the observers (because the for loop above doesn't
        //  update mutableCurrentWeeklyCalendar.value directly, but its list content
        mutableDeleteSuccess.value = true
    }

    fun updateCalendarByUser(email: String, profile: Profile, startDate: Date, endDate: Date) {
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(email, profile.getProfileName()).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        initCalenderView(response, startDate, endDate)
                    } else {
                        //  todo handle error
                    }
                }
            }
        )
    }

    private fun setMutableMonthlyCalendar(monthEvents: Array<MutableList<CalendarEvent>>) {
        mutableCurrentMonthlyCalendar.value = monthEvents
    }

    private fun initCalenderView(
        calendarInfo: Response<ResponseBody>,
        startDate: Date,
        endDate: Date
    ) {
        val jObject = JSONObject(calendarInfo.body()!!.string())
        val drugInfoList = jObject.get(DbConstants.DRUG_INFO_LIST)

        val monthEvents = eventInterpreter.getEventsForCalendarByDate(
            startDate, endDate,
            drugInfoList as JSONArray
        )
        setMutableMonthlyCalendar(monthEvents)
    }
}