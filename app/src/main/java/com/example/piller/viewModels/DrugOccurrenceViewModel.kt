package com.example.piller.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.DrugMap
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.Dose
import com.example.piller.models.DrugObject
import com.example.piller.models.Occurrence
import com.example.piller.models.UserObject
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
    private lateinit var drug: DrugObject
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

    fun convertRepeatEnumToString(repeatOn: RepeatOn): String {
        return when (repeatOn) {
            RepeatOn.DAY -> "Daily"
            RepeatOn.WEEK -> "Weekly"
            RepeatOn.MONTH -> "Monthly"
            RepeatOn.YEAR -> "Yearly"
            else -> "Repeat once"
        }
    }

    fun setDrug(newDrug: DrugObject) {
        drug = newDrug
    }

    fun getDrug(): DrugObject {
        return drug
    }

    private fun setWeekdayChecked(selectedDays: Array<Boolean>): List<Int> {
        //  0 = sunday, 1 = monday and so on
        for ((index, isDaySelected) in selectedDays.withIndex()) {
            if (isDaySelected) {
                weekdayRepeat.add(index + 1)
            } else {
                weekdayRepeat.remove(index + 1)
            }
        }
        return weekdayRepeat.toList()
    }

    fun setDrugRepeatStartDate(yearSelected: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        //  in order to save the previous selected time - set the time in millis to the current
        //  drug time in millis, and update only the date
        calendar.timeInMillis = drug.occurrence.repeatStart
        calendar.set(Calendar.YEAR, yearSelected)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        drug.occurrence.repeatStart = calendar.timeInMillis
    }

    fun setDrugRepeatEndDate(repeatEndDate: Date) {
        drug.occurrence.repeatEnd = repeatEndDate.time
    }

    fun removeDrugRepeatEndDate() {
        drug.occurrence.repeatEnd = 0
    }

    fun setDrugRepeatStartTime(hours: Int, minutes: Int) {
        val calendar = Calendar.getInstance()
        //  in order to save the previous selected date - set the time in millis to the current
        //  drug time in millis, and update only the hours and minutes
        calendar.timeInMillis = drug.occurrence.repeatStart
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        drug.occurrence.repeatStart = calendar.timeInMillis
    }

    private fun setDrugRepeatOn(
        repeatOn: RepeatOn,
        repeatValue: Int,
        daysRepeatCheck: Array<Boolean>
    ) {
        //  create a new occurrence so if it is in edit mode - we won't save the previous repeat
        val occurrence = Occurrence()
        occurrence.eventId = drug.occurrence.eventId
        occurrence.repeatStart = drug.occurrence.repeatStart
        occurrence.repeatEnd = drug.occurrence.repeatEnd
        when (repeatOn) {
            RepeatOn.DAY -> {
                occurrence.repeatDay = repeatValue
            }
            RepeatOn.WEEK -> {
                occurrence.repeatWeek = repeatValue
                occurrence.repeatWeekday = setWeekdayChecked(daysRepeatCheck)
            }
            RepeatOn.MONTH -> {
                occurrence.repeatMonth = repeatValue
            }
            RepeatOn.YEAR -> {
                occurrence.repeatYear = repeatValue
            }
            else -> {
            }
        }
        drug.occurrence = occurrence
    }

    fun addNewDrugToUser(
        loggedUserObject: UserObject,
        repeatOn: RepeatOn,
        repeatValue: Int,
        daysRepeatCheck: Array<Boolean>,
        context: Context
    ) {
        setDrugRepeatOn(repeatOn, repeatValue, daysRepeatCheck)
        retrofit.addDrugCalendarByUser(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            drug
        ).enqueue(
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
                        updateDrugInfo(response)
                        //create notification
                        AlarmScheduler.scheduleAllNotifications(loggedUserObject, context, drug)
                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        snackBarMessage.value = jObjError["message"] as String
                    }
                }
            }
        )
    }

    private fun updateDrugInfo(response: Response<ResponseBody>) {
        val responseObject = JSONObject(response.body()!!.string())
        drug.occurrence.eventId = responseObject.get("event_id").toString()
        drug.taken_id = responseObject.get("taken_id").toString()
        drug.dose.doseId = responseObject.get("dose_id").toString()
        drug.refill.refillId = responseObject.get("refill_id").toString()
        drug.drugId = responseObject.get("drug_id").toString()
        DrugMap.instance.setDrugObject(drug.calendarId, drug)
    }

    fun updateDrugOccurrence(
        loggedUserObject: UserObject,
        repeatOn: RepeatOn,
        repeatValue: Int,
        daysRepeatCheck: Array<Boolean>,
        context: Context
    ) {
        setDrugRepeatOn(repeatOn, repeatValue, daysRepeatCheck)
        retrofit.updateDrugOccurrence(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            drug.drugId,
            drug
        ).enqueue(
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
                        AlarmScheduler.removeAllNotifications(loggedUserObject, context, drug)
                        updateDrugInfo(response)
                        AlarmScheduler.scheduleAllNotifications(loggedUserObject, context, drug)

                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        snackBarMessage.value = jObjError["message"] as String
                    }
                }
            }
        )
    }


    fun setDrugDosage(measurementType: String, totalDose: Float) {
        drug.dose = Dose(measurementType = measurementType, totalDose = totalDose)
    }

    fun updateDrugDosage(totalDosage: Float) {
        drug.dose.totalDose = totalDosage
    }

    fun setDrugRefill(currentlyHave: Int, refillReminder: Int, refillReminderTime: String) {
        drug.refill.isToNotify = true
        drug.refill.pillsBeforeReminder = refillReminder
        drug.refill.pillsLeft = currentlyHave
        drug.refill.reminderTime = refillReminderTime
    }

    fun removeDrugRefill() {
        drug.refill.isToNotify = false
    }
}