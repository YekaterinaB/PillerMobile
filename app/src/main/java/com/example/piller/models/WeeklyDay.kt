package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WeeklyDay(
    @SerializedName("dayName") var dayName: String,
    @SerializedName("dayNumber") var dayNumber: Int
) : Parcelable