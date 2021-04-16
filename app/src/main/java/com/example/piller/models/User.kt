package com.example.piller.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("email") var email: String,
    @SerializedName("mainProfileName") var mainProfileName: String,
    @SerializedName("password") var password: String
)

