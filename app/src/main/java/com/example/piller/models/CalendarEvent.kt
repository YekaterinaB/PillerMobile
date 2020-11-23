package com.example.piller.models

import com.google.gson.annotations.SerializedName
import java.util.*
enum class DayOfWeek {
    SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SUTERDAY
}
data class CalendarEvent (
    @SerializedName("drug_name") var drug_name: String,
    @SerializedName("index_day")var index_day: Int,// 0-first day of the asked timeline...
    @SerializedName("intake_time")var intake_time: Date,
    @SerializedName("is_taken")var is_taken: Boolean
)