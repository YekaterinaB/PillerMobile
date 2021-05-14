package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.DrugMap
import com.example.piller.utilities.DateUtils
import com.example.piller.EventInterpreter
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.CalendarProfile
import com.example.piller.models.UserObject
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

    val showLoadingScreen: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun initiateMonthEvents(
        loggedUserObject: UserObject,
        calendarProfile: CalendarProfile,
        startDate: Date,
        endDate: Date
    ) {
        if (calendarProfile.getMonthlyCalendar().isEmpty()) {
            updateCalendarByUser(loggedUserObject, startDate, endDate)
        } else {
            // update mutable calendar
            setMutableMonthlyCalendar(calendarProfile.getMonthlyCalendar())
        }
    }

    fun deleteDrugs(rxcuisToDelete: List<Int>) {
        for (calendarEvents in mutableCurrentMonthlyCalendar.value!!) {
            for (index in calendarEvents.size - 1 downTo 0) {
                for (rxcuiToDelete in rxcuisToDelete) {
                    val drugObj = DrugMap.instance.getDrugObject(
                        calendarEvents[index].calendarId, calendarEvents[index].drugId
                    )
                    if (drugObj.rxcui == rxcuiToDelete) {
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
        for (calendarEvents in mutableCurrentMonthlyCalendar.value!!) {
            for (index in calendarEvents.size - 1 downTo 0) {
                val drugObjByIndex = DrugMap.instance.getDrugObject(
                    calendarEvents[index].calendarId, calendarEvents[index].drugId
                )
                for (rxcuiToDelete in rxcuisToDelete) {
                    val drugObjToDelete = DrugMap.instance.getDrugObject(
                        rxcuiToDelete.calendarId, rxcuiToDelete.drugId
                    )
                    if (drugObjByIndex.rxcui == drugObjToDelete.rxcui && DateUtils.isDateAfter(
                            calendarEvents[index].intakeTime, rxcuiToDelete.intakeTime
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

    fun updateCalendarByUser(loggedUserObject: UserObject, startDate: Date, endDate: Date) {
        showLoadingScreen.value = true
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId
        ).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        initCalenderView(response, startDate, endDate)
                    }
                    showLoadingScreen.value = false
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
        val calendarId = jObject.get(DbConstants.CALENDAR_ID).toString()
        val monthEvents = eventInterpreter.getEventsForCalendarByDate(
            startDate, endDate, drugInfoList as JSONArray, calendarId
        )
        setMutableMonthlyCalendar(monthEvents)
    }
}