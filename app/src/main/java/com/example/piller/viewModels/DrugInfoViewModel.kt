package com.example.piller.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.CalendarAPI
import com.example.piller.api.DrugAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugOccurrence
import com.example.piller.notif.AlarmScheduler
import com.example.piller.utilities.ImageCache
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class DrugInfoViewModel : ViewModel() {
    private val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
    private val drugAPIRetrofit = ServiceBuilder.buildService(DrugAPI::class.java)

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val deleteSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val deleteFutureSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val drugImageBitmap: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>(null)
    }

    fun deleteAllOccurrencesOfDrug(
        email: String,
        currentProfile: String,
        drug: DrugOccurrence,
        context: Context
    ) {
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
                        AlarmScheduler.removeAlarmsForReminder(context, drug, email, currentProfile)
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
        retrofit.deleteFutureOccurrencesOfDrugByUser(
            email,
            currentProfile,
            drug.event_id,
            repeatEnd
        ).enqueue(
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
                        //remove the notifications becuase drug end is not initialized
                        AlarmScheduler.removeAlarmsForReminder(context, drug, email, currentProfile)
                        // create new set of notifications with updated drug
                        AlarmScheduler.scheduleAlarmsForReminder(context,email,currentProfile,drug)
                    } else {
                        mutableToastError.value = "Could not delete future occurrences drug."
                    }
                }
            }
        )
    }

    fun initiateDrugImage(rxcui: Int) {
        if (rxcui != 0 && !setImageFromCache(rxcui.toString())) {
            drugAPIRetrofit.getDrugImage(rxcui.toString()).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        mutableToastError.value = "Could not connect to server."
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == 200) {
                            val jObject = JSONObject(response.body()!!.string())
                            setImageFromUrl(jObject["imageSrc"].toString())
                        } else {
                            mutableToastError.value = "Could not get drug image."
                        }
                    }
                }
            )
        }
    }

    private fun setImageFromUrl(src: String) {
        //  must run on a thread!!
        Thread {
            try {
                val url = URL(src)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                //  inside a thread we can't use mutableLiveData.value = ..., we have to use the
                //  function mutableLiveData.postValue(...)
                drugImageBitmap.postValue(BitmapFactory.decodeStream(connection.inputStream))
            } catch (e: IOException) {
            }
        }.start()
    }

    private fun setImageFromCache(rxcui: String): Boolean {
        val image = ImageCache.instance.retrieveBitmapFromCache(rxcui)
        if (image != null) {
            drugImageBitmap.value = image
            return true
        }
        return false
    }

}