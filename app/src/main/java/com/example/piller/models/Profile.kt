package com.example.piller.models

import android.os.Parcelable
import com.example.piller.utilities.DbConstants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Profile(
    @SerializedName(DbConstants.profileIdStr) var profileId: String,
    @SerializedName(DbConstants.profileName) var name: String,
    @SerializedName(DbConstants.profileRelation) var relation: String
) : Parcelable