package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Dose(
    @SerializedName(DbConstants.doseId) var doseId: String = DbConstants.defaultStringValue,
    @SerializedName(DbConstants.measurementType) var measurementType: String = DbConstants.defaultStringValue,
    @SerializedName(DbConstants.totalDose) var totalDose: Float = DbConstants.initialTotalDose
) : Parcelable