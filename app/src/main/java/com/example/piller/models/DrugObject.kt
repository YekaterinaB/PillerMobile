package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrugObject(
    @SerializedName(DbConstants.drugId) var drugId: String,
    @SerializedName(DbConstants.CALENDAR_ID) var calendarId: String,
    @SerializedName(DbConstants.drugName) var drugName: String,
    @SerializedName(DbConstants.rxcui) var rxcui: Int,
    @SerializedName("taken_id") var taken_id: String = DbConstants.defaultStringValue,
    @SerializedName(DbConstants.occurrences) var occurrence: Occurrence = Occurrence(),
    @SerializedName(DbConstants.dose) var dose: Dose = Dose(),
    @SerializedName(DbConstants.refill) var refill: Refill = Refill()
) : Parcelable