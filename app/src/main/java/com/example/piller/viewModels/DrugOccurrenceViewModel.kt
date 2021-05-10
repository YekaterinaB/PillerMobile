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
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.JSONMessageExtractor
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

    var refillReminder = DbConstants.defaultRefillReminder
    var refillReminderTime = DbConstants.defaultRefillReminderTime
    var repeatCheckWeekdays: Array<Boolean> =
        arrayOf(false, false, false, false, false, false, false)
    var hasRepeatEnd = false
    var repeatOnEnum = RepeatOn.NO_REPEAT
    var repeatStartTime: MutableList<Calendar> = mutableListOf(Calendar.getInstance())
    var dosageMeasurementType: String = DbConstants.defaultStringValue
    var totalDose: Float = DbConstants.defaultTotalDose

    val snackBarMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val addedDrugSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val updatedDrugSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun getRefillReminderHour(): Int {
        return refillReminderTime.substring(0, 2).toInt()
    }

    fun getRefillReminderMinute(): Int {
        return refillReminderTime.substring(3).toInt()
    }

    fun convertRepeatEnumToString(repeatOn: RepeatOn): String {
        return when (repeatOn) {
            RepeatOn.DAY -> DbConstants.dailyString
            RepeatOn.WEEK -> DbConstants.weeklyString
            RepeatOn.MONTH -> DbConstants.monthlyString
            RepeatOn.YEAR -> DbConstants.yearlyString
            else -> DbConstants.repeatOnceString
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

    fun setDrugRepeatStartTime(hours: Int, minutes: Int) {
        val calendar = Calendar.getInstance()
        //  in order to save the previous selected date - set the time in millis to the current
        //  drug time in millis, and update only the hours and minutes
        calendar.timeInMillis = drug.occurrence.repeatStart
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        drug.occurrence.repeatStart = calendar.timeInMillis
    }

    private fun setDrugRepeatOn(repeatValue: Int) {
        //  create a new occurrence so if it is in edit mode - we won't save the previous repeat
        val occurrence = Occurrence()
        occurrence.eventId = drug.occurrence.eventId
        occurrence.repeatStart = drug.occurrence.repeatStart
        occurrence.repeatEnd = drug.occurrence.repeatEnd
        when (repeatOnEnum) {
            RepeatOn.DAY -> {
                occurrence.repeatDay = repeatValue
            }
            RepeatOn.WEEK -> {
                occurrence.repeatWeek = repeatValue
                occurrence.repeatWeekday = setWeekdayChecked(repeatCheckWeekdays)
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

    private fun setDrugByData(drugRepeatEnd: Date, repeatValue: Int) {
        if (hasRepeatEnd) {
            drug.occurrence.repeatEnd = drugRepeatEnd.time
        } else {
            //  if the user didn't choose repeat end - then set it to default
            drug.occurrence.repeatEnd = DbConstants.defaultRepeatEnd
        }
        setDrugRepeatOn(repeatValue)
        drug.dose = Dose(measurementType = dosageMeasurementType, totalDose = totalDose)
        drug.refill.reminderTime = refillReminderTime
    }

    fun addNewDrugToUser(
        loggedUserObject: UserObject,
        repeatValue: Int,
        drugRepeatEnd: Date,
        context: Context
    ) {
        setDrugByData(drugRepeatEnd, repeatValue)
        retrofit.addDrugCalendarByUser(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            drug
        ).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    snackBarMessage.value = DbConstants.couldNotAddDrugError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        addedDrugSuccess.value = true
                        updateDrugInfo(response)
                        //create notification
                        AlarmScheduler.scheduleAllNotifications(loggedUserObject, context, drug)
                    } else {
                        snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                    }
                }
            }
        )
    }

    private fun updateDrugInfo(response: Response<ResponseBody>) {
        val responseObject = JSONObject(response.body()!!.string())
        drug.occurrence.eventId = responseObject.get(DbConstants.eventId).toString()
        drug.taken_id = responseObject.get(DbConstants.takenId).toString()
        drug.dose.doseId = responseObject.get(DbConstants.doseId).toString()
        drug.refill.refillId = responseObject.get(DbConstants.refillId).toString()
        drug.drugId = responseObject.get(DbConstants.drugId).toString()
        DrugMap.instance.setDrugObject(drug.calendarId, drug)
    }

    fun updateDrugOccurrence(
        loggedUserObject: UserObject,
        repeatValue: Int,
        drugRepeatEnd: Date,
        context: Context
    ) {
        setDrugByData(drugRepeatEnd, repeatValue)
        retrofit.updateDrugOccurrence(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            drug.drugId,
            drug
        ).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    snackBarMessage.value = DbConstants.couldNotAddDrugError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        updatedDrugSuccess.value = true
                        AlarmScheduler.removeAllNotifications(loggedUserObject, context, drug)
                        updateDrugInfo(response)
                        AlarmScheduler.scheduleAllNotifications(loggedUserObject, context, drug)
                    } else {
                        snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                    }
                }
            }
        )
    }

    fun updateDrugDosage(totalDosage: Float) {
        drug.dose.totalDose = totalDosage
    }

    fun setDrugRefill(currentlyHave: Int, refillReminderTime: String) {
        drug.refill.isToNotify = true
        drug.refill.pillsBeforeReminder = refillReminder
        drug.refill.pillsLeft = currentlyHave
        drug.refill.reminderTime = refillReminderTime
    }

    fun removeDrugRefill() {
        drug.refill.isToNotify = false
    }

    fun initRepeatCheckedWeekdays() {
        if (drug.occurrence.hasRepeatWeek()) {
            for (day in drug.occurrence.repeatWeekday) {
                repeatCheckWeekdays[day - 1] = true
            }
        }
    }
}