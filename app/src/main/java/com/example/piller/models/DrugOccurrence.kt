package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrugOccurrence(
    @SerializedName("name") var drug_name: String,
    @SerializedName("rxcui") var rxcui: Int,
    @SerializedName("event_id") var event_id: String="",
    @SerializedName("repeat_year") var repeatYear: String = "0",
    @SerializedName("repeat_month") var repeatMonth: String = "0",
    @SerializedName("repeat_day") var repeatDay: String = "0",
    @SerializedName("repeat_week") var repeatWeek: String = "0",
    @SerializedName("repeat_weekday") var repeatWeekday: String = "0",
    @SerializedName("repeat_start") var repeatStart: Long = 0,
    @SerializedName("repeat_end") var repeatEnd: Long = 0
) : Parcelable