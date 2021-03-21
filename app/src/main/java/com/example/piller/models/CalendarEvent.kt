package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class CalendarEvent(
    @SerializedName("calendar_id") var calendarId: String,
    @SerializedName("drug_id") var drugId: String,
    //@SerializedName("drug_object") var drugObject:DrugObject,
    @SerializedName("index_day") var indexDay: Int,// 0-first day of the asked timeline...
    @SerializedName("intake_time") var intakeTime: Date,
    @SerializedName("intake_end_time") var intakeEndTime: Date,
    @SerializedName("is_taken") var isTaken: Boolean
) : Parcelable