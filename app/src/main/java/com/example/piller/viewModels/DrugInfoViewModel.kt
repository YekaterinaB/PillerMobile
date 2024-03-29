package com.example.piller.viewModels

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.DrugMap
import com.example.piller.api.CalendarAPI
import com.example.piller.api.DrugAPI
import com.example.piller.api.DrugIntakeAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
import com.example.piller.notif.AlarmScheduler
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.ImageUtils
import com.example.piller.utilities.JSONMessageExtractor
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class DrugInfoViewModel : ViewModel() {
    private val _retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
    private val _drugAPIRetrofit = ServiceBuilder.buildService(DrugAPI::class.java)
    private val _drugIntakeAPIRetrofit = ServiceBuilder.buildService(DrugIntakeAPI::class.java)

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val deleteSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val deleteFutureSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val drugImageSource: MutableLiveData<File> by lazy {
        MutableLiveData<File>(null)
    }

    val intakeUpdateSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val pillsLeftMutable: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun deleteAllOccurrencesOfDrug(
        loggedUserObject: UserObject,
        drug: DrugObject,
        context: Context
    ) {
        _retrofit.deleteDrugByUser(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            drug.drugId
        ).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        deleteSuccess.value = true
                        AlarmScheduler.removeAllNotifications(loggedUserObject, context, drug)
                    } else {
                        mutableToastError.value = DbConstants.couldNotDeleteDrugError
                    }
                }
            }
        )
    }

    fun deleteFutureOccurrencesOfDrug(
        loggedUserObject: UserObject, drug: DrugObject, repeatEnd: String, context: Context
    ) {
        _retrofit.deleteFutureOccurrencesOfDrugByUser(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            drug.drugId,
            repeatEnd
        ).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        deleteFutureSuccess.value = true
                        //  remove the notifications because drug end is not initialized
                        AlarmScheduler.removeAllNotifications(loggedUserObject, context, drug)
                        // create new set of notifications with updated drug
                        AlarmScheduler.scheduleAllNotifications(loggedUserObject, context, drug)
                        DrugMap.instance.setDrugObject(drug.calendarId, drug) //update drug in map
                    } else {
                        mutableToastError.value = "Could not delete future occurrences drug."
                    }
                }
            }
        )
    }

    fun initiateDrugImage(context: Context, rxcui: String) {
        if (rxcui != "0" && !setImageFromCache(context, rxcui)) {
            _drugAPIRetrofit.getDrugImage(rxcui).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        mutableToastError.value = DbConstants.couldNotConnectServerError
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == DbConstants.OKCode) {
                            val jObject = JSONObject(response.body()!!.string())
                            setImageFromUrl(context, jObject["imageSrc"].toString(), rxcui)
                        } else {
                            mutableToastError.value = "Could not get drug image."
                        }
                    }
                }
            )
        }
    }

    private fun setImageFromUrl(context: Context, src: String, rxcui: String) {
        //  must run on a thread!!
        Thread {
            try {
                val url = URL(src)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                //  inside a thread we can't use mutableLiveData.value = ..., we have to use the
                //  function mutableLiveData.postValue(...)
                val image = BitmapFactory.decodeStream(connection.inputStream)
                val imageFile = ImageUtils.saveFile(context, image, rxcui)
                drugImageSource.postValue(imageFile)
            } catch (e: IOException) {
            }
        }.start()
    }

    private fun setImageFromCache(context: Context, rxcui: String): Boolean {
        val image = ImageUtils.loadImageFile(context, rxcui)
        if (image != null && image.exists()) {
            drugImageSource.value = image
            return true
        }
        return false
    }

    fun updateDrugIntake(taken: Boolean, intakeId: String, refillId: String, date: Long) {
        if (taken) {
            setIntakeTaken(intakeId, refillId, date)
        } else {
            setIntakeNotTaken(intakeId, refillId, date)
        }
    }

    private fun updateIntakeByResult(pillsLeft: Int) {
        intakeUpdateSuccess.value = true
        pillsLeftMutable.value = pillsLeft
    }

    private fun setIntakeTaken(intakeId: String, refillId: String, date: Long) {
        _drugIntakeAPIRetrofit.setIntakeTaken(intakeId, refillId, date).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not set intake."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        mutableToastError.value = JSONMessageExtractor.getErrorMessage(response)
                    } else {
                        updateIntakeByResult(response.body()!!.string().replace('\"', ' ').toInt())
                    }
                }
            }
        )
    }

    private fun setIntakeNotTaken(intakeId: String, refillId: String, date: Long) {
        _drugIntakeAPIRetrofit.setIntakeNotTaken(intakeId, refillId, date).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not set intake."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        mutableToastError.value = JSONMessageExtractor.getErrorMessage(response)
                    } else {
                        updateIntakeByResult(response.body()!!.string().replace('\"', ' ').toInt())
                    }
                }
            }
        )
    }
}