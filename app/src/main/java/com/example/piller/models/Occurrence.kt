package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Occurrence(
    @SerializedName("event_id") var eventId: String = "",
    @SerializedName("repeat_year") var repeatYear: Int = 0,
    @SerializedName("repeat_month") var repeatMonth: Int = 0,
    @SerializedName("repeat_day") var repeatDay: Int = 0,
    @SerializedName("repeat_week") var repeatWeek: Int = 0,
    @SerializedName("repeat_weekday") var repeatWeekday: List<Int> = listOf(0),
    @SerializedName("repeat_start") var repeatStart: MutableList<Long> = mutableListOf(0),
    @SerializedName("repeat_end") var repeatEnd: Long = 0
) : Parcelable
