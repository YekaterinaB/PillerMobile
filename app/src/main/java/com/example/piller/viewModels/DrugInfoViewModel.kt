package com.example.piller.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugOccurrence
import com.example.piller.notif.AlarmScheduler
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class DrugInfoViewModel : ViewModel() {
    private val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val deleteSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val deleteFutureSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun deleteAllOccurrencesOfDrug(email: String, currentProfile: String, drug: DrugOccurrence,context: Context) {
        retrofit.deleteDrugByUser(email, currentProfile, drug.event_id).enqueue(
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
                        AlarmScheduler.removeAlarmsForReminder(context, drug,email,currentProfile)
                    } else {
                        mutableToastError.value = "Could not delete drug."
                    }
                }
            }
        )
    }

    fun deleteFutureOccurrencesOfDrug(
        email: String,
        currentProfile: String,
        drug: DrugOccurrence,
        repeatEnd: String,
        context: Context
    ) {
        retrofit.deleteFutureOccurrencesOfDrugByUser(email, currentProfile, drug.event_id, repeatEnd).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        deleteFutureSuccess.value = true
                        AlarmScheduler.removeAlarmsForReminder(context, drug,email,currentProfile)
                    } else {
                        mutableToastError.value = "Could not delete future occurrences drug."
                    }
                }
            }
        )
    }
}