package com.example.piller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserObject(
    @SerializedName("userId") var userId: String,
    @SerializedName("email") var email: String,
    @SerializedName("mainProfile") var mainProfile: Profile?,
    @SerializedName("currentProfile") var currentProfile: Profile?,
    @SerializedName("googleUser") var googleUser: Boolean


    ) : Parcelable