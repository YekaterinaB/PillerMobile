package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Dose(
    @SerializedName("dose_id") var doseId: String="" ,
    @SerializedName("measurement_type") var measurementType: String="",
    @SerializedName("total_dose") var totalDose: Int = 0
) : Parcelable