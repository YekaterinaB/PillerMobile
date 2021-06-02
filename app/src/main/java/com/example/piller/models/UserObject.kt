package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserObject(
    @SerializedName(DbConstants.userId) var userId: String,
    @SerializedName(DbConstants.logged_email) var email: String,
    @SerializedName(DbConstants.main_profile) var mainProfile: Profile?,
    @SerializedName(DbConstants.current_profile) var currentProfile: Profile?
) : Parcelable