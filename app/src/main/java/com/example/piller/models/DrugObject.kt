package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrugObject(
    @SerializedName("calendar_id") var calendarId: String,
    @SerializedName("name") var drugName: String,
    @SerializedName("rxcui") var rxcui: Int,
    @SerializedName("taken_id") var taken_id: String = "",
    @SerializedName("occurrence") var occurrence: Occurrence = Occurrence(),
    @SerializedName("dose") var dose: Dose = Dose()
) : Parcelable