package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class ProfileSerializable(
    @SerializedName(DbConstants.profileName) var name: String,
    @SerializedName(DbConstants.profileRelation) var relation: String
) : Parcelable