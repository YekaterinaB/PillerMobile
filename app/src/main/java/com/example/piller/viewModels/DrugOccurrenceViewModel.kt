package com.example.piller.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugOccurrence
import com.example.piller.notif.AlarmScheduler
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*

class DrugOccurrenceViewModel : ViewModel() {
    enum class RepeatOn {
        DAY, WEEK, MONTH, YEAR, NO_REPEAT
    }

    private val weekdayRepeat = mutableSetOf<Int>()
    private lateinit var drug: DrugOccurrence
    private val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)

    val snackBarMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val addedDrugSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val updatedDrugSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }


    fun setDrug(newDrug: DrugOccurrence) {
        drug = newDrug
    }

    fun getDrug(): DrugOccurrence {
        return drug
    }

    fun setWeekdayChecked(weekdayNumber: Int, checked: Boolean) {
        if (checked) {
            weekdayRepeat.add(weekdayNumber)
        } else {
            weekdayRepeat.remove(weekdayNumber)
        }
    }

    fun setDrugRepeatStartDate(yearSelected: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        //  in order to save the previous selected time - set the time in millis to the current
        //  drug time in millis, and update only the date
        calendar.timeInMillis = drug.repeatStart
        calendar.set(Calendar.YEAR, yearSelected)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        drug.repeatStart = calendar.timeInMillis
    }

    fun setDrugRepeatStartTime(hours: Int, minutes: Int) {
        val calendar = Calendar.getInstance()
        //  in order to save the previous selected date - set the time in millis to the current
        //  drug time in millis, and update only the hours and minutes
        calendar.timeInMillis = drug.repeatStart
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        drug.repeatStart = calendar.timeInMillis
    }

    private fun updateDrugWeekday() {
        if (weekdayRepeat.size > 0) {
            drug.repeatWeekday = weekdayRepeat.joinToString(separator = ",")
        }
    }

    private fun setRepeatOn(repeatOn: RepeatOn, repeatValue: String) {
        when (repeatOn) {
            RepeatOn.DAY -> {
                drug.repeatDay = repeatValue
            }
            RepeatOn.WEEK -> {
                drug.repeatWeek = repeatValue
                updateDrugWeekday()
            }
            RepeatOn.MONTH -> {
                drug.repeatMonth = repeatValue
            }
            RepeatOn.YEAR -> {
                drug.repeatYear = repeatValue
            }
            else -> {
            }
        }
    }

    fun addNewDrugToUser(
        email: String,
        profileName: String,
        repeatOn: RepeatOn?,
        repeatValue: String?,
        context: Context
    ) {
        repeatValue?.let { repeatOn?.let { it1 -> setRepeatOn(it1, it) } }
        retrofit.addDrugCalendarByUser(email, profileName, drug).enqueue(
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
                        drug.event_id = response.body()!!.string().replace("\"", "")
                        //create notification
                        AlarmScheduler.scheduleAlarmsForReminder(context, email, profileName, drug)
                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        snackBarMessage.value = jObjError["message"] as String
                    }
                }
            }
        )
    }

    fun updateDrugOccurrence(
        email: String,
        currentProfile: String,
        repeatOn: RepeatOn?,
        repeatValue: String?,
        context: Context
    ) {
        repeatValue?.let { repeatOn?.let { it1 -> setRepeatOn(it1, it) } }
        retrofit.updateDrugOccurrence(email, currentProfile, drug.event_id, drug).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    snackBarMessage.value = "Could not add drug."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        updatedDrugSuccess.value = true
                        AlarmScheduler.removeAlarmsForReminder(context, drug, email, currentProfile)
                        drug.event_id = response.body()!!.string().replace("\"", "")
                        AlarmScheduler.scheduleAlarmsForReminder(
                            context,
                            email,
                            currentProfile,
                            drug
                        )

                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        snackBarMessage.value = jObjError["message"] as String
                    }
                }
            }
        )
    }
}