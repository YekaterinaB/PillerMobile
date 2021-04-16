package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserObject(
    @SerializedName("userId") var userId: String,
    @SerializedName("email") var email: String,
    @SerializedName("mainProfileName") var mainProfileName: String,
    @SerializedName("currentProfile") var currentProfile: Profile?
) : Parcelable