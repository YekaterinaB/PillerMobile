package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class ProfileSerializable(
    @SerializedName("name") var name: String,
    @SerializedName("relation") var relation: String
) : Parcelable