package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class DrugInfoViewModel : ViewModel() {
    private lateinit var calendarEvent: CalendarEvent
    private val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val deleteSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun setCalendarEvent(newCalendarEvent: CalendarEvent) {
        calendarEvent = newCalendarEvent
    }

    fun getCalendarEvent(): CalendarEvent {
        return calendarEvent
    }

    fun deleteAllOccurrencesOfDrug(email: String, name: String, rxcui: String) {
        retrofit.deleteDrugByUser(email, name, rxcui).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        deleteSuccess.value = true
                    } else {
                        mutableToastError.value = "Could not delete drug."
                    }
                }
            }
        )
    }
}