package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.DrugMap
import com.example.piller.utilities.DateUtils
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

class WeeklyCalendarViewModel : ViewModel() {
    private val eventInterpreter = EventInterpreter()
    lateinit var calendarId: String
    val mutableCurrentWeeklyCalendar: MutableLiveData<Array<MutableList<CalendarEvent>>> by lazy {
        MutableLiveData<Array<MutableList<CalendarEvent>>>()
    }

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val mutableDeleteSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun getWeekEvents(loggedUserEmail: String, profile: Profile) {
        if (!profile.getIsInitialized()) {
            // ask db for user calendar if not initialized
            getCalendarByUser(loggedUserEmail, profile)
        } else {
            // update mutable calendar
            changeMutableWeeklyCalendar(profile.getWeeklyCalendar())
        }
    }

    fun deleteDrug(calendarEvent: CalendarEvent) {
        val drugObj = DrugMap.instance.getDrugObject(calendarEvent.calendarId, calendarEvent.drugId)
        for (calendarEvents in mutableCurrentWeeklyCalendar.value!!) {
            for (index in calendarEvents.size - 1 downTo 0) {
                val drugObjectInIndex = DrugMap.instance.getDrugObject(
                    calendarEvents[index].calendarId, calendarEvents[index].drugId
                )
                if (drugObjectInIndex.occurrence.eventId == drugObj.occurrence.eventId) {
                    calendarEvents.removeAt(index)
                }
            }
        }

        //  do the next line in order to notify the observers (because the for loop above doesn't
        //  update mutableCurrentWeeklyCalendar.value directly, but its list content
        DrugMap.instance.removeDrugFromMap(drugObj.calendarId, drugObj)
        mutableDeleteSuccess.value = true
    }

    fun deleteFutureDrug(calendarEvent: CalendarEvent) {
        val drugObj = DrugMap.instance.getDrugObject(calendarEvent.calendarId, calendarEvent.drugId)

        for (calendarEvents in mutableCurrentWeeklyCalendar.value!!) {
            for (index in calendarEvents.size - 1 downTo 0) {
                val drugObjectInIndex = DrugMap.instance.getDrugObject(
                    calendarEvents[index].calendarId, calendarEvents[index].drugId
                )
                //  remove drug if the intake date is after the day after the given date of calendarEvent
                if (drugObjectInIndex.occurrence.eventId == drugObj.occurrence.eventId
                    && DateUtils.isDateAfter(
                        calendarEvents[index].intakeTime, calendarEvent.intakeTime
                    )
                ) {
                    calendarEvents.removeAt(index)
                }
            }
        }

        //  do the next line in order to notify the observers (because the for loop above doesn't
        //  update mutableCurrentWeeklyCalendar.value directly, but its list content
        mutableDeleteSuccess.value = true
    }

    private fun getCalendarByUser(email: String, profile: Profile) {
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

    private fun initCalenderView(calendarInfo: Response<ResponseBody>) {
        val jObject = JSONObject(calendarInfo.body()!!.string())
        val drugInfoList = jObject.get(DbConstants.DRUG_INFO_LIST)
        calendarId = jObject.get("calendar_id").toString()
        val startDate = DateUtils.getFirstDayOfWeek()
        val endDate = DateUtils.getLastDayOfWeek()
        val weekEvents = eventInterpreter.getEventsForCalendarByDate(
            startDate, endDate,
            drugInfoList as JSONArray, calendarId
        )
        changeMutableWeeklyCalendar(weekEvents)
    }
}