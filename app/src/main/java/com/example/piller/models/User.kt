package com.example.piller.models
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("email") var email: String,
    @SerializedName("name")var name: String,
    @SerializedName("password")var password: String)

