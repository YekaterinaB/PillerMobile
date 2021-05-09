package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Occurrence(
    @SerializedName(DbConstants.eventId) var eventId: String = DbConstants.defaultStringValue,
    @SerializedName(DbConstants.repeatYear) var repeatYear: Int = DbConstants.defaultRepeatYear,
    @SerializedName(DbConstants.repeatMonth) var repeatMonth: Int = DbConstants.defaultRepeatMonth,
    @SerializedName(DbConstants.repeatDay) var repeatDay: Int = DbConstants.defaultRepeatDay,
    @SerializedName(DbConstants.repeatWeek) var repeatWeek: Int = DbConstants.defaultRepeatWeek,
    @SerializedName(DbConstants.repeatWeekDay) var repeatWeekday: List<Int> = DbConstants.defaultRepeatWeekDay,
    @SerializedName(DbConstants.repeatStart) var repeatStart: Long = DbConstants.defaultRepeatStart,
    @SerializedName(DbConstants.repeatEnd) var repeatEnd: Long = DbConstants.defaultRepeatEnd
) : Parcelable {
    fun repeatOnce(): Boolean {
        return !hasRepeatYear() && !hasRepeatMonth() && !hasRepeatWeek() && !hasRepeatDay()
    }

    fun hasRepeatYear(): Boolean {
        return repeatYear > 0
    }

    fun hasRepeatMonth(): Boolean {
        return repeatMonth > 0
    }

    fun hasRepeatWeek(): Boolean {
        return repeatWeek > 0
    }

    fun hasRepeatWeekday(): Boolean {
        return repeatWeekday[0] > 0
    }

    fun hasRepeatDay(): Boolean {
        return repeatDay > 0
    }

    fun hasRepeatEnd(): Boolean {
        return repeatEnd > 0
    }
}
