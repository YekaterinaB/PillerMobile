package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class CalendarEvent(
    @SerializedName("drug_name") var drug_name: String,
    @SerializedName("drug_rxcui") var drug_rxcui: String,
    @SerializedName("index_day") var index_day: Int,// 0-first day of the asked timeline...
    @SerializedName("intake_time") var intake_time: Date,
    @SerializedName("event_id") var event_id: String,
    @SerializedName("repeat_weekday") var repeat_weekday: String,
    @SerializedName("is_taken") var is_taken: Boolean,
    @SerializedName("showTakenCheckBox") var showTakenCheckBox: Boolean = false
) : Parcelable