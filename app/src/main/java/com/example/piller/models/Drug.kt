package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Drug(
    @SerializedName("drug_name") var drug_name: String,
    @SerializedName("rxcui") var rxcui: Int
) : Parcelable