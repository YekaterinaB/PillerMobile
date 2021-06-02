package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*


@Parcelize
data class CalendarEvent(
    @SerializedName(DbConstants.CALENDAR_ID) var calendarId: String,
    @SerializedName(DbConstants.drugId) var drugId: String,
    @SerializedName("index_day") var indexDay: Int,// 0-first day of the asked timeline...
    @SerializedName("intake_time") var intakeTime: Date,
    @SerializedName("intake_end_time") var intakeEndTime: Date,
    @SerializedName("is_taken") var isTaken: Boolean
) : Parcelable