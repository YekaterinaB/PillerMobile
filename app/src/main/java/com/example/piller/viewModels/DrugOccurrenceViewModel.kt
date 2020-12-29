package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.Drug
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class DrugOccurrenceViewModel : ViewModel() {
    val snackBarMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val addedDrugSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    private val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)

    fun addNewDrugToUser(email: String, name: String, newDrug: Drug) {
        retrofit.addDrugCalendarByUser(email, name, newDrug).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    snackBarMessage.value = "Could not add drug."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        addedDrugSuccess.value = true
                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        snackBarMessage.value = jObjError["message"] as String
                    }
                }
            }
        )
    }
}