package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Refill(
    @SerializedName("refill_id") var refillId: String = "",
    @SerializedName("is_to_notify") var isToNotify: Boolean = false,
    @SerializedName("pills_left") var pillsLeft: Int = 0,
    @SerializedName("pills_before_reminder") var pillsBeforeReminder: Int = 1,
    @SerializedName("reminder_time") var reminderTime: String = "00:00"
    ) : Parcelable