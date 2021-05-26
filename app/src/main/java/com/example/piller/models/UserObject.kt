package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserObject(
    @SerializedName(DbConstants.userId) var userId: String,
    @SerializedName("email") var email: String,
    @SerializedName("mainProfile") var mainProfile: Profile?,
    @SerializedName("currentProfile") var currentProfile: Profile?
) : Parcelable