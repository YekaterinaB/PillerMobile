package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Refill(
    @SerializedName(DbConstants.refillId) var refillId: String = DbConstants.defaultStringValue,
    @SerializedName(DbConstants.isToNotify) var isToNotify: Boolean = false,
    @SerializedName(DbConstants.pillsLeft) var pillsLeft: Int = DbConstants.defaultPillsLeft,
    @SerializedName(DbConstants.pillsBeforeReminder) var pillsBeforeReminder: Int = DbConstants.defaultPillsBeforeReminder,
    @SerializedName(DbConstants.reminderTime) var reminderTime: String = DbConstants.defaultRefillTime
) : Parcelable